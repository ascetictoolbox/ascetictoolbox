package eu.ascetic.saas.monitorcontroller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("UserInfo")
public class UserInfo {

	// @GET here defines, this method will method will process HTTP GET
	// requests.
	@GET
	// @Path here defines method level path. Identifies the URI path that a
	// resource class method will serve requests for.
	@Path("/name/{i}")
	// @Produces here defines the media type(s) that the methods
	// of a resource class can produce.
	@Produces(MediaType.TEXT_XML)
	// @PathParam injects the value of URI parameter that defined in @Path
	// expression, into the method.
	public String userName(@PathParam("i") String i) {

		String name = i;
		return "<User>" + "<Name>" + name + "</Name>" + "</User>";
	}

	@GET
	@Path("/age/{j}")
	@Produces(MediaType.TEXT_XML)
	public String userAge(@PathParam("j") int j) {

		int age = j;
		return "<User>" + "<Age>" + age + "</Age>" + "</User>";
	}
}
