package es.bsc.amon.mq.notif;

public interface PeriodicNotifier {
	long getFrequency();
	void sendNotification() throws PeriodicNotificationException;
}
