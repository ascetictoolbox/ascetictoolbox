package es.bsc.vmmanagercore.rest;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * CORS (cross-origin resource sharing) support filter. I could not connect to the REST service from an
 * external application without applying this CORS filter. For more information about CORS check
 * http://enable-cors.org/
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class CorsSupportFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest req, ContainerResponse contResp) {
        ResponseBuilder resp = Response.fromResponse(contResp.getResponse());
        resp.header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        String reqHead = req.getHeaderValue("Access-Control-Request-Headers");

        if (null != reqHead && !reqHead.equals(null)){
            resp.header("Access-Control-Allow-Headers", reqHead);
        }

        contResp.setResponse(resp.build());
        return contResp;
    }

}