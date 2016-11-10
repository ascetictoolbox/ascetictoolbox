package es.bsc.paas.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * Client class to retrieve energy estimations from the PaaS energy modeller
 * @author Mario Macias (http://github.com/mariomac)
 */
@Component
public class PaasEnergyModeller {

    Logger log = LoggerFactory.getLogger(PaasEnergyModeller.class);

    @Value("${application.manager.url}")
    private String applicationManagerUrl;

    RestTemplate rest = new RestTemplate();
    
    private double valueFromXml(String response){
        log.trace(response);
        String doubleStr = response.substring(
            response.indexOf("<value>") + 7, 
            response.indexOf("</value>")
        );

        //log.trace("douvleValueStr = " + doubleStr);
        return Double.parseDouble(doubleStr.trim());
    }
    
    private double chargesFromXml(String response){
        log.trace(response);
        String doubleStr = response.substring(
            response.indexOf("<charges>") + 9, 
            response.indexOf("</charges>")
        );

        //log.trace("douvleValueStr = " + doubleStr);
        return Double.parseDouble(doubleStr.trim());
    }

    public double getEnergyEstimation(String applicationId, String deploymentId, long duration) {
        String energyEstimation = rest.getForObject(URI.create(applicationManagerUrl
            + "/applications/" + applicationId
            + "/deployments/" + deploymentId
            + "/energy-estimation?duration=" + duration), String.class);
        return valueFromXml(energyEstimation);
    }

    public double getEnergyConsumption(String applicationId, String deploymentId) {
        String energyConsumption = rest.getForObject(URI.create(applicationManagerUrl
            + "/applications/" + applicationId
            + "/deployments/" + deploymentId
            + "/energy-consumption"), String.class);
        return valueFromXml(energyConsumption);
    }
    
    public double getPowerEstimation(String applicationId, String deploymentId, long duration) {
        String powerEstimation = rest.getForObject(URI.create(applicationManagerUrl
            + "/applications/" + applicationId
            + "/deployments/" + deploymentId
            + "/power-estimation?duration=" + duration), String.class);
        return valueFromXml(powerEstimation);
    }

    public double getPowerConsumption(String applicationId, String deploymentId) {
        String powerConsumption = rest.getForObject(URI.create(applicationManagerUrl
            + "/applications/" + applicationId
            + "/deployments/" + deploymentId
            + "/power-consumption"), String.class);
        return valueFromXml(powerConsumption);
    }
    
    public double getPriceEstimation(String applicationId, String deploymentId) {
        String pricePrediction = rest.getForObject(URI.create(applicationManagerUrl
            + "/applications/" + applicationId
            + "/deployments/" + deploymentId
            + "/predict-price-next-hour"), String.class);
        return chargesFromXml(pricePrediction);
    }
}