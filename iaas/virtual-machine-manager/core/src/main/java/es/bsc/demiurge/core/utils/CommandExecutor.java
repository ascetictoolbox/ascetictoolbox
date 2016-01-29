/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.core.utils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Executes system commands from Java code.
 * I got the code from this tutorial: http://www.mkyong.com/java/how-to-execute-shell-command-from-java/
 * and adapted it.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es).
 */
public class CommandExecutor {

    private static Logger log = LogManager.getLogger(CommandExecutor.class);

    // Suppress default constructor for non-instantiability
    private CommandExecutor() {
        throw new AssertionError();
    }
    
    /**
     * Executes a system command.
     *
     * @param command the command
     * @return the result of executing the command
     */
    public static String executeCommand(String command) throws IOException, InterruptedException {
        StringBuilder result = new StringBuilder();
        Process p;
        BufferedReader reader = null;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
                result.append(System.getProperty("line.separator"));
            }
            return result.toString();
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

}
