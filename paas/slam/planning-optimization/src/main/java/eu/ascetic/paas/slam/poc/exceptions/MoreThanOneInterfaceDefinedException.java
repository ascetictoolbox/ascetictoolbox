package eu.ascetic.paas.slam.poc.exceptions;

/**
 * The <code>MoreThanOneInterfaceDefinedException</code> exception class represents there are more than one interfaces
 * defined.
 * 
 * @author Kuan Lu
 */
public class MoreThanOneInterfaceDefinedException extends Exception {

    private static final long serialVersionUID = 1L;

    public MoreThanOneInterfaceDefinedException() {
        // TODO Auto-generated constructor stub
    }

    public MoreThanOneInterfaceDefinedException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public MoreThanOneInterfaceDefinedException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public MoreThanOneInterfaceDefinedException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
