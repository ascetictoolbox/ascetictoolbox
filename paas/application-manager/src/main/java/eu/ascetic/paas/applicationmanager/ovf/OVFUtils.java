package eu.ascetic.paas.applicationmanager.ovf;

import org.apache.log4j.Logger;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.utils.OvfRuntimeException;

/**
 * Class that handles all the parsing of the OVF incoming from the different modules in the Application Manager
 * @author David Garcia Perez - Atos
 */
public class OVFUtils {
	private static Logger logger = Logger.getLogger(OVFUtils.class);
			
	/**
	 * Extracts the field ovf:Name from VirtualSystemCollection to differenciate between applications
	 * @param ovf String representing the OVF definition of an Application
	 * @return the application name
	 */
	public static String getApplicationName(String ovf) {
		
		try {
			OvfDefinition ovfDocument = OvfDefinition.Factory.newInstance(ovf);
			return ovfDocument.getVirtualSystemCollection().getName();
		} catch(OvfRuntimeException ex) {
			logger.info("Error parsing OVF file: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}
}
