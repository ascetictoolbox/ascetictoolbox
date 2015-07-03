package integratedtoolkit.util;


/**
 * The ElementNotFoundException is an Exception that will arise when some 
 * element that someone was looking for in a set is not inside it. 
 */
public class ElementNotFoundException extends Exception {

    /**
     * Constructs a new ElementNotFoundException with the default messatge
     */
    public ElementNotFoundException() {
        super("Cannot find the requested element");
    }

    /**
     * Constructs a new ElementNotFoundException with that message
     * @param message Message that will return the exception
     */
    public ElementNotFoundException(String message) {
        super(message);
    }
    
}
