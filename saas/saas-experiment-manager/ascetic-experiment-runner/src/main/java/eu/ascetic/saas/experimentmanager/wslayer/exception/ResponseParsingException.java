package eu.ascetic.saas.experimentmanager.wslayer.exception;

public class ResponseParsingException extends WSBaseException {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -5034563191530297034L;


	public ResponseParsingException() {
		super();
	}

	public ResponseParsingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ResponseParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResponseParsingException(String message) {
		super(message);
	}

	public ResponseParsingException(Throwable e) {
		super(e);
	}

}
