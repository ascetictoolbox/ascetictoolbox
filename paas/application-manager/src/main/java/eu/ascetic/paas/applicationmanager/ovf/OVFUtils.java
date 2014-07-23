package eu.ascetic.paas.applicationmanager.ovf;

import eu.ascetic.utils.ovf.api.OvfDefinition;

/**
 * Class that handles all the parsing of the OVF incoming from the different modules in the Application Manager
 * @author David Garcia Perez - Atos
 */
public class OVFUtils {

	/**
	 * Extracts the field ovf:Name from VirtualSystemCollection to differenciate between applications
	 * @param ovf String representing the OVF definition of an Application
	 * @return the application name
	 */
	public static String getApplicationName(String ovf) {
		OvfDefinition ovfDocument = OvfDefinition.Factory.newInstance(ovf);
		
		return ovfDocument.getVirtualSystemCollection().getName();
	}
}
