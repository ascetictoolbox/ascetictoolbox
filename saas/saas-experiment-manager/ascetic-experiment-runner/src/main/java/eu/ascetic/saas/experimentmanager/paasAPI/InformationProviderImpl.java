package eu.ascetic.saas.experimentmanager.paasAPI;

import java.util.List;

import eu.ascetic.saas.experimentmanager.wslayer.WSBasic;
import eu.ascetic.saas.experimentmanager.wslayer.RESSOURCEFORMAT;

public class InformationProviderImpl implements InformationProvider {
	
	private String urlToApplicationManager;
	private String urlToApplicationMonitor;
	
	
	public InformationProviderImpl(String urlToApplicationManager, String urlToApplicationMonitor){
		this.setUrlToApplicationManager(urlToApplicationManager);
		this.setUrlToApplicationMonitor(urlToApplicationMonitor);
	}
	
	/* (non-Javadoc)
	 * @see eu.ascetic.saas.experimentmanager.paasAPI.IPaaSInformationProvider#listApplications()
	 */
	public List<String> listApplications(){
		return null;
	}
	
	/* (non-Javadoc)
	 * @see eu.ascetic.saas.experimentmanager.paasAPI.IPaaSInformationProvider#listDeployments(java.lang.String)
	 */
	public List<String> listDeployments(String appId){
		return null;
	}
	
	/* (non-Javadoc)
	 * @see eu.ascetic.saas.experimentmanager.paasAPI.IPaaSInformationProvider#listVirtualMachine(java.lang.String, java.lang.String)
	 */
	public List<String> listVirtualMachine(String appId, String deplId) throws Exception{
		String url = "http://192.168.3.222/application-manager/applications/" + appId + "/deployments/" + deplId + "/vms";
		String query = "/collection/items/vm/id";
		return WSBasic.getList(url, RESSOURCEFORMAT.XML, query);
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.saas.experimentmanager.paasAPI.IPaaSInformationProvider#listEventIds(java.lang.String, java.lang.String)
	 */
	public List<String> listEventIds(String appId, String deplId) throws Exception {
		String url = "http://192.168.3.222:9000/query";
		String post = "FROM events MATCH appId = \"" + appId + "\" AND deploymentId = \"" + deplId + "\", GROUP BY data.eventType , avg(data.duration) as dur";
		String query = "$.._id";
		return WSBasic.getList(url, RESSOURCEFORMAT.JSON, query, post);
	}

	public String getUrlToApplicationManager() {
		return urlToApplicationManager;
	}

	public void setUrlToApplicationManager(String urlToApplicationManager) {
		this.urlToApplicationManager = urlToApplicationManager;
	}

	public String getUrlToApplicationMonitor() {
		return urlToApplicationMonitor;
	}

	public void setUrlToApplicationMonitor(String urlToApplicationMonitor) {
		this.urlToApplicationMonitor = urlToApplicationMonitor;
	}
	
	

	
}
