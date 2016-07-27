package es.bsc.paas.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
@Component
public class PaasEnergyModeller {

	Logger log = LoggerFactory.getLogger(PaasEnergyModeller.class);

	@Value("${application.manager.url}")
	private String applicationManagerUrl;

	RestTemplate rest = new RestTemplate();

	public double getEnergyEstimation(String applicationId, String deploymentId, long duration) {
		try {
			// GET /applications/{application_name}/deployments/{deployment_id}/energy-estimation?duration={duration_long}
			// TODO: uncomment this
			String estimationString = rest.getForObject(URI.create(applicationManagerUrl
					+ "/applications/" + applicationId
					+ "/deployments/" + deploymentId
					+ "/energy-estimation?duration=" + duration), String.class);
			log.trace(estimationString);

			String estimationValueStr = estimationString.substring(
					estimationString.indexOf("<value>") + 7,
					estimationString.indexOf("</value>"));

			log.trace("estimationVlueStr = " + estimationValueStr);
			return new Double(estimationValueStr.trim());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return -1;
	}
}
