package eu.ascetic.saas.experimentmanager.exception;

public class NoMeasureException extends Exception {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -5034563191530297034L;


	public NoMeasureException() {
		super();
	}

	public NoMeasureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NoMeasureException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoMeasureException(String message) {
		super(message);
	}

	public NoMeasureException(Throwable e) {
		super(e);
	}

}
