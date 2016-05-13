package eu.ascetic.saas.experimentmanager.exception;

public class IncorrectScopeException extends Exception {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -5034563191530297034L;


	public IncorrectScopeException() {
		super();
	}

	public IncorrectScopeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IncorrectScopeException(String message, Throwable cause) {
		super(message, cause);
	}

	public IncorrectScopeException(String message) {
		super(message);
	}

	public IncorrectScopeException(Throwable e) {
		super(e);
	}

}
