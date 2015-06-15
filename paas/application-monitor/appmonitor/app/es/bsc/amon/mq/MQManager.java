package es.bsc.amon.mq;

import com.fasterxml.jackson.databind.node.ObjectNode;
import es.bsc.amon.mq.dispatch.InitiateMonitoringDispatcher;
import es.bsc.amon.mq.notif.PeriodicNotificationException;
import es.bsc.amon.mq.notif.PeriodicNotifier;
import play.Logger;
import play.libs.Json;

import javax.jms.*;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.*;

public enum MQManager {
	INSTANCE;

	Context context;
	Connection connection;
	ConnectionFactory connectionFactory;
	Session session;
	Queue queue;
	MessageConsumer messageConsumer;


	MessageDispatcher messageDispatcherInstance;
	PeriodicNotificationSender periodicNotificationSender;

	Map<String,CommandDispatcher> commandDispatchers = new HashMap<String,CommandDispatcher>();

	public void init() {

		try {
			Logger.info("Initiating Message Queue Manager...");

			context = new InitialContext();

			connectionFactory
					= (ConnectionFactory) context.lookup("asceticpaas");
			connection = connectionFactory.createConnection();
			connection.start();

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			queue = (Queue) context.lookup("appmon");

			messageConsumer = session.createConsumer(queue);

			messageDispatcherInstance = new MessageDispatcher();
			new Thread(messageDispatcherInstance).start();

			periodicNotificationSender = new PeriodicNotificationSender();
			new Thread(periodicNotificationSender).start();

			commandDispatchers.put(InitiateMonitoringDispatcher.COMMAND_NAME, new InitiateMonitoringDispatcher(session));

			Logger.info("Message Queue Manager Sucessfully created...");

		} catch(JMSException|NamingException e) {
			Logger.error("Error initializing MQ Manager: " + e.getMessage() + " Continuing startup without MQ services...");
		}
	}

	public void stop() {
		if(messageDispatcherInstance != null) messageDispatcherInstance.running = false;
		if(periodicNotificationSender != null) periodicNotificationSender.running = false;
		try {
			if(messageConsumer != null) messageConsumer.close();
			if(session != null) session.close();
			if(connection != null) connection.close();
			if(context != null) context.close();
		} catch(Exception e) {
			Logger.error(e.getMessage());
		}
	}

	public void addPeriodicNotifier(PeriodicNotifier pn) {
		periodicNotificationSender.addNotifier(pn);
	}

	public void removeNotifier(PeriodicNotifier pn) {
		periodicNotificationSender.askForRemoval(pn);
	}

	private class MessageDispatcher implements Runnable {
		boolean running;
		@Override
		public void run() {
			running = true;
			while(running) {
				try {
					TextMessage message = (TextMessage)messageConsumer.receive();
					Logger.debug("received message: " + message.getText());

					ObjectNode on = (ObjectNode)Json.parse(message.getText());
					String command =  on.get(CommandDispatcher.FIELD_COMMAND).textValue();
					commandDispatchers.get(command).onCommand(on);
				} catch(Exception e) {
					if(running) {
						Logger.error("Error dispatching messages: " + e.getMessage(), e);
					} else {
						Logger.debug("While closing MessageDispatcher: " + e.getMessage(), e);
					}
				}
				Thread.yield();
			}
			Logger.info("MessageDispatcher successfully finished...");
		}
	}

	private static class PeriodicNotificationSender implements Runnable {
		boolean running;

		private List<Tuple> notifiers = Collections.synchronizedList(new LinkedList<Tuple>());
		private Set<Tuple> askedForRemoval = Collections.synchronizedSet(new HashSet<Tuple>());

		public void addNotifier(PeriodicNotifier pn) {
			notifiers.add(new Tuple(pn));
		}

		public void askForRemoval(PeriodicNotifier pn) {
			synchronized (notifiers) {
				for(Iterator<Tuple> tit = notifiers.iterator() ; tit.hasNext() ;) {
					Tuple t = tit.next();
					if(t.notifier == pn) {
						askedForRemoval.add(t);
					}
				}
			}
		}

		@Override
		public void run() {
			running = true;
			while(running) {
				synchronized (notifiers) {
					long now = System.currentTimeMillis();
					for(Tuple t : notifiers) {
						if(t.nextNotification <= now) {
							try {
								t.notifier.sendNotification();
							} catch(PeriodicNotificationException e) {
								Logger.warn("Error sending notification: " + e.getMessage(), e);
							}
							t.nextNotification = now + t.notifier.getFrequency();
						}
					}
					if(askedForRemoval.size() > 0) {
						Logger.debug("Removing the clients");
						synchronized (askedForRemoval) {
							notifiers.removeAll(askedForRemoval);
							askedForRemoval.clear();
						}
					}
				}
				Thread.yield();
			}
		}

		private static class Tuple {
			long nextNotification;
			final PeriodicNotifier notifier;

			public Tuple(PeriodicNotifier notifier) {
				this.notifier = notifier;
				nextNotification = System.currentTimeMillis() + notifier.getFrequency();
			}
		}
	}

}
