package eu.ascetic.saas.experimentmanager.paasAPI;

import java.util.List;
import java.util.Map;

public interface InformationProvider {

	List<String> listApplications();

	List<String> listDeployments(String appId);

	List<String> listVirtualMachine(String appId, String deplId) throws Exception;
	
	public Map<String,String> listOfVM(String appId, String deplId) throws Exception;

	List<String> listEventIds(String appId, String deplId) throws Exception;
	
	
	public String getUrlToApplicationManager() ;

	public void setUrlToApplicationManager(String urlToApplicationManager) ;

	public String getUrlToApplicationMonitor() ;

	public void setUrlToApplicationMonitor(String urlToApplicationMonitor) ;
	
	
}