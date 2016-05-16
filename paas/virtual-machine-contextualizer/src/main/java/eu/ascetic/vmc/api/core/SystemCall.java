/**
 *  Copyright 2013 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.vmc.api.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Wrapper class for invoking OS level system calls for the purpose of calling
 * external scripts and programs.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.3
 */
public class SystemCall {

    private static final int OUTPUT_ARRAY_INITIAL_CAPACITY = 50;
    private static final int THREAD_SLEEP_TIME = 250;
    public static final int RETURN_VALUE_ON_ERROR = -2;
    private String commandName;
    private List<String> output;
    private int returnValue;
    private String workingDirectory;

    protected static final Logger LOGGER = Logger.getLogger(SystemCall.class);

    /**
     * Initialises an instance of the SystemCall object.
     * 
     * @param workingDirectory
     *            The directory to work within
     */
    public SystemCall(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        output = new ArrayList<String>(OUTPUT_ARRAY_INITIAL_CAPACITY);
        returnValue = -1;
    }

    /**
     * Run a command via a system call. The return value and output from the
     * command are accessible via: {@link SystemCall#getOutput()} and
     * {@link SystemCall#getReturnValue()}.
     * 
     * @param commandName
     *            The program name to execute.
     * @param arguments
     *            The argument list to pass to the program.
     * @throws SystemCallException
     *             Provides a mechanism to propagate all exception to VMC core.
     */
    public void runCommand(String commandName, List<String> arguments)
            throws SystemCallException {

        ArrayList<String> command = new ArrayList<String>();
        String commandString = commandName;

        command.add(commandName);

        for (int i = 0; i < arguments.size(); i++) {
            command.add(arguments.get(i));
            commandString = commandString + " " + arguments.get(i); // NOSONAR Overhead is minimal
        }

        // Run the command...
        LOGGER.info("Runnning external command: " + commandString);
        execute(command);
        LOGGER.debug("Return value is: " + returnValue);
    }

    /**
     * Execute using {@link ProcessBuilder} a command using the environment of
     * the JVM.
     * 
     * @param command
     *            The command to execute.
     * @throws SystemCallException
     *             Provides a mechanism to propagate all exception to VMC core.
     */
    private void execute(List<String> command) throws SystemCallException { // NOSONAR Complexity necessary
        ProcessBuilder pb = new ProcessBuilder(command);
        File dir = new File(workingDirectory);
        pb.directory(dir);
        pb.redirectErrorStream(true);

        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            LOGGER.error("Error!", e);
            try {
                Thread.sleep(THREAD_SLEEP_TIME);
            } catch (InterruptedException e1) {
                // Do nothing...
            }
            returnValue = RETURN_VALUE_ON_ERROR;
            throw new SystemCallException("Failed to start process!", e);
        }

        String line = null;
        InputStream stdout = p.getInputStream();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stdout, StandardCharsets.UTF_8));

        try {
            while ((line = reader.readLine()) != null) {
                LOGGER.debug("Script output: " + line);
                output.add(line);
            }
        } catch (IOException e) {
            LOGGER.error("Error!", e);
            returnValue = RETURN_VALUE_ON_ERROR;
            throw new SystemCallException("Failed to read line from stdout!",
                    e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                LOGGER.error("Error!", e);
                returnValue = RETURN_VALUE_ON_ERROR;
                throw new SystemCallException("Failed to close stdout!", e);
            }
        }

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            LOGGER.error("Error!", e);
            returnValue = RETURN_VALUE_ON_ERROR;
            throw new SystemCallException(
                    "Interrupted while waiting for process to terminate!", e);
        }

        returnValue = p.exitValue();
    }

    /**
     * @return the commandName
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * @return the output
     */
    public List<String> getOutput() {
        return output;
    }

    /**
     * @return the returnValue
     */
    public int getReturnValue() {
        return returnValue;
    }
}
