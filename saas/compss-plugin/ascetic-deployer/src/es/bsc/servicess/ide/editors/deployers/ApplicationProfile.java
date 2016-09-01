package es.bsc.servicess.ide.editors.deployers;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class ApplicationProfile {
	
	public static String DEFAULT_PROFILE = "100@0@0";
	public static String DEFAULT_METRICS="1@1";
	public static int DEFAULT_WEIGHT = 1;
	public static String WEIGHT = "@Weight";
	private PropertiesConfiguration profile;

	public ApplicationProfile(String pathToConfigFile)
			throws ConfigurationException {
		profile = new PropertiesConfiguration(pathToConfigFile);
	}

	public ApplicationProfile(File file) throws ConfigurationException {
		profile = new PropertiesConfiguration(file);
	}
	
	public String getImplementationProfile(String pack, String core){
		return profile.getString(core+"@"+pack, DEFAULT_PROFILE);
	}
	
	public int getWeight(String core){
		return profile.getInt(core+"@Weight", DEFAULT_WEIGHT);
	}
	
	public String getDefaultMetrics(String pack){
		return profile.getString(pack+"@Metrics", DEFAULT_METRICS);
	}
	
	
}
