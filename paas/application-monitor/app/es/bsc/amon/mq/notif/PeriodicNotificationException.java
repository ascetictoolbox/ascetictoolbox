package es.bsc.amon.mq.notif;

public class PeriodicNotificationException extends Exception {
	public PeriodicNotificationException(Throwable cause) {
		super(cause);
	}

	public PeriodicNotificationException(String message, Throwable cause) {
		super(message, cause);
	}

	public PeriodicNotificationException(String message) {
		super(message);
	}

	public PeriodicNotificationException() {
	}
}
