
package es.bsc.demiurge.core.monitoring.exceptions;

/**
 * @author Mauro Canuto <mauro.canuto@bsc.es>
 * 
 */
public class MonitoringException extends Exception {

    /**
     * Make a new exception.
     *
     * @param message the error message
     */
    public MonitoringException(String message) {
        super(message);
    }
}