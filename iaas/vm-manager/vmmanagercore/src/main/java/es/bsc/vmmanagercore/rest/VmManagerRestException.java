package es.bsc.vmmanagercore.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


public class VmManagerRestException extends WebApplicationException {

	private static final long serialVersionUID = 1L;

	public VmManagerRestException(String message) {
		super(Response.status(Status.BAD_REQUEST)
				.entity(message)
				.type(MediaType.TEXT_PLAIN)
				.build());
	}

}
