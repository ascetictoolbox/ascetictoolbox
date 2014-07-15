/**
 * Copyright 2014 University of Leeds
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
package eu.ascetic.utils.execution;

import eu.ascetic.utils.execution.stream.RedirectGobbler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class wraps around a Process and provides a clean interface and event
 * based system around processes.
 *
 * This class is similar to @see java.lang.ProcessBuilder.
 *
 * Another form of thread management can be found in:
 * http://download.oracle.com/javase/1,5.0/docs/api/java/util/concurrent/Executor.html
 * @author Richard Kavanagh
 */
public class ManagedProcess {

    private Process process = null;
    private File workingDirectory = null;
    private ManagedProcessNotifier notifier = null;
    private String command = "";
    private ArrayList<String> environment = new ArrayList<>();
    private String stdIn = "";
    private String stdOut = "";
    private String stdError = "";

    @SuppressWarnings("unused")
    /**
     * This ensures the default no-args constructor cannot be used.
     */
    private ManagedProcess() {
    }

    /**
     * This constructs a managed process that will print out to file the streams
     * in a tidy fashion.
     * @param command The command to execute
     * @param environment The environmental variables to use. Each element
     * should be in the format name=value, or null if the subprocess should
     * inherit the environment of the current process. 
     * @param stdIn The filename for standard in, if null or "" then it is discarded.
     * @param stdOut The filename for standard out, if null or "" then output is sent to either screen or discarded.
     * @param stdError The filename for standard error, if null or "" then output is sent to either screen or discarded.
     * @param workingDirectory The working directory from where to perform execution.
     * @param toScreen This states if the output should be sent to screen or not.
     */
    public ManagedProcess(String command, ArrayList<String> environment, String stdIn, String stdOut, String stdError, File workingDirectory, boolean toScreen) {
        persistCommandDescription(environment, stdIn, stdOut, stdError, workingDirectory);
        initCommand(command);
        attachStreams(stdIn, stdOut, stdError, toScreen);
    }

    /**
     * This constructs a managed process that will print out to file the streams
     * in a tidy fashion.
     * @param commands The commands to execute
     * @param environment The environmental variables to use. Each element
     * should be in the format name=value, or null if the subprocess should
     * inherit the environment of the current process. 
     * @param stdIn The filename for standard in, if null or "" then it is discarded.
     * @param stdOut The filename for standard out, if null or "" then output is sent to either screen or discarded.
     * @param stdError The filename for standard error, if null or "" then output is sent to either screen or discarded.
     * @param workingDirectory The working directory from where to perform execution.
     * @param toScreen This states if the output should be sent to screen or not.
     */
    public ManagedProcess(ArrayList<String> commands, ArrayList<String> environment, String stdIn, String stdOut, String stdError, File workingDirectory, boolean toScreen) {
        persistCommandDescription(environment, stdIn, stdOut, stdError, workingDirectory);
        initCommand(commands);
        attachStreams(stdIn, stdOut, stdError, toScreen);
    }

    /**
     * This constructs a managed process that will print out to file the streams
     * in a tidy fashion.
     * @param command The command to execute
     * @param stdIn The filename for standard in, if null or "" then it is discarded.
     * @param stdOut The filename for standard out, if null or "" then output is sent to either screen or discarded.
     * @param stdError The filename for standard error, if null or "" then output is sent to either screen or discarded.
     * @param workingDirectory The working directory from where to perform execution.
     * @param toScreen This states if the output should be sent to screen or not.
     */
    public ManagedProcess(String command, String stdIn, String stdOut, String stdError, File workingDirectory, boolean toScreen) {
        persistCommandDescription(null, stdIn, stdOut, stdError, workingDirectory);
        initCommand(command);
        attachStreams(stdIn, stdOut, stdError, toScreen);
    }

    /**
     * This constructs a managed process that will print out to file the streams
     * in a tidy fashion.
     * @param commands The commands to execute
     * @param stdIn The filename for standard in, if null or "" then it is discarded.
     * @param stdOut The filename for standard out, if null or "" then output is sent to either screen or discarded.
     * @param stdError The filename for standard error, if null or "" then output is sent to either screen or discarded.
     * @param workingDirectory The working directory from where to perform execution.
     * @param toScreen This states if the output should be sent to screen or not.
     */
    public ManagedProcess(ArrayList<String> commands, String stdIn, String stdOut, String stdError, File workingDirectory, boolean toScreen) {
        persistCommandDescription(null, stdIn, stdOut, stdError, workingDirectory);
        initCommand(commands);
        attachStreams(stdIn, stdOut, stdError, toScreen);
    }

    /**
     * This constructs a managed process that will print out to file the streams
     * in a tidy fashion.
     * @param command The command to execute
     * @param stdOut The filename for standard out, if null or "" then output is sent to either screen or discarded.
     * @param stdError The filename for standard error, if null or "" then output is sent to either screen or discarded.
     * @param workingDirectory The working directory from where to perform execution.
     * @param toScreen This states if the output should be sent to screen or not.
     */
    public ManagedProcess(String command, String stdOut, String stdError, File workingDirectory, boolean toScreen) {
        persistCommandDescription(null, "", stdOut, stdError, workingDirectory);
        initCommand(command);
        attachStreams("", stdOut, stdError, toScreen);
    }

    /**
     * This constructs a managed process that will print out to file the streams
     * in a tidy fashion.
     * @param commands The commands to execute
     * @param stdOut The filename for standard out, if null or "" then output is sent to either screen or discarded.
     * @param stdError The filename for standard error, if null or "" then output is sent to either screen or discarded.
     * @param workingDirectory The working directory from where to perform execution.
     * @param toScreen This states if the output should be sent to screen or not.
     */
    public ManagedProcess(ArrayList<String> commands, String stdOut, String stdError, File workingDirectory, boolean toScreen) {
        persistCommandDescription(null, "", stdOut, stdError, workingDirectory);
        initCommand(commands);
        attachStreams("", stdOut, stdError, toScreen);
    }

    /**
     * This saves parameters of the managed process creation process to memory,
     * for later retrieval.
     * @param environment The environmental variables to use. Each element
     * should be in the format name=value, or null if the subprocess should
     * inherit the environment of the current process.
     * @param stdIn The filename for standard in, if null or "" then it is discarded.
     * @param stdOut The filename for standard out, if null or "" then output is sent to either screen or discarded.
     * @param stdError The filename for standard error, if null or "" then output is sent to either screen or discarded.
     * @param workingDirectory The working directory from where to perform execution.
     */
    private void persistCommandDescription(
            ArrayList<String> environment,
            String stdIn,
            String stdOut,
            String stdError,
            File workingDirectory) {
        this.stdIn = stdIn;
        this.stdOut = stdOut;
        this.stdError = stdError;
        this.environment = environment;
        if (workingDirectory.exists() && workingDirectory.isDirectory()) {
            this.workingDirectory = workingDirectory;
        }
    }

    /**
     * This executes a command.
     * @param command The command to execute
     */
    private void initCommand(String command) {
        try {
            this.command = command;
            /*
             * If the working directory does not exist or is not a directory ensure
             * the defaults are used instead.
             */
            if (!workingDirectory.exists() || !workingDirectory.isDirectory()) {
                workingDirectory = null;
            }
            if (environment != null) {

                String[] environmentVars = new String[1];
                environment.toArray(environmentVars);
                process = Runtime.getRuntime().exec(command, environmentVars, workingDirectory);
            } else {
                process = Runtime.getRuntime().exec(command, null, workingDirectory);
            }
        } catch (IOException ex) {
            Logger.getLogger(ManagedProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This executes a sequence of commands.
     * @param commands The commands to execute
     */
    private void initCommand(ArrayList<String> commands) {
        try {
            this.command = commands.toString();
            String[] cmds = (String[]) commands.toArray(new String[0]);
            if (environment != null) {
                String[] environmentVars = new String[1];
                environment.toArray(environmentVars);
                process = Runtime.getRuntime().exec(cmds, environmentVars, workingDirectory);
            } else {
                process = Runtime.getRuntime().exec(cmds, null, workingDirectory);
            }
        } catch (IOException ex) {
            Logger.getLogger(ManagedProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This attaches the input and output streams to a managed command.
     * @param stdIn The filename for standard in, if null or "" then it is discarded.
     * @param stdOut The filename for standard out, if null or "" then output is sent to either screen or discarded.
     * @param stdError The filename for standard error, if null or "" then output is sent to either screen or discarded.
     * @param toScreen This states if the output should be sent to screen or not.
     */
    private void attachStreams(String stdIn, String stdOut, String stdError, boolean toScreen) {
        try {
            try (OutputStream outs = process.getOutputStream()) {
                if (stdIn != null && !stdIn.equals("")) {
                    try (FileInputStream input = new FileInputStream(stdIn)) {
                        int value = input.read();
                        while (value != -1) {
                            outs.write(value);
                            value = input.read();
                        }
                    } catch (IOException e) {
                        Logger.getLogger(ManagedProcess.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }

            FileOutputStream fosError = null;
            if (stdError != null && !stdError.equals("")) {
                fosError = new FileOutputStream(stdError);
            }
            RedirectGobbler errorGobbler = new RedirectGobbler(process.getErrorStream(), fosError, toScreen);
//            ScreenGobbler errorGobbler = new ScreenGobbler(process.getErrorStream());
//            DiscardGobbler errorGobbler = new DiscardGobbler(process.getErrorStream());

            // any output?
            FileOutputStream fosOutput = null;
            if (stdOut != null && !stdOut.equals("")) {
                fosOutput = new FileOutputStream(stdOut);
            }
            RedirectGobbler outputGobbler = new RedirectGobbler(process.getInputStream(), fosOutput, toScreen);
//            ScreenGobbler outputGobbler = new ScreenGobbler(process.getInputStream());
//            DiscardGobbler outputGobbler = new DiscardGobbler(process.getInputStream());

            // kick them off
            new Thread(errorGobbler).start();
            new Thread(outputGobbler).start();

        } catch (IOException e) {
            Logger.getLogger(ManagedProcess.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * This registers an interested party to the event of the process finishing.
     * Notifications to the completion are sent in an unordered fashion.
     * @param listener The listener that is to be registered.
     */
    public void listen(ManagedProcessListener listener) {
        if (notifier == null) {
            notifier = new ManagedProcessNotifier(this);
        }
        notifier.listen(listener);
    }

    /**
     * This removes a listener from the list of interested parties to the end
     * of the processes execution.
     * @param listener The listener to deregister
     */
    public void stoplistening(ManagedProcessListener listener) {
        if (notifier != null) {
            notifier.stoplistening(listener);
        }
    }

    /**
     * This removes all listeners from the list of interested parties to the end
     * of the processes execution.
     */
    public void stoplistening() {
        if (notifier != null) {
            notifier.stoplistening();
        }
    }

    /**
     * This forcefully kills the managed process.
     */
    public void kill() {
        try {
            /**
             * Process resources are not automatically freed until finalization
             * When a Process is instantiated three streams are allocated to handle
             * stdout, stderr, and stdin. These streams do not close on their own
             * until the process object is finalized, which can often result in
             * resource exhaustion. There are at least five different defects in
             * the Sun bug database regarding this issue and several of them provide
             * conflicting information regarding how to properly release system
             * resources tied to an instance of Process.
             *
             * The safe bet for handling this problem today is to explicitly
             * clean up every instance of Process by calling close on each stream
             * made available through Process.getOutputSteam, Process.getInputStream,
             * and Process.getErrorStream, and then call Process.destroy even
             * if the process is already terminated.
             *
             * http://kylecartmell.com/?p=9 Accessed: 30th September 11
             */
            process.getErrorStream().close();
            process.getInputStream().close();
            process.getOutputStream().close();
        } catch (IOException ex) {
            Logger.getLogger(ManagedProcess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //stop listening then kill the thread, this quitely kills the thread.
            //i.e. don't want the notifiy event to fire.
            if (notifier != null) {
                notifier.stoplistening();
            }
            process.destroy();
        }
    }

    /**
     * This indicates if the process has finished or not. This is none blocking,
     * and unlike process.exitValue() does not throw an exception when called.
     * @return If the process has finished or not.
     */
    public boolean hasFinished() {
        try {
            //Note: exitValue is none blocking unlike waitFor()
            process.exitValue();
            /**
             * If the exception has not been thrown then this process has finished.
             */
            return true;
        } catch (IllegalThreadStateException ite) {
            //do nothing the process is still running proceed to finally
        } finally {
        }
        return false;
    }

    /**
     * This returns the exit code for the process. If it has not yet finished
     * then it throws an IllegalThreadStateException.
     * This is best to be used in conjunction with hasFinished or the finish 
     * notification system.
     * @return The exit code for the managed process.
     */
    public int getExitValue() {
        return process.exitValue();
    }

    /**
     * This returns the process that is been managed. If waitFor() must be used
     * it is recommended to use this method and then perform the waitFor() upon
     * the process. The ideal mechanism is to register interest with the events.
     * i.e. listen()
     * @return The underlying process that is been managed.
     */
    public Process getProcess() {
        return process;
    }

    /**
     * The command that launched this managed process.
     * @return the command The command that has been run.
     */
    public String getCommand() {
        return command;
    }

    /**
     * The location that standard in is to be read from.
     * @return the stdIn
     */
    public String getStdIn() {
        return stdIn;
    }

    /**
     * The location that standard out is to be written to.
     * @return the stdOut
     */
    public String getStdOut() {
        return stdOut;
    }

    /**
     * The location that standard error is to be written to.
     * @return the stdError
     */
    public String getStdError() {
        return stdError;
    }

    /**
     * This lists the environmental variables that are in use.
     * @return the environment
     */
    public ArrayList<String> getEnvironment() {
        return environment;
    }

    /**
     * This returns the working directory of this managed process. If the current
     * threads working directory/system default is in use this will return null.
     * @return the working directory
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }
}
