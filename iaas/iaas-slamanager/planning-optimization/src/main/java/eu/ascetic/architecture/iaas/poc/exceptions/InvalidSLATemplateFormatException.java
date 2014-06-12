package eu.ascetic.architecture.iaas.poc.exceptions;

/**
 * The <code>InvalidSLATemplateFormatException</code> exception class represents
 * the incoming SLA template is invalid.
 * 
 * @author Kuan Lu
 */
public class InvalidSLATemplateFormatException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidSLATemplateFormatException() {
		// TODO Auto-generated constructor stub
	}

	public InvalidSLATemplateFormatException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public InvalidSLATemplateFormatException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public InvalidSLATemplateFormatException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
