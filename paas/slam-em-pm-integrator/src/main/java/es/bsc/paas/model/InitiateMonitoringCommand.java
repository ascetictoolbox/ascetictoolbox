package es.bsc.paas.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
public class InitiateMonitoringCommand {
/*
{"ApplicationId":"JEPlus","DeploymentId":"893","Frequency":6000}
 */
	public static final String COMMAND_NAME = "initiateMonitoring";

	public static final String FIELD_APP_ID = "ApplicationId";
	public static final String FIELD_DEPLOYMENT_ID = "DeploymentId";
	public static final String FIELD_FREQUENCY = "Frequency";
	public static final String FIELD_SLA_ID = "slaId";

	private static final long DEFAULT_FREQUENCY = 5*60*1000;

	private String applicationId, deploymentId, slaId;
	private long frequency;

	private InitiateMonitoringCommand(String applicationId, String deploymentId, String slaId, long frequency) {
		this.applicationId = applicationId;
		this.deploymentId = deploymentId;
		this.slaId = slaId;
		this.frequency = frequency;
	}

	public static InitiateMonitoringCommand fromJson(String json) throws IOException {
		ObjectNode on = (ObjectNode) new ObjectMapper().readTree(json);
		return new InitiateMonitoringCommand(
				on.has(FIELD_APP_ID) ? on.get(FIELD_APP_ID).textValue() : null,
				on.has(FIELD_DEPLOYMENT_ID) ? on.get(FIELD_DEPLOYMENT_ID).textValue() : null,
				on.has(FIELD_SLA_ID) ? on.get(FIELD_SLA_ID).textValue() : null,
				on.has(FIELD_FREQUENCY) ? on.get(FIELD_FREQUENCY).longValue() : DEFAULT_FREQUENCY
		);
	}

	public String getApplicationId() {
		return applicationId;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public String getSlaId() {
		return slaId;
	}

	public long getFrequency() {
		return frequency;
	}
}
