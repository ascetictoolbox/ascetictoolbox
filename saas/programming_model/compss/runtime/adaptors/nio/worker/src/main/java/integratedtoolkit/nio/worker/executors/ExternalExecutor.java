package integratedtoolkit.nio.worker.executors;

import integratedtoolkit.api.ITExecution;
import integratedtoolkit.nio.NIOParam;
import integratedtoolkit.nio.NIOTask;
import integratedtoolkit.nio.worker.NIOWorker;
import integratedtoolkit.nio.worker.ThreadPrintStream;
import integratedtoolkit.util.StreamGobbler;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public abstract class ExternalExecutor extends Executor {

    @Override
    String createSandBox() throws Exception {
        File wdirFile = new File(NIOWorker.workingDir + File.separator + "sand_" + UUID.randomUUID().hashCode());
        if (wdirFile.mkdir()) {
            return wdirFile.getAbsolutePath();
        } else {
            throw new Exception("Sandbox not created");
        }
    }

    @Override
    void executeTask(String sandBox, NIOTask nt, NIOWorker nw) throws Exception {
        Map<String, String> env = getEnvironment(nt);
        ArrayList<String> args = getLaunchCommand(nt);
        addArguments(args, nt);
        String strArgs = getArgumentsAsString(args);
        addEnvironment(env, nt, nw);
        ArrayList<String> command = new ArrayList<String>();
        command.add("/bin/bash");
        command.add("-e");
        command.add("-c");
        command.add(strArgs);
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("EXECUTOR COMMAND: ");
            for (String c : command) {
                sb.append(c).append(" ");
            }
            logger.debug(sb.toString());
        }

        executeExternal(nt.getJobId(), command, env, sandBox);
    }

    private String getArgumentsAsString(ArrayList<String> args) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String c : args) {
            if (!first) {
                sb.append(" ");
            } else {
                first = false;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    @Override
    void removeSandBox(String sandBox) throws IOException {
        File wdirFile = new File(sandBox);
        removeSandBox(wdirFile);
    }

    private void removeSandBox(File f) throws IOException {
        if (f.exists()) {
            if (f.isDirectory()) {
                for (File child : f.listFiles()) {
                    removeSandBox(child);
                }
            }
            Files.delete(f.toPath());

        }
    }

    public abstract Map<String, String> getEnvironment(NIOTask nt);

    public abstract ArrayList<String> getLaunchCommand(NIOTask nt);

    private static void addArguments(ArrayList<String> lArgs, NIOTask nt) {
        lArgs.add(Boolean.toString(nt.workerDebug));
        lArgs.add(nt.getClassName());
        lArgs.add(nt.getMethodName());
        lArgs.add(Boolean.toString(nt.isHasTarget()));
        lArgs.add(Integer.toString(nt.getNumParams()));
        for (NIOParam param : nt.getParams()) {
            ITExecution.ParamType type = param.getType();
            lArgs.add(Integer.toString(type.ordinal()));
            if (type == ITExecution.ParamType.FILE_T || type == ITExecution.ParamType.OBJECT_T) {
                lArgs.add(param.getValue().toString());
                if (type == ITExecution.ParamType.OBJECT_T) {
                    if (param.isWrite()) {
                        lArgs.add("W");
                    } else {
                        lArgs.add("R"); // for the worker to know it must write the object to disk

                    }
                }
            } else if (type == ITExecution.ParamType.STRING_T) {

                String value = param.getValue().toString();
                String[] vals = value.split(" ");
                int numSubStrings = vals.length;
                lArgs.add(Integer.toString(numSubStrings));
                for (String v : vals) {
                    lArgs.add(v);
                }
            } else { // Basic types
                lArgs.add(param.getValue().toString());
            }
        }
    }

    private void addEnvironment(Map<String, String> env, NIOTask nt, NIOWorker nw) {
        env.put("IT_WORKING_DIR", nw.getWorkingDir());
        env.put("IT_APP_DIR", nt.appDir);
    }

    private void executeExternal(int jobId, ArrayList<String> command, Map<String, String> env, String sandbox) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(sandbox));
        pb.environment().putAll(env);
        //pb.redirectOutput(new File("/dev/null"));
        Process execProc = null;
        try {
            logger.debug("Starting process ...");
            execProc = pb.start();
            try {
                execProc.getOutputStream().close();
            } catch (IOException e) {
                // Stream closed
            }
            /*BufferedReader reader = new BufferedReader(new InputStreamReader(execProc.getInputStream()));
             String line;
             while ((line = reader.readLine()) != null) {
             System.out.println(line);
             }
             reader = new BufferedReader(new InputStreamReader(execProc.getErrorStream()));
             while ((line = reader.readLine()) != null) {
             System.err.println(line);
             }*/
            logger.debug("Starting stdout/stderr gobblers ...");
            PrintStream out = ((ThreadPrintStream) System.out).getStream();
            PrintStream err = ((ThreadPrintStream) System.err).getStream();
            out.println("L'out funciona");
            err.println("L'error funciona");
            StreamGobbler outputGobbler = new StreamGobbler(execProc.getInputStream(), out);
            StreamGobbler errorGobbler = new StreamGobbler(execProc.getErrorStream(), err);
            outputGobbler.start();
            errorGobbler.start();
            int exitValue = execProc.waitFor();
            logger.debug("Task finished. Waiting for gobblers to end...");
            outputGobbler.join();
            errorGobbler.join();
            if (exitValue != 0) {
                throw new JobExecutionException("Job " + jobId + " has failed. Exit values is " + exitValue);
            } else {
                logger.debug("Job" + jobId + " has finished with exit value 0");
            }
        } catch (IOException e) {
            System.err.println("Exception starting process  " + jobId);
            throw e;
        } catch (InterruptedException e) {
            System.err.println("Process interrupted " + jobId);
            throw e;
        } finally {
            if (execProc != null) {
                if (execProc.getInputStream() != null) {
                    try {
                        execProc.getInputStream().close();
                    } catch (IOException e) {

                    }
                }
                if (execProc.getErrorStream() != null) {
                    try {
                        execProc.getErrorStream().close();
                    } catch (IOException e) {

                    }
                }
            }
        }
    }
}
