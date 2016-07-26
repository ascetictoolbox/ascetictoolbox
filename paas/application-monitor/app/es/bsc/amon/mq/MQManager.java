package es.bsc.amon.mq;

import com.fasterxml.jackson.databind.node.ObjectNode;
import es.bsc.amon.mq.dispatch.AppEstimationsReader;
import es.bsc.amon.mq.dispatch.InitiateMonitoringDispatcher;
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
	TopicConnection commandTopicConnection;
	TopicConnectionFactory commandTopicConnectionFactory;
	TopicSession commandTopicSession;
	Topic commandTopic;
	MessageConsumer commandTopicMessageConsumer;


	CommandTopicMessageDispatcher commandQueueMessageDispatcherInstance;
	PeriodicNotificationSender periodicNotificationSender;

	Map<String,CommandDispatcher> commandDispatchers = new HashMap<String,CommandDispatcher>();

	AppEstimationsReader estimationsReader;
	public void init() {

		try {
			Logger.info("Initiating Message Queue Manager...");

			context = new InitialContext();

			commandTopicConnectionFactory
					= (TopicConnectionFactory) context.lookup("asceticpaas");
			commandTopicConnection = commandTopicConnectionFactory.createTopicConnection();
			commandTopicConnection.start();

			commandTopicSession = commandTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

			commandTopic = (Topic) context.lookup("appmon");

			commandTopicMessageConsumer = commandTopicSession.createSubscriber(commandTopic);

			commandQueueMessageDispatcherInstance = new CommandTopicMessageDispatcher();
			new Thread(commandQueueMessageDispatcherInstance).start();

			periodicNotificationSender = new PeriodicNotificationSender();
			new Thread(periodicNotificationSender).start();

			commandDispatchers.put(InitiateMonitoringDispatcher.COMMAND_NAME, new InitiateMonitoringDispatcher(commandTopicSession));

			Logger.info("Message Queue Manager Sucessfully created...");

		} catch(JMSException|NamingException e) {
			Logger.error("Error initializing MQ Manager: " + e.getMessage() + ". Continuing startup without MQ services...", e);
		}
		try {
			estimationsReader = new AppEstimationsReader();
		} catch(Exception e) {
			Logger.error("Error instantiating AppEstimationsReader: " + e.getMessage(),e);
		}
	}

	public void stop() {
		if(estimationsReader != null) estimationsReader.stop();

		if(commandQueueMessageDispatcherInstance != null) commandQueueMessageDispatcherInstance.running = false;
		if(periodicNotificationSender != null) periodicNotificationSender.running = false;
		try {
			if(commandTopicMessageConsumer != null) commandTopicMessageConsumer.close();
			if(commandTopicSession != null) commandTopicSession.close();
			if(commandTopicConnection != null) commandTopicConnection.close();
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

	private class CommandTopicMessageDispatcher implements Runnable {
		boolean running;
		@Override
		public void run() {
			running = true;
			while(running) {
				try {

					TextMessage message = (TextMessage) commandTopicMessageConsumer.receive();
					Logger.debug("received message: " + message.getText());

					ObjectNode on = (ObjectNode)Json.parse(message.getText());
					String command =  on.get(CommandDispatcher.FIELD_COMMAND).textValue();
					commandDispatchers.get(command).onCommand(on);
				} catch(Exception e) {
					if(running) {
						Logger.warn("Error dispatching messages: " + e.getMessage());
						Logger.warn("Reconnecting to the MQ");
						try {
							commandTopicConnectionFactory
									= (TopicConnectionFactory) context.lookup("asceticpaas");
							commandTopicConnection = commandTopicConnectionFactory.createTopicConnection();
							commandTopicConnection.start();

							commandTopicSession = commandTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

							commandTopic = (Topic) context.lookup("appmon");

							commandTopicMessageConsumer = commandTopicSession.createSubscriber(commandTopic);
						} catch(NamingException | JMSException re) {
							Logger.error(re.getMessage(),re);
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e1) {
								Logger.error(e1.getMessage(),e1);
							}
						}

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
			Logger.debug("Starting PeriodicNotificationSender thread");
			while(running) {
				synchronized (notifiers) {
					long now = System.currentTimeMillis();
					for(Tuple t : notifiers) {
						if(t.nextNotification <= now) {
							try {
//								Logger.debug("Time to send notification for " + t.notifier.toString());
								t.notifier.sendNotification();
							} catch(Exception e) {
								Logger.warn("Error sending notification: " + e.getMessage(), e);
							} finally {
								t.nextNotification = now + t.notifier.getFrequency();
							}
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
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Logger.warn(e.getMessage(),e);
				}
			}
			Logger.warn("Exiting from PeriodicNotificationSender thread");
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
