package es.bsc.vmmanagercore.rest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class implements the REST calls that are related with the logs system.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class LogCallsManager {

    /**
     * Class constructor.
     */
    public LogCallsManager() { }

    /**
     * Returns the log file. This log file contains information about the decisions made by the scheduling algorithms.
     *
     * @return the log file or an empty string if the logs could not be read
     */
    public String getLogs() {
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
        } catch (IOException e) {
            return "";
        }
        return logs;
    }
}
