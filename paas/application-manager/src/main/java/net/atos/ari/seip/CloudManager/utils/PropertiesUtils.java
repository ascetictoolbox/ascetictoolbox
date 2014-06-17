package net.atos.ari.seip.CloudManager.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;


public class PropertiesUtils {

	private static Logger log = Logger.getLogger(PropertiesUtils.class);

	private static final String CLOUD_SLM_CONFIG_FILE = "/etc/CloudManager.properties";
	private static final String LOG4J_CONFIG_FILE = "/etc/log4j.properties";

	private static final String CLOUD_SLM_CONFIG_FILE_WIN = "\\etc\\CloudManager.properties";
	private static final String LOG4J_CONFIG_FILE_WIN = "\\etc\\log4j.properties";

	public static String getProperty(String configFile, String property){
		PropertiesConfiguration configTrust = PropertiesUtils.getPropertiesConfiguration(configFile);
		return configTrust.getString(property);
	}
	
	public static PropertiesConfiguration getPropertiesConfiguration(
			String configFile) {
		String filePath = null;
		PropertiesConfiguration config = null;
		filePath = file4OS(configFile);
		try {
			config = new PropertiesConfiguration(filePath);
		} catch (ConfigurationException e) {
			log.error("TRUST: Error reading " + filePath + " configuration file: " + e.getMessage());
			e.printStackTrace();
		}
		return config;
	}

	public static String getConfigFilePath(String configFile) {
		String CLM_HOME = System.getProperty("CLM_HOME");
		log.info(CLM_HOME);
		if (CLM_HOME == null) {
			if (System.getProperty("file.separator").equalsIgnoreCase("/")) {
				CLM_HOME = "/opt/cloudmanager";
				log.debug("SLM_HOME: " + CLM_HOME + " (DEFAULT)");
			} else {
				CLM_HOME = "d:\\opt\\cloudmanager";
				log.debug("SLM_HOME: " + CLM_HOME + " (DEFAULT)");
			}
		} else {
			log.debug("TRUST: SLM_HOME: " + CLM_HOME);
		}

		File fileObject = new File(CLM_HOME.concat(configFile));
		if (!fileObject.exists()) {
			try {
				createDefaultConfigFile(fileObject);
			} catch (Exception ex) {
				log.error("Error reading "
						+ CLM_HOME.concat(configFile)
						+ " configuration file: " + ex.getMessage());
				ex.printStackTrace();
			}
		}

		return CLM_HOME.concat(configFile);
	}

	private static void createDefaultConfigFile(File fileObject)
			throws Exception {
		log.info("File " + fileObject.getAbsolutePath()
				+ " didn't exist. Creating one with default values...");

		// Create parent directories.
		log.info("Creating parent directories.");
		new File(fileObject.getParent()).mkdirs();

		// Create an empty file to copy the contents of the default file.
		log.info("Creating empty file.");
		new File(fileObject.getAbsolutePath()).createNewFile();

		// Copy file.
		log.info("Copying file " + fileObject.getName());
		InputStream streamIn = PropertiesUtils.class.getResourceAsStream("/"
				+ fileObject.getName());
		FileOutputStream streamOut = new FileOutputStream(
				fileObject.getAbsolutePath());
		byte[] buf = new byte[8192];
		while (true) {
			int length = streamIn.read(buf);
			if (length < 0) {
				break;
			}
			streamOut.write(buf, 0, length);
		}

		// Close streams after copying.
		try {
			streamIn.close();
		} catch (IOException ignore) {
			log.error("Couldn't close input stream");
		}
		try {
			streamOut.close();
		} catch (IOException ignore) {
			log.error("Couldn't close file output stream");
		}
	}

	private static String file4OS(String configFile) {
		if (configFile.equalsIgnoreCase("LOG")) {
			if (System.getProperty("file.separator").equalsIgnoreCase("/")) {
				return getConfigFilePath(LOG4J_CONFIG_FILE);
			} else {
				return getConfigFilePath(LOG4J_CONFIG_FILE_WIN);
			}
		} else {
			if (System.getProperty("file.separator").equalsIgnoreCase("/")) {
				return getConfigFilePath(CLOUD_SLM_CONFIG_FILE);
			} else {
				return getConfigFilePath(CLOUD_SLM_CONFIG_FILE_WIN);
			}
		}
	}

}
