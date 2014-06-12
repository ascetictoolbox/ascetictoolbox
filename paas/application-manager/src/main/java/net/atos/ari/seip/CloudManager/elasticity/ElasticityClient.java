package net.atos.ari.seip.CloudManager.elasticity;

import javax.ws.rs.core.MediaType;

import net.atos.ari.seip.CloudManager.REST.utils.HTTPOperations;
import net.atos.ari.seip.CloudManager.REST.utils.Paths;
import net.atos.ari.seip.CloudManager.utils.ManifestUtils;
import eu.optimis.manifest.api.sp.Manifest;

public class ElasticityClient {
	
	String url;
	
	public ElasticityClient(String host, int port){
		url = "http://"+host+":"+port+Paths.ELASTICITY_SERVICE;
	}

	public void loadElasticityRules(String ServiceId, Manifest mani){
		String manifest = ManifestUtils.manifest2string(mani);
		String loadUrl = url + Paths.ELASTICITY_API + Paths.LOAD_RULES;
		HTTPOperations.doPOST(loadUrl, manifest,  MediaType.APPLICATION_XML,  MediaType.APPLICATION_XML, String.class);
	}
	
	public void deleteElasticityRules (String serviceId){
		String deleteUrl = url + Paths.ELASTICITY_API + Paths.DELETE_RULES + "/" + serviceId;  
		HTTPOperations.doGET(deleteUrl, MediaType.APPLICATION_XML, MediaType.APPLICATION_XML, String.class);
	}
}
