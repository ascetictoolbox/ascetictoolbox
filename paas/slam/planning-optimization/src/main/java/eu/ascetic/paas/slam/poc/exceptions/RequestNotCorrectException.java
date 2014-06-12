package eu.ascetic.paas.slam.poc.exceptions;

public class RequestNotCorrectException extends Exception {
    /**
     * The <code>RequestNotCorrectException</code> exception class represents the incoming request is either null or
     * incorrect format.
     * 
     * @author Kuan Lu
     */
    private static final long serialVersionUID = 1L;

    public RequestNotCorrectException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }
}
