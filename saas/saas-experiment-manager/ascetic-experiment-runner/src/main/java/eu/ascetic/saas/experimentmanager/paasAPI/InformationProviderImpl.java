package eu.ascetic.saas.experimentmanager.paasAPI;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.ascetic.saas.experimentmanager.wslayer.RESSOURCEFORMAT;
import eu.ascetic.saas.experimentmanager.wslayer.WSBasic;
import eu.ascetic.saas.experimentmanager.wslayer.exception.ResponseParsingException;
import eu.ascetic.saas.experimentmanager.wslayer.exception.WSException;

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
		String url = urlToApplicationManager+"/application-manager/applications/" + appId + "/deployments/" + deplId + "/vms";
		String query = "/collection/items/vm/id";
		return WSBasic.getList(url, RESSOURCEFORMAT.XML, query);
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.saas.experimentmanager.paasAPI.IPaaSInformationProvider#listEventIds(java.lang.String, java.lang.String)
	 */
	public List<String> listEventIds(String appId, String deplId) throws Exception {
		String url = urlToApplicationMonitor+"/query";
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
	

	@Override
	public Map<String, String> listOfVM(String appId, String deplId) throws Exception {
		String url = urlToApplicationManager+"/application-manager/applications/" + appId + "/deployments/" + deplId + "/vms";
		Client client = Client.create();
		WebResource webResource = client.resource(url);

		ClientResponse response;
		Logger.getLogger("WSBasic").info("Querying " + url + " ... ");
		response = webResource.accept("application/xml").get(ClientResponse.class);
		
		if (response.getStatus() != 200) {
			   throw new WSException(response.getStatus(),"Failed : HTTP error code for url "+url+" : "
				+ response.getStatus());
			}
		
		Logger.getLogger("WSBasic").info("Response : "+response.toString());

		Document root = null;
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			root = db.parse(response.getEntity(InputStream.class));
		} catch (Exception e) {
			throw new WSException(-1,"Bad response",e);
		}
		
		try {
			XPath xPath =  XPathFactory.newInstance().newXPath();
			NodeList resp = (NodeList) xPath.compile("/collection/items/vm").evaluate(root, XPathConstants.NODESET);
			
			Map<String,String> result = new HashMap<>();
			for(int i=0;i<resp.getLength();++i){
				Node n = resp.item(i);
				// id is located in /id and name is located in /ovf-id
				String id = (String) xPath.compile("id/text()").evaluate(n, XPathConstants.STRING);
				String name = (String) xPath.compile("ovf-id/text()").evaluate(n, XPathConstants.STRING);
				if(id!=null&&name!=null){
					result.put(id, name);
				}
			}
			
			return result;
		} catch (Exception e) {
			throw new ResponseParsingException(e);
		} 
	}
	
}
