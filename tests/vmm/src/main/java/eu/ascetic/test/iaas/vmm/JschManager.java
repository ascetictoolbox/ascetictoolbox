/**
 * Copyright 2016 Barcelona Super Computing Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.test.iaas.vmm;

import com.jcraft.jsch.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class JschManager {
    private static Logger log = LogManager.getLogger(JschManager.class);
    private Session session;
    
    /**
     * 
     * @param host the host
     * @param user the user
     * @throws JSchException cmd exception
     */
    public JschManager(String host, String user) throws JSchException{
        log.info("Connecting to " + host + " with " + user + " via private key");
        JSch.setConfig("StrictHostKeyChecking", "no");
        JSch jsch = new JSch();
        jsch.addIdentity("~/ascetic_key");

        session = jsch.getSession(user, host, 22);
        session.connect(120000);
    }
    
    /**
     * Throws a shell command.
     * 
     * @param command the cmd to throw
     * @return output of the command
     * @throws JSchException cmd exception
     * @throws IOException io exception
     */
    public String cmd(String command) throws JSchException, IOException  {
        StringBuilder outputBuffer = new StringBuilder();
        log.info(command);
        Channel channel = this.session.openChannel("exec");
        ((ChannelExec)channel).setCommand(command);
        InputStream commandOutput = channel.getInputStream();
        channel.connect();
        int readByte = commandOutput.read();

        while(readByte != 0xffffffff) {
            outputBuffer.append((char)readByte);
            System.out.print((char)readByte);
            readByte = commandOutput.read();
        }
        channel.disconnect();
        //log.info(outputBuffer.toString());
        return outputBuffer.toString();
    }
    
    /**
     * Closes connection.
     */
    public void close()
    {
        session.disconnect();
    }
    
    /**
     * 
     * @return a cmd session
     */
    public Session getSession() {
        return session;
    }
}

