package es.bsc.amon.mq.dispatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.bsc.amon.controller.EventsDBMapper;
import es.bsc.amon.controller.QueriesDBMapper;
import es.bsc.amon.mq.MQManager;
import es.bsc.amon.mq.dispatch.aggregations.Function;
import es.bsc.amon.mq.dispatch.aggregations.PercentileFunction;
import es.bsc.amon.mq.notif.PeriodicNotificationException;
import es.bsc.amon.mq.notif.PeriodicNotifier;
import play.Logger;

import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.*;
import java.util.regex.Pattern;

class AppMeasuresNotifier implements PeriodicNotifier {
	final String appId;
	final String deploymentId;
	final String slaId;
	final String[] terms;

	// By the moment, only percentiles will be stored here
	// since they are the only that mongodb doesn't compute
	// by itself
	final Map<String,Function> aggregateFunctions;
	final long frequency;
	final long period;
	String queryHead, queryTail;

	final String topicName;

	final MessageProducer producer;
	final Session session;
	static Pattern aggregateFunctionCall = Pattern.compile("[A-Za-z\\d]*\\(([A-Za-z\\d]+)(,[A-Za-z\\d]+)*\\)");
	static Pattern aggregateParts = Pattern.compile("[\\(,\\)]");

	public AppMeasuresNotifier(Session session, String appId, String deploymentId, String slaId, String[] terms, long frequency, long period) throws PeriodicNotificationException {
		removeOn = System.currentTimeMillis() + AUTO_REMOVAL_TIME;
		try {
			this.session = session;
			this.appId = appId;
			this.deploymentId = deploymentId;
			this.slaId = slaId;
			this.terms = terms;
			this.frequency = frequency;
			this.period = period;
			topicName = TOPIC_PREFIX + appId + TOPIC_SUFFIX;

			String topicKey = "topic." + appId;

			Properties p = new Properties();
			p.load(InitiateMonitoringDispatcher.class.getResourceAsStream("/jndi.properties"));
			p.put(topicKey, topicName);
			final Context context = new InitialContext(p);
			context.addToEnvironment(topicKey, topicName);
			final Topic topic = (Topic) context.lookup(appId);
			producer = session.createProducer(topic);

			StringBuilder sb = new StringBuilder("FROM ").append(EventsDBMapper.COLL_NAME).append(" MATCH ").append('\n');
			if(appId != null) {
				sb.append(EventsDBMapper.APPID).append(" = '").append(appId).append("' ").append('\n');
				if(deploymentId != null) {
					sb.append("AND ");
				}
			}
			if(deploymentId != null) {
				sb.append(EventsDBMapper.DEPLOYMENT_ID).append(" = '").append(deploymentId).append("' ");
			}
			queryHead = sb.toString();

			sb = new StringBuilder(" GROUP BY NOTHING"); //.append(EventsDBMapper.TIMESTAMP).append(" - ").append(EventsDBMapper.TIMESTAMP).append(" % ").append(getFrequency());

			Map<String,Function> aggregateFunctions = new HashMap<>();
			// AVERAGE OF ALL TERMS: TODO: consider specifying other aggregators: sum, max, min...
			for(int i = 0 ; i < terms.length ; i++ ) {
				String term = terms[i].trim();
				if(aggregateFunctionCall.matcher(term).matches()) {
					String[] parts = aggregateParts.split(term);
					String aggregateFunctionName = parts[0];
					String metric = parts[1];
					try {
						switch(aggregateFunctionName) {
							// percentile(metric,percent)
							case "percentile": {
								String reportedName = "percentile_" + metric + "_" + parts[2];
								Function function = new PercentileFunction(new Integer(parts[2]));
								aggregateFunctions.put(reportedName,function);
								sb.append( " push(data.").append(metric).append(") as ").append(reportedName);
								} break;
							// max(metric)
							case "max":
								sb.append(" max(data.").append(metric).append(") as ").append( "max_"+metric );
								break;
							// last(metric)
							case "last":
								sb.append(" last(data.").append(metric).append(") as ").append( "last_"+metric );
								break;
							default:
								throw new Exception("Unsuported function name: "+aggregateFunctionName);
						}
						// todo: share arrays between aggregate functions that use the same metric
					} catch(Exception e) {
						throw new Exception("The syntax of the aggregate function seems invalid",e);
					}
				} else {
					sb.append(" avg(data.").append(term).append(") as ").append(term);
				}
			}
			this.aggregateFunctions = Collections.unmodifiableMap(aggregateFunctions);
			queryTail = sb.toString();

		} catch (Exception e) {
			Logger.error(e.getMessage(),e);
			throw new PeriodicNotificationException("Error instantiating App Measures Notifier: " + e.getMessage(),e);
		}

	}


	@Override
	public long getFrequency() {
		return frequency;
	}

	// TODO --> substitute AUTO-REMOVAL (initally 24h) BY
	//				1 - Subscribte to App Manager events (on undeployment)
	//				2 - Remove after X minutes/without new metrics
	private static final long AUTO_REMOVAL_TIME = 24 * 60 * 60 * 1000;
	private long removeOn;
	@Override
	public void sendNotification() throws PeriodicNotificationException {
		long now = System.currentTimeMillis();
		if(now >= removeOn) {
			MQManager.INSTANCE.removeNotifier(this);
			Logger.debug("Asking for AUTO-REMOVAL for notifier: " + toString());
			return;
		}
		try {
			long from = period > 0 ? period : frequency;
			StringBuilder sb = new StringBuilder(queryHead).append('\n')
					.append(" AND ").append(EventsDBMapper.TIMESTAMP).append(" > ").append(now - from).append('\n')
					.append(" AND ").append(EventsDBMapper.TIMESTAMP).append(" <= ").append(now).append(queryTail);
							//EventsDBMapper
			String query = sb.toString();
//			Logger.debug("Sending query to aggregation framework:\n" + query);
			ArrayNode an = QueriesDBMapper.INSTANCE.aggregate(query);
//			Logger.info("Received: " + an.toString());

			if(an == null || an.size() == 0) {
//				Logger.debug("Response is null or 0");
			} if(an != null && an.size() > 0) {
				for(JsonNode jn : an) {
					ObjectNode response = JsonNodeFactory.instance.objectNode();
					if(appId != null) {
						response.put(InitiateMonitoringDispatcher.FIELD_APP_ID, appId);
					}
					if(deploymentId != null) {
						response.put(InitiateMonitoringDispatcher.FIELD_DEPLOYMENT_ID, deploymentId);
					}
					if(slaId != null) {
						response.put(InitiateMonitoringDispatcher.FIELD_SLA_ID, slaId);
					}
					response.put("Timestamp", System.currentTimeMillis());
					// TO DO: optimize
					ObjectNode termsON = JsonNodeFactory.instance.objectNode();
					Iterator<String> fields = jn.fieldNames();
					while(fields.hasNext()) {
						String t = fields.next();
						if(aggregateFunctions.containsKey(t) && jn.get(t).getNodeType() == JsonNodeType.ARRAY) {
							ArrayNode arrayNode = (ArrayNode) jn.get(t);
							if(arrayNode.size() > 0) {
								double[] numbers = new double[arrayNode.size()];
								int idx = 0;
								for(JsonNode item : arrayNode) {
									numbers[idx++] = item.asDouble();
								}
								termsON.put(t,aggregateFunctions.get(t).calculate(numbers));
							}
						} else {
							termsON.put(t, jn.get(t));
						}
					}

					response.set(InitiateMonitoringDispatcher.FIELD_TERMS, termsON);

					String responseStr = response.toString();
					//Logger.debug("Sending periodic notification: " + responseStr);

					TextMessage responseMessage = session.createTextMessage(responseStr);
					producer.send(responseMessage);				}
				}
		} catch(Exception e) {
			throw new PeriodicNotificationException("Error sending notification: " + e.getMessage(), e);
		}
	}

	@Override
	public String toString() {
		return "AppMeasuresNotifier{" +
				"appId='" + appId + '\'' +
				", deploymentId='" + deploymentId + '\'' +
				", slaId='" + slaId + '\'' +
				", terms=" + Arrays.toString(terms) +
				", frequency=" + frequency +
				", queryHead='" + queryHead + '\'' +
				", queryTail='" + queryTail + '\'' +
				", topicName='" + topicName + '\'' +
				", removeOn=" + removeOn +
				'}';
	}

	private static final String TOPIC_PREFIX = "application-monitor.monitoring.";
	private static final String TOPIC_SUFFIX = ".measurement";
}
