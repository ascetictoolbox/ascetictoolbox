/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.iaas.slamanager.poc.manager.resource;

import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.ClientResponse;

public interface ResourceManager {

	public ClientResponse estimates(JSONObject request);

	public JSONObject verifyCommitResources(JSONObject request);

}
