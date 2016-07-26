package es.bsc.paas.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
@Component
public class PaasEnergyModeller {

	Logger log = LoggerFactory.getLogger(PaasEnergyModeller.class);

	@Value("${application.manager.url}")
	private String applicationManagerUrl;

	RestTemplate rest = new RestTemplate();

	private Random deleteThisProperty = new Random(System.currentTimeMillis());
	public double getEnergyEstimation(String applicationId, String deploymentId, long duration) {
		try {
			// GET /applications/{application_name}/deployments/{deployment_id}/energy-estimation?duration={duration_long}

			// TODO: uncomment this
//			String estimationString = rest.getForObject(URI.create(applicationManagerUrl
//					+ "/applications/" + applicationId
//					+ "/deployments/" + deploymentId
//					+ "/energy-estimation?duration=" + duration), String.class);
//			log.trace(estimationString);

			//TODO: parse estimation string and return the estimation value
			// by the moment, returning a Random
			return 20 * deleteThisProperty.nextDouble();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return -1;
	}
}
