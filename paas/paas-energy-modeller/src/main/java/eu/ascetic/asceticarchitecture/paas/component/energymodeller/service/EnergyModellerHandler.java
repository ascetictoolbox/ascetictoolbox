package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EMSettings;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModellerExternal;

/**
 * @author davide sommacampagna
 *
 */

public class EnergyModellerHandler implements PaaSEnergyModellerExternal {

	/**
	 * property file from which EM will load the settings
	 */
	private final static String propertyFile = "config.properties";
	/**
	 * the class representing the settings
	 */
	private EMSettings emsettings;

	private final static Logger LOGGER = Logger.getLogger(EnergyModellerHandler.class.getName());

	/**
	 * constructor, load from the configuration file the settings
	 */
	public EnergyModellerHandler() {
		
	}
	

	@Override
	public boolean initialize(){
		LOGGER.info("Initializing component");
		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream(propertyFile);
		if (inputStream == null) {
			LOGGER.warn("Properties not loaded, using default values");
			return true;
		}
		Properties props = new Properties();
		try {
			props.load(inputStream);
			emsettings = new EMSettings(props);
			LOGGER.info("Properties loaded");
			
			return true;
		} catch (IOException e) {
			LOGGER.error("Properties not loaded due to a failure");
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean startModellingApplicationEnergy(String providerid,
			String applicationid, String deploymentid) {
		LOGGER.info("Starting to monitor application " + applicationid
				+ " in the current deployment " + deploymentid);
		return true;
	}

	@Override
	public boolean stopModellingApplicationEnergy(String providerid,
			String applicationid, String deploymentid) {
		return true;
	}

	@Override
	public String energyApplicationConsumption(String providerid,
			String applicationid, String deploymentid) {
		return "120";
	}

	@Override
	public String energyEstimation(String providerid, String applicationid,
			String deploymentid, String eventid) {
		return "120";
	}

	@Override
	public boolean trainApplication(String providerid, String applicationid,
			String deploymentid, String eventid) {
		return true;
	}

	/**
	 * @return the emsettings of the Energy Modeller. It allows to get the current settings loaded from file
	 */
	public EMSettings getEmsettings() {
		return emsettings;
	}

	/**
	 * @param emsettings allows to inject settings in to the PaaS Energy Modeller
	 */
	public void setEmsettings(EMSettings emsettings) {
		this.emsettings = emsettings;
	}

}
