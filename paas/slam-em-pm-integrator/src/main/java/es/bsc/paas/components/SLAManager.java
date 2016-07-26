package es.bsc.paas.components;

import org.springframework.stereotype.Component;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
@Component
public class SLAManager {

	private static final String QUEUE_TEMPLATE = "application-monitor.monitoring.%s.%s.estimation";

	public void reportEnergyEstimation(String applicationId, String deploymentId, long referredtimestamp) {
		String queueName = String.format(QUEUE_TEMPLATE,applicationId,deploymentId);

		//{"ApplicationId":"maximTestApp","DeploymentId":"938","Timestamp":1234123411243,"data":{"energyEstimation":48.36}}
		// timestamp: somewhere in the future, taken from the "referredtimestamp"

	}
}
