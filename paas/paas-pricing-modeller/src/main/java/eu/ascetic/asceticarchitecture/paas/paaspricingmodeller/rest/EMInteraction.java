
package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


public class EMInteraction {



    public double getEnergyofVM(String applicationID, String deploymentID, String VMid, String startTime, String endTime){
    	RestVMClient client = new RestVMClient(applicationID, deploymentID);
    	double response = client.getEnergyConsumption(Double.class, VMid, null, startTime, endTime);
    	try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Double.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
         //   double deployment = (double) jaxbUnmarshaller.unmarshal(response);
         //   return deployment;
            return response;
        } catch (JAXBException ex) {
            Logger.getLogger(EMInteraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0.0;
    	
    }
    
    public double getPredictedEnergyofVM(String applicationID, String deploymentID, String VMid, String duration){
    	RestVMClient client = new RestVMClient(applicationID, deploymentID);
    	double response = client.getEnergyEstimation(Double.class, VMid, null, duration);
    	try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Double.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
         //   double deployment = (double) jaxbUnmarshaller.unmarshal(response);
         //   return deployment;
            return response;
        } catch (JAXBException ex) {
            Logger.getLogger(EMInteraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0.0;
    	
    }

}
