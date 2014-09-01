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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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
     *            The argument list to pass to the program. Can be null.
     * @throws SystemCallException
     *             Provides a mechanism to propagate all exception to VMC core.
     */
    @Override
    // FIXME: remove sshCommandName and alter prototype to have single
    // List<String> sshCommands?
    public void runCommand(String sshCommandName, List<String> sshArguments)
            throws SystemCallException {

        String sshCommand;
        // Construct the argument for the SSH command + escaping
        if (sshArguments != null) {
            String sshArgumentsAsString = null;
            for (Iterator<String> iterator = sshArguments.iterator(); iterator
                    .hasNext();) {
                if (sshArgumentsAsString == null) {
                    sshArgumentsAsString = iterator.next();
                } else {
                    sshArgumentsAsString = sshArgumentsAsString + " "
                            + iterator.next();
                }

            }
            sshCommand = sshCommandName + " " + sshArgumentsAsString;
        } else {
            sshCommand = sshCommandName;
        }

        LOGGER.info("Constructing remote system call command: " + sshCommand);

        // Check to see if backtick subsitution is being used
        if (sshCommand.contains("`")) {
            throw new SystemCallException(
                    "Legacy support for command substitution via backticks in bash scripts are not supported by ProcessBuilder, consider replacing with $(...)");
        }

        sshCommand = sshCommand.replace("'", "\\'");
        sshCommand = sshCommand.replace("\"", "\\\"");
        sshCommand = sshCommand.replace("[", "\\[");
        sshCommand = sshCommand.replace("]", "\\]");

        // If using cygwin then we can skip the following escaping as the
        // windows shell ignores these chars anyway
        if (!sshPath.contains("cygwin")) {
            sshCommand = sshCommand.replace("$", "\\$");
        }

        // Construct the SSH command
        commandName = sshPath;

        // Add SSH arguments as a string to member variable
        arguments = new Vector<String>(1);
        arguments.add("-o");
        arguments.add("UserKnownHostsFile=/dev/null");
        arguments.add("-o");
        arguments.add("StrictHostKeyChecking=no");
        arguments.add("-c");
        arguments.add("blowfish");
        arguments.add("-i");
        arguments.add(sshKeyPath);
        arguments.add(sshUser + "@" + hostAddress);
        arguments.add(sshCommand);

        Vector<String> systemCallCommand = new Vector<String>();
        systemCallCommand.add(commandName);

        String commandString = commandName;

        for (int i = 0; i < arguments.size(); i++) {
            systemCallCommand.add(arguments.get(i));
            commandString = commandString + " \"" + arguments.get(i) + "\"";
        }

        // Run the command...
        LOGGER.info("Runnning system call command: " + commandString);
        execute(systemCallCommand);
        LOGGER.debug("Return value is: " + returnValue);
    }

    /**
     * Helper method to convert a script to a single line command.
     * 
     * @param scriptFile
     *            String representation of the script to convert
     * @return The converted script.
     * @throws IOException
     *             if an I/O error occurs reading from the scriptFile
     */
    public static String scriptToSingleLineCommand(File scriptFile)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(scriptFile
                .getAbsolutePath()));
        String scriptAsString = new String(encoded, Charset.defaultCharset());
        scriptAsString = scriptAsString.replaceAll("(?m)^#.*(?:\r?\n)?", "");
        scriptAsString = scriptAsString.replaceAll("(?m)^\\s+", "");
        String convertedScript = scriptAsString.replace("\n", ";");
        return convertedScript;
    }
}
