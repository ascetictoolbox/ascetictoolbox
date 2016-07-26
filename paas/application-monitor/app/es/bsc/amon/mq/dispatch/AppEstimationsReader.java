package es.bsc.amon.mq.dispatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import play.Logger;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class AppEstimationsReader {

	boolean running = true;
	Thread theThread;

	// 8 Apr 2016 08:57:56 GMT
	public static final DateFormat emDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
	public static final TimeZone emTimeZone = TimeZone.getTimeZone("GMT");

	public void stop() {
		running = false;
	}
	public AppEstimationsReader() {
		try {
			theThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Logger.info("Initializing Estimations Reader thread");
						Properties p = new Properties();
						p.load(InitiateMonitoringDispatcher.class.getResourceAsStream("/jndi.properties"));
						p.load(InitiateMonitoringDispatcher.class.getResourceAsStream("/jndiEstimations.properties"));
						final Context context = new InitialContext(p);
						TopicConnectionFactory connectionFactory
								= (TopicConnectionFactory) context.lookup("asceticpaas");
						Topic topic = (Topic) context.lookup("prediction");
						TopicConnection connection = connectionFactory.createTopicConnection();
						connection.start();
						TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
						TopicSubscriber clientTopic = session.createSubscriber(topic);
						while (running) {
							try {
								TextMessage tm = (TextMessage) clientTopic.receive(5000);
//						{"lakjsf":null,"provider":"00000","applicationid":"maximTestApp","deploymentid":"938","vms":["5699"],"unit":"WATTHOUR","generattiontimestamp":"8 Apr 2016 08:57:56 GMT","referredtimestamp":"8 Apr 2016 08:57:56 GMT","value":48.36}
//								To: {"ApplicationId":"SinusApp","Timestamp":1431592067367,"Terms":{"metric":9.862471417356321}}
								if (tm != null) {
									System.out.println("received message: " + tm.getText());

									ObjectNode estimationFromEM = (ObjectNode) new ObjectMapper().readTree(tm.getText());
									ObjectNode metricToSLAM = JsonNodeFactory.instance.objectNode();
									metricToSLAM.set(SLAM_APPID, estimationFromEM.get(EM_APPID));
									metricToSLAM.set(SLAM_DEPLOYMENTID, estimationFromEM.get(EM_DEPLOYMENTID));

									long timestamp = System.currentTimeMillis();
									String dateStr = null;
									try {
										dateStr = estimationFromEM.get(EM_REFERREDTIMESTAMP).asText();
										Calendar c = GregorianCalendar.getInstance(emTimeZone);
										c.setTime(emDateFormat.parse(dateStr));
										timestamp = c.getTimeInMillis();
									} catch(Exception e) {
										Logger.warn("Unable to parse the referred timestamp: " + dateStr + ". Proceeding with current timestamp as default",e);
									}

									metricToSLAM.set(SLAM_TIMESTAMP,new LongNode(timestamp));

									ObjectNode terms = JsonNodeFactory.instance.objectNode();
									terms.set(SLAM_METRIC, estimationFromEM.get(EM_VALUE));
									metricToSLAM.set(SLAM_TERMS, terms);

									submitEstimation(metricToSLAM);
								}
								//EventsDBMapper.INSTANCE.storeEvent(asEvent);
							} catch (Exception e) {
								Thread.sleep(3000);
								Logger.warn(e.getMessage(), e);
								if (running) {
									try {
										connectionFactory
												= (TopicConnectionFactory) context.lookup("asceticpaas");
										topic = (Topic) context.lookup("prediction");
										connection = connectionFactory.createTopicConnection();
										connection.start();
										session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
										clientTopic = session.createSubscriber(topic);
									} catch (Exception ex) {
										Logger.error("Error reconnecting from estimations reader", ex);
									}
								}
							}
						}
					} catch (Exception e) {
						Logger.error("Error initializing EM estimations reader: " + e.getMessage());
					}
				}
			});

			theThread.start();

			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					running = false;
				}
			}));

		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}
	
	private void submitEstimation(ObjectNode metricToSlam) throws JMSException, IOException, NamingException {
		String appId = metricToSlam.get(SLAM_APPID).asText();
		String deploymentId = metricToSlam.get(SLAM_DEPLOYMENTID).asText();

		String topicName = TOPIC_PREFIX + appId + "." + deploymentId  + TOPIC_SUFFIX;
		String topicKey = "topic." + appId + deploymentId;

		Properties p = new Properties();
		p.load(InitiateMonitoringDispatcher.class.getResourceAsStream("/jndi.properties"));
		p.load(InitiateMonitoringDispatcher.class.getResourceAsStream("/jndiEstimations.properties"));
		p.put(topicKey,topicName);

		final Context context = new InitialContext(p);
		TopicConnectionFactory connectionFactory
				= (TopicConnectionFactory) context.lookup("asceticpaas");
		TopicConnection connection = connectionFactory.createTopicConnection();
		connection.start();
		TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

		Logger.debug("Submitting estimation to topic: " + topicName);
		Logger.debug("Submitting next estimation: " + metricToSlam.toString());
		TextMessage estimationMessage = session.createTextMessage(metricToSlam.toString());
		final Topic topic = (Topic) context.lookup(appId+deploymentId);
		session.createProducer(topic).send(estimationMessage);
	}

	final static String getTopicKey(String appId, String deploymentId) {
		return "topic." + appId + "." + deploymentId;
	}

	final static String getTopicName(String appId, String deploymentId) {
		return TOPIC_PREFIX + appId + "." + deploymentId + TOPIC_SUFFIX;

	}

	private static final String EM_APPID = "applicationid";
	private static final String EM_DEPLOYMENTID = "deploymentid";
	private static final String EM_REFERREDTIMESTAMP = "referredtimestamp";
	private static final String EM_VALUE = "value";

	// {"ApplicationId":"SinusApp","Timestamp":1431592067367,"Terms":{"metric":9.862471417356321}}
	private static final String SLAM_APPID = "ApplicationId";
	private static final String SLAM_DEPLOYMENTID = "DeploymentId";
	private static final String SLAM_TIMESTAMP = "Timestamp";
	private static final String SLAM_TERMS = "Terms";
	private static final String SLAM_METRIC = "metric";

	private static final String TOPIC_PREFIX = "application-monitor.monitoring.";
	private static final String TOPIC_SUFFIX = ".estimation";
}
