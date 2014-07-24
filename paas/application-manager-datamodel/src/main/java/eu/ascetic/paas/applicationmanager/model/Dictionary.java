package eu.ascetic.paas.applicationmanager.model;

/**
 * Collection of variables to maintain equal through the entire application
 * @author David Garcia Perez - Atos
 */
public class Dictionary {
	public static final String APPLICATION_MANAGER_NAMESPACE = "http://application_manager.ascetic.eu/doc/schemas/xml";
	
	public static String APPLICATION_STATUS_SUBMITTED = "SUBMITTED";
	public static String APPLICATION_STATUS_NEGOTIATION = "NEGOTIATION";
	public static String APPLICATION_STATUS_NEGOTIATIED = "NEGOTIATED";
	public static String APPLICATION_STATUS_CONTEXTUALIZATION = "CONTEXTUALIZATION";
	public static String APPLICATION_STATUS_CONTEXTUALIZED = "CONTEXTUALIZED";
	public static String APPLICATION_STATUS_DEPLOYING = "DEPLOYING";
	public static String APPLICATION_STATUS_DEPLOYED = "DEPLOYED";
	public static String APPLICATION_STATUS_TERMINATED = "TERMINATED";
	public static String APPLICATION_STATUS_ERROR = "ERROR";
	
	/** The Constant CONTENT_TYPE_JSON. */
	public static final String CONTENT_TYPE_JSON = "application/json";
	
	/** The Constant USER_AGENT. */
	public static final String USER_AGENT = "ASCETiC-ApplicationManager/0.0.1";
	
	/** The Constant ACCEPT. */
	public static final String ACCEPT = "*/*";
}
