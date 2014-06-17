package eu.ascetic.application.manager.rest;

import javax.ws.rs.Path;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import eu.ascetic.application.manager.rest.service.ApplicationManagerServiceAbstractImpl;

@Path("/")
@Component
@Scope("request")
public class ApplicationManagerServiceRest extends ApplicationManagerServiceAbstractImpl {

}