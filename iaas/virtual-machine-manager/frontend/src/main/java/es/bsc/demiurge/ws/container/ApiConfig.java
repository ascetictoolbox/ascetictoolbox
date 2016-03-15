package es.bsc.demiurge.ws.container;

import es.bsc.demiurge.ws.rest.DemiurgeRestV1;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
public class ApiConfig extends ResourceConfig {
    public ApiConfig() {
        register(DemiurgeRestV1.class);
    }
}
