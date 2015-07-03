package integratedtoolkit.connectors;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;
import integratedtoolkit.ITConstants;

import integratedtoolkit.connectors.utils.KeyManager;
import integratedtoolkit.types.CloudImageDescription;

public abstract class AbstractSSHConnector extends AbstractConnector {

    //private Map<String, Connection> ipToConnection;    
    private static Integer MAX_ALLOWED_ERRORS = 3;
    private static Integer RETRY_TIME = 15; //Seconds
    private String defaultUser = System.getProperty("user.name");
    private String keyPairName;
    private String keyPairLocation = System.getProperty("user.name") + File.separator + ".ssh";

    public AbstractSSHConnector(String providerName, HashMap<String, String> props) {
        super(providerName, props);
        if (props.get("vm-user") != null) {
            defaultUser = props.get("vm-user");
        }
        if (props.get("vm-keypair-name") != null) {
            keyPairName = props.get("vm-keypair-name");
        }
        if (props.get("vm-keypair-location") != null) {
            keyPairLocation = props.get("vm-keypair-location");
        }

    }

    @Override
    public void configureAccess(String workerIP, String user, String password) throws ConnectorException {
        logger.info("Configuring access for user " + user + " in " + workerIP);
        Session c = null;
        try {
            putInKnownHosts(workerIP);
            String keypair = KeyManager.getKeyPair();
            String passwordOrKeyPair = null;
            boolean setPassword = false;
            if (user == null) {
                user = defaultUser;
            }
            if (password != null) {
                setPassword = true;
                passwordOrKeyPair = password;
            } else if (keyPairName != null) {
                setPassword = false;
                passwordOrKeyPair = keyPairLocation + File.separator + keyPairName;
            }
            c = getSession(workerIP, user, setPassword, passwordOrKeyPair);
            if (keypair == null) {
                throw new ConnectorException("There are no key pair to configure. Please create with ssh-keygen tool");
            }
            logger.debug("Configuring key pair: " + keypair);
            configureKeys(workerIP, user, c, KeyManager.getPublicKey(keypair), KeyManager.getPrivateKey(keypair), KeyManager.getKeyType());

        } catch (Exception e) {
            throw new ConnectorException(e);
        } finally {
            if (c != null && c.isConnected()) {
                logger.debug("Disconnecting session");
                c.disconnect();
            }
        }

    }

    @Override
    public void prepareMachine(String ip, CloudImageDescription cid)
            throws ConnectorException {
        String user = cid.getProperties().get(ITConstants.USER);
        LinkedList<String[]> packages = cid.getPackages();
        if (!packages.isEmpty()) {
            Session c = null;
            ChannelSftp client = null;
            try {
                c = getSession(ip, user, false, null);
                client = (ChannelSftp) c.openChannel("sftp");
                client.connect();
                for (String[] p : packages) {
                    String[] path = p[0].split("/");
                    String name = path[path.length - 1];
                    String target = p[1] + "/" + name;
                    if (client == null) {
                        if (c != null) {
                            c.disconnect();
                        }
                        throw new ConnectorException("Can not connect to " + ip);
                    }

                    client.put(p[0], target, new MyProgressMonitor());
                    client.chmod(Integer.parseInt("700", 8), target);
                    // Extracting Worker package
                    String command = "tar xzf " + target + " -C " + p[1] + " && rm " + p[1] + "/" + name;
                    executeTask(ip, user, command, c);
                    //Adding classpath in bashrc
                    command = "echo \"\nfor i in " + p[1] + "/*.jar ; "
                            + "do\n"
                            + "\texport CLASSPATH=\\$CLASSPATH:\\$i\n"
                            + "done\" >> /home/" + user + "/.bashrc";
                    executeTask(ip, user, command, c);
                }

            } catch (Exception e) {
                logger.error("Failed preparing the Machine " + ip, e);
                throw new ConnectorException("Failed preparing the Machine " + ip
                        + ": " + e.getMessage());
            } finally {
                if (client != null && client.isConnected()) {
                    client.disconnect();
                }
                if (c != null) {
                    c.disconnect();
                }

            }
        }

    }

    private void putInKnownHosts(String workerIP) throws ConnectorException {
        logger.debug("Putting id of new machine in master machine known hosts");

        String key = new String();
        String[] cmd = {"/bin/sh", "-c", "ssh-keyscan -t rsa,dsa " + workerIP};
        int errors = 0;
        String errorString = null;
        while (key.isEmpty() && errors < MAX_ALLOWED_ERRORS) {
            InputStream errStream = null;
            InputStream outStream = null;
            try {
                Process p = Runtime.getRuntime().exec(cmd);
                errStream = p.getErrorStream();
                outStream = p.getInputStream();
                p.waitFor();
                if (p.exitValue() == 0) {
                    key = readInputStream(outStream);
                } else {
                    errorString = readInputStream(outStream);
                    errors++;
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {

                    }
                }
            } catch (Exception e) {
                throw new ConnectorException(e);
            } finally {
                try {
                    outStream.close();
                    errStream.close();
                } catch (IOException e) {
                    logger.debug("Exception closing streams (" + e.getMessage() + ")");
                }

            }
            //logger.debug("Key is " + key);
        }
        if (errors == MAX_ALLOWED_ERRORS) {
            throw new ConnectorException(" Error executing key-scan command: " + errorString);
        }
        cmd = new String[]{"/bin/sh", "-c", "/bin/echo " + "\"" + key + "\"" + " >> " + System.getProperty("user.home") + "/.ssh/known_hosts"};

        synchronized (knownHosts) {
            errors = 0;
            int exitValue = -1;
            while (exitValue == 0 && errors < MAX_ALLOWED_ERRORS) {
                InputStream errStream = null;
                InputStream outStream = null;
                try {
                    Process p = Runtime.getRuntime().exec(cmd);
                    errStream = p.getErrorStream();
                    outStream = p.getInputStream();
                    p.waitFor();
                    if (p.exitValue() != 0) {
                        errorString = readInputStream(outStream);
                        errors++;
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {

                        }
                    }
                } catch (Exception e) {
                    throw new ConnectorException(e);
                } finally {
                    try {
                        outStream.close();
                        errStream.close();
                    } catch (IOException e) {
                        logger.debug("Exception closing streams (" + e.getMessage() + ")");
                    }

                }
            }
        }
        if (errors == MAX_ALLOWED_ERRORS) {
            throw new ConnectorException(" Error setting key in local known_hosts: " + errorString);
        }

    }

    public void configureKeys(String workerIP, String user, Session c,
            String publicKey, String privateKey, String keyType)
            throws ConnectorException {
        try {
            String command = "/bin/echo \"" + publicKey + "\" > /home/" + user + "/.ssh/" + keyType + ".pub";
            executeTask(workerIP, user, command, c);
            command = "/bin/echo \"" + publicKey + "\" >> /home/" + user + "/.ssh/authorized_keys";
            executeTask(workerIP, user, command, c);
            command = "/bin/echo \"" + privateKey + "\" > /home/" + user + "/.ssh/" + keyType + ";"
                    + "chmod 600 /home/" + user + "/.ssh/" + keyType;
            executeTask(workerIP, user, command, c);
        } catch (Exception e) {
            throw new ConnectorException(e);
        }

    }

    
    protected void executeTask(String ip, String user, String command, Session session) throws ConnectorException {
        ChannelExec exec = null;
        try {
            exec = (ChannelExec) session.openChannel("exec");
            exec.setCommand(command);
            InputStream inputStream = exec.getErrStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            exec.connect();
            int exitStatus = -1;
            while (exitStatus < 0) {
                exitStatus = exec.getExitStatus();
                if (exitStatus == 0) {
                    bufferedReader.close();
                    inputStream.close();
                    logger.debug("Command successfully executed: " + command);
                    return;
                } else if (exitStatus > 0) {
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append('\n');
                    }
                    logger.debug("Failed. Returned std error: " + stringBuilder.toString());
                    bufferedReader.close();
                    inputStream.close();
                    throw new Exception("Failed to execute command " + command + " in " + ip + " (exit status:" + exitStatus + ")");
                }
                logger.debug("Command still on execution");
                try {
                    Thread.sleep(RETRY_TIME * 1000);
                } catch (InterruptedException e) {
                    logger.debug("Sleep interrupted");
                }

            }
            bufferedReader.close();
            inputStream.close();

        } catch (Exception e) {
            logger.error("Exception running command on " + user + "@" + ip, e);
            throw new ConnectorException(e);
        } finally {
            if (exec != null && exec.isConnected()) {
                logger.debug("Disconnecting exec channel");
                exec.disconnect();
            }
        }
    }

    private String readInputStream(InputStream is) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }
        } catch (Exception e) {
            throw (e);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                logger.debug("Exception closing the reader (" + e.getMessage() + ")");
            }
        }
        return stringBuilder.toString();
    }

    private static Session getSession(String host, String user, boolean password, String keyPairOrPassword) throws Exception {
        //String[] client2server = ("aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc").split(",");
        //String[] server2client = ("aes256-ctr,aes192-ctr,aes128-ctr,blowfish-ctr,aes256-cbc,aes192-cbc,aes128-cbc,blowfish-cbc").split(",");
        Session session = null;
        JSch jsch = new JSch();
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        if (keyPairOrPassword == null) {

            password = false;
            keyPairOrPassword = KeyManager.getKeyPair();
            logger.warn("Neither password nor key-pair specified. Trying with default key-pair (" + KeyManager.getKeyPair() + ")");
        }
        int errors = 0;
        JSchException exception = null;
        while (errors < MAX_ALLOWED_ERRORS) {
            try {
                if (password) {
                    session = jsch.getSession(user, host, 22);
                    session.setPassword(keyPairOrPassword);
                } else {
                    jsch.addIdentity(keyPairOrPassword);
                    session = jsch.getSession(user, host, 22);
                }
                session.setConfig(config);
                session.connect();
                if (session.isConnected()) {
                    return session;
                } else {
                    errors++;
                    if (password) {
                        logger.warn("Error connecting to " + user + "@" + host + " with password. Retrying after " + RETRY_TIME * errors + " seconds...");
                    } else {
                        logger.warn("Error connecting to " + user + "@" + host + " with public key" + keyPairOrPassword + ". Retrying after " + RETRY_TIME * errors + " seconds...");
                    }

                }
            } catch (JSchException e) {
                errors++;
                exception = e;
                logger.warn("Error creating session to " + user + "@" + host + "(" + e.getMessage() + "). Retrying after " + RETRY_TIME * errors + " seconds...");
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            }
            try {
                Thread.sleep(RETRY_TIME * errors * 1000);
            } catch (Exception e) {
                logger.debug("Sleep interrumped");
            }
        }
        if (exception != null) {
            throw new Exception("Error creating session to " + user + "@" + host, exception);
        } else {
            throw new Exception("Error creating session to " + user + "@" + host);
        }
    }

    public static class MyProgressMonitor implements SftpProgressMonitor {

        long max = 1;
        long count = 0;

        public MyProgressMonitor() {

        }

        @Override
        public boolean count(long count) {
            this.count += count;
            float percent = this.count * 100 / max;
            logger.debug("..." + percent + "%");
            return true;
        }

        @Override
        public void end() {
            logger.debug("Operation Finished");

        }

        @Override
        public void init(int op, String src, String dest, long max) {
            logger.debug("Starting " + ((op == SftpProgressMonitor.PUT)
                    ? "put" : "get") + ": " + src);
            this.max = max;
        }

    }
}
