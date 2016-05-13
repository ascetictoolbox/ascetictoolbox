package eu.ascetic.saas.experimentmanager.wslayer.exception;

public abstract class WSBaseException extends Exception {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -5034563191530297034L;


	public WSBaseException() {
		super();
	}

	public WSBaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public WSBaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public WSBaseException(String message) {
		super(message);
	}

	public WSBaseException(Throwable e) {
		super(e);
	}

}
