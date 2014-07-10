package es.bsc.vmmanagercore.rest;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * This class implements the REST calls that are related with the logs system.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class LogCallsManager {

    public LogCallsManager() { }

    /**
     * Returns the log file. This log file contains information about the decisions made by the scheduling algorithms.
     *
     * @return the log file
     */
    public String getLogs() {
        // Read the logs file and return its content.
        // If for some reason the logs cannot be read, return an empty string
        String logs;
        try {
            BufferedReader br = new BufferedReader(new FileReader("log/vmmanager.log"));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            logs = sb.toString();
        } catch (Exception e) {
            return "";
        }
        return logs;
    }
}
