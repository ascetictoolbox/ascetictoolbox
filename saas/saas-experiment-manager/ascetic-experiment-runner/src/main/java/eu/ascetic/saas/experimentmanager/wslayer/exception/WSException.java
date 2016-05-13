package eu.ascetic.saas.experimentmanager.wslayer.exception;

public class WSException extends WSBaseException {
	
	private int status = -1;

	/**
	 * 
	 */
	private static final long serialVersionUID = 7437773495385311388L;

	public WSException(int status) {
		super();
		this.status=status;
	}

	public WSException(int status, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.status=status;
	}

	public WSException(int status, String message, Throwable cause) {
		super(message, cause);
		this.status=status;
	}

	public WSException(int status, String message) {
		super(message);
		this.status=status;
	}

	public WSException(int status, Throwable e) {
		super(e);
		this.status=status;
	}


	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}
}
