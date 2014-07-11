package eu.ascetic.paas.applicationmanager.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * Common methods for all the rest APIs
 * @author David Garcia Perez - Atos
 *
 */
public abstract class AbstractRest {
	
	protected Response buildResponse(Response.Status status, String payload) {
		ResponseBuilder builder = Response.status(status);
		builder.entity(payload);
		return builder.build();
	}
}
