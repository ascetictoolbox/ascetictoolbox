/**
 *  Copyright 2014 University of Leeds
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
package eu.ascetic.vmic.api.core;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import eu.ascetic.vmic.api.datamodel.GlobalConfiguration;

/**
 * Wrapper class for invoking remote OS level system calls via SSH for the
 * purpose of remotely calling external scripts and programs. Can be called as a
 * thread.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class SystemCallRemote extends SystemCall {

    private String sshPath;
    private String sshKeyPath;
    private String sshUser;
    private String hostAddress;

    /**
     * Initialises an instance of the SystemCallRemote object (Non-threaded).
     * 
     * @param workingDirectory
     *            The directory to work within
     * @param globalConfiguration
     */
    public SystemCallRemote(String workingDirectory,
            GlobalConfiguration globalConfiguration) {
        super(workingDirectory);
        // TODO Auto-generated constructor stub
        sshPath = globalConfiguration.getSshPath();
        sshKeyPath = globalConfiguration.getSshKeyPath();
        sshUser = globalConfiguration.getSshUser();
        hostAddress = globalConfiguration.getHostAddress();
    }

    /**
     * Initialises an instance of the SystemCallRemote object in threaded mode.
     * 
     * @param workingDirectory
     *            The directory to work within
     * @param commandName
     *            The command to run in a thread
     * @param arguments
     *            The arguments of the command to run
     * @param globalConfiguration
     */
    public SystemCallRemote(String workingDirectory, String commandName,
            List<String> arguments, GlobalConfiguration globalConfiguration) {
        super(workingDirectory, commandName, arguments);
        sshPath = globalConfiguration.getSshPath();
        sshKeyPath = globalConfiguration.getSshKeyPath();
        sshUser = globalConfiguration.getSshUser();
        hostAddress = globalConfiguration.getHostAddress();
    }

    /**
     * Run a command via a remote system call over SSH. The return value and
     * output from the command are accessible via:
     * {@link SystemCall#getOutput()} and {@link SystemCall#getReturnValue()}.
     * 
     * @param commandName
     *            The program name to execute.
     * @param arguments
     *            The argument list to pass to the program.
     * @throws SystemCallException
     *             Provides a mechanism to propagate all exception to VMC core.
     */
    @Override
    public void runCommand(String commandName, List<String> arguments)
            throws SystemCallException {

        // Construct the argument for the SSH command + escaping
        String sshArgument = null;
        for (Iterator<String> iterator = arguments.iterator(); iterator
                .hasNext();) {
            sshArgument = sshArgument + " " + iterator.next();
        }

        // Run the command...
        LOGGER.info("Constructing remote system call command: " + commandName
                + " " + sshArgument);

        sshArgument.replace("'", "\\'");
        sshArgument.replace("\"", "\\\"");
        sshArgument.replace("$", "\\$");

        Vector<String> newArguments = new Vector<String>(1);
        newArguments.add("\"" + sshArgument + "\"");

        // Construct the SSH command
        commandName = sshPath + " -i " + sshKeyPath + " " + sshUser + "@"
                + hostAddress;

        // Original SystemCall logic here
        this.commandName = commandName;
        this.arguments = newArguments;

        Vector<String> command = new Vector<String>();
        String commandString = commandName;
        command.add(commandName);

        for (int i = 0; i < arguments.size(); i++) {
            command.add(newArguments.get(i));
            commandString = commandString + " " + arguments.get(i);
        }

        // Run the command...
        LOGGER.info("Runnning system call command: " + commandString);
        execute(command);
        LOGGER.debug("Return value is: " + returnValue);
    }

    /**
     * Helper method to convert a script to a single line command.
     * 
     * @param scriptAsString
     *            String representation of the script to convert
     * @return The converted script.
     */
    public String scriptToSingleLineCommand(String scriptAsString) {
        // TODO
        scriptAsString.replace("\n", ";");
        return null;
    }
}
