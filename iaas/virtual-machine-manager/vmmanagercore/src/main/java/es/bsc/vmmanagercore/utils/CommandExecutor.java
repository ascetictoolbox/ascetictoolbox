package es.bsc.vmmanagercore.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Executes system commands from Java code.
 * I got the code from this tutorial: http://www.mkyong.com/java/how-to-execute-shell-command-from-java/
 * and adapted it.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es).
 */
public class CommandExecutor {

    /**
     * Executes a system command.
     *
     * @param command the command
     * @return the result of executing the command
     */
    public static String executeCommand(String command) {
        StringBuilder result = new StringBuilder();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
                result.append(System.getProperty("line.separator"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

}
