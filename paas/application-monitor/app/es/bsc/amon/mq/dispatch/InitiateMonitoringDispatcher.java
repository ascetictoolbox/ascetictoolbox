package es.bsc.amon.mq.dispatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.bsc.amon.mq.CommandDispatcher;
import es.bsc.amon.mq.MQManager;
import es.bsc.amon.mq.SessionHolder;
import play.Logger;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.*;

public class InitiateMonitoringDispatcher implements CommandDispatcher {

	public static final String COMMAND_NAME = "initiateMonitoring";

	public static final String FIELD_APP_ID = "ApplicationId";
	public static final String FIELD_DEPLOYMENT_ID = "DeploymentId";
	public static final String FIELD_TERMS = "Terms";
	public static final String FIELD_FREQUENCY = "Frequency";
	public static final String FIELD_PERIOD = "Period";
	public static final String FIELD_SLA_ID = "SlaId";
	public static final String FIELD_SLA_ID_LOWER = "slaId";

	private static final long DEFAULT_FREQUENCY = 5*60*1000;


	private SessionHolder sessionHolder;

	public InitiateMonitoringDispatcher(SessionHolder sessionHolder) {
		this.sessionHolder = sessionHolder;
	}

	private static String getString(ObjectNode n, String field) {
		JsonNode jn = n.get(field);
		return jn == null ? null : jn.textValue();
	}

	/*
	 {
	 	"ApplicationId" : "...",
	 	"DeploymentId" : "...",
	 	"slaId" : "...",
	 	"Frequency" : 1234
	 	"Terms" : [ "a_term", "other_term", "percentile('some_term',90)"]
	 }
	 In the terms, the default behaviour is to return the average.
	 If the "percentile" function is specified, it will return the percentile

	 */

	@Override
	public void onCommand(ObjectNode msgBody) {
		try {
			String appId = getString(msgBody,FIELD_APP_ID);
			if(appId == null) {
				Exception ife = new IllegalArgumentException("Application ID cannot be null");
				throw ife;
			}
			String deploymentId = getString(msgBody, FIELD_DEPLOYMENT_ID);
			String slaId = getString(msgBody, FIELD_SLA_ID);
			if(slaId == null || "".equals(slaId.trim())) {
				slaId = getString(msgBody,FIELD_SLA_ID_LOWER);
			}

			JsonNode termsJson = msgBody.get(FIELD_TERMS);
			List<String> terms = new ArrayList<String>();

			if(termsJson != null && termsJson.isTextual()) {
				terms.add(termsJson.textValue());
			} else if(termsJson != null && termsJson.isArray()) {
				for(JsonNode jn :(termsJson)) {
					if(jn.isTextual()) {
						terms.add(jn.textValue());
					}
				}
			}

			if(termsJson == null || terms.size() == 0) {
				throw new IllegalArgumentException("There are no valid SLA terms: " + termsJson);
			}

			JsonNode freqJson = msgBody.get(FIELD_FREQUENCY);
			long frequency = freqJson == null ? DEFAULT_FREQUENCY : freqJson.asLong(DEFAULT_FREQUENCY);
			JsonNode periodJson = msgBody.get(FIELD_PERIOD);
			long period = periodJson == null ? -1 : periodJson.asLong(-1);

			AppMeasuresNotifier amn = new AppMeasuresNotifier(sessionHolder,appId,deploymentId, slaId, terms.toArray(new String[terms.size()]),frequency, period);

			MQManager.INSTANCE.addPeriodicNotifier(amn);
		} catch(IllegalArgumentException e ) {
			Logger.debug("Bad command format: " + e.getMessage());
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}
}
