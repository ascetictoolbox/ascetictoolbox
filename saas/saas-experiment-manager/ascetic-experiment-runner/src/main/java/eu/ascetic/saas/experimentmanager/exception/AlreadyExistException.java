package eu.ascetic.saas.experimentmanager.exception;

public class AlreadyExistException extends Exception {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -5034563191530297034L;


	public AlreadyExistException() {
		super();
	}

	public AlreadyExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AlreadyExistException(String message, Throwable cause) {
		super(message, cause);
	}

	public AlreadyExistException(String message) {
		super(message);
	}

	public AlreadyExistException(Throwable e) {
		super(e);
	}

}
