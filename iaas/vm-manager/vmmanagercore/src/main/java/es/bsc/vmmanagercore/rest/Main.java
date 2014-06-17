package es.bsc.vmmanagercore.rest;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import es.bsc.vmmanagercore.manager.VmManagerConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class Main {

    public static final String BASE_URI = VmManagerConfiguration.getInstance().deployBaseUrl;
    public static final String DEPLOY_PACKAGE = VmManagerConfiguration.getInstance().deployPackage;
    public static final String STOP_MESSAGE = "Press any key to stop the server...";

    @SuppressWarnings("unchecked")
    public static HttpServer startServer() {
        final ResourceConfig rc = new PackagesResourceConfig(DEPLOY_PACKAGE);
        rc.getContainerResponseFilters().add(CorsSupportFilter.class);
        try {
            return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void logSystemStart() {
        BasicConfigurator.configure();
        Logger logger = LoggerFactory.getLogger(Main.class);
        logger.debug("Starting server");
    }

    public static void main(String[] args) throws IOException {
        logSystemStart();
        final HttpServer server = startServer();
        server.start();
        System.out.println(STOP_MESSAGE);
        System.in.read();
    }
    
}