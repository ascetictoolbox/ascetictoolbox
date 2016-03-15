package es.bsc.demiurge.ws.container;

import es.bsc.demiurge.core.configuration.Config;
import org.apache.activemq.transport.discovery.http.EmbeddedJettyServer;
import org.eclipse.jetty.rewrite.handler.RedirectPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.DispatcherType;
import java.io.IOException;
import java.util.EnumSet;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
public class EmbeddedJetty {


    private static final String GUI_MAPPING_URL = "/gui/*"; // todo: combine with "/gui/*"
    private static final String API_MAPPING_URL = "/api/*";


    public void startJetty(int port) throws Exception {
		// prepare server
		final Server server;
		if(Config.INSTANCE.disableSecurity) {
			server = new Server(port);
		} else {
			// TODO: allow configuration-defined REAL certificates
			server = new Server();
			HttpConfiguration https = new HttpConfiguration();
			https.addCustomizer(new SecureRequestCustomizer());
			SslContextFactory sslContextFactory = new SslContextFactory();
			sslContextFactory.setKeyStorePath(EmbeddedJetty.class.getResource(
					"/keystore").toExternalForm());
			sslContextFactory.setKeyStorePassword("testtest");
			sslContextFactory.setKeyManagerPassword("test");
			ServerConnector sslConnector = new ServerConnector(server,
					new SslConnectionFactory(sslContextFactory, "http/1.1"),
					new HttpConnectionFactory(https));
			sslConnector.setPort(port);
			server.setConnectors(new Connector[] { sslConnector });
		}

		HandlerCollection handlerCollection = new HandlerCollection(false);

		// Rewrite handler to avoid error when URL ends with /gui instead of /gui/
		RewriteHandler rewriteHandler = new RewriteHandler();
		RedirectPatternRule rpr = new RedirectPatternRule();
		rpr.setPattern("/gui");
		rpr.setLocation("/gui/");
		rewriteHandler.addRule(rpr);
		handlerCollection.addHandler(rewriteHandler);

		if(!Config.INSTANCE.disableSecurity) {
			// Specify the Session ID Manager
			HashSessionIdManager idmanager = new HashSessionIdManager();
			server.setSessionIdManager(idmanager);
		}

		// Spring GUI and API servlet context handler
		handlerCollection.addHandler(getServletContextHandler(handlerCollection));

		server.setHandler(handlerCollection);
		server.start();
        server.join();


        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Stopping jetty");
                try {
                    server.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                server.destroy();
            }
        }));
    }

    private static ServletContextHandler getServletContextHandler(Handler handlerCollection) throws IOException {
		AnnotationConfigWebApplicationContext springContext = new AnnotationConfigWebApplicationContext();
		springContext.setConfigLocation("es.bsc.demiurge.ws");

		ServletContextHandler jettyServletContext = new ServletContextHandler();
        jettyServletContext.setErrorHandler(null);
        jettyServletContext.setContextPath(Config.INSTANCE.contextPath);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(springContext);
		ServletHolder guiServletHolder = new ServletHolder(dispatcherServlet);
        jettyServletContext.addServlet(guiServletHolder, GUI_MAPPING_URL);

        ServletHolder apiServletContainer = new ServletHolder(new ServletContainer(new ApiConfig()));
        jettyServletContext.addServlet(apiServletContainer, API_MAPPING_URL);
        jettyServletContext.addEventListener(new ContextLoaderListener(springContext));

		if(!Config.INSTANCE.disableSecurity) {
			// GUI SECURITY
			{
				XmlWebApplicationContext ctx = new XmlWebApplicationContext();
				ctx.setConfigLocation("classpath:/gui-security-config.xml");
				FilterHolder fh = new FilterHolder(new DelegatingFilterProxy("springSecurityFilterChain",ctx));
				jettyServletContext.addFilter(fh,"/gui/*",EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));
			}
			// API SECURITY
			{
				XmlWebApplicationContext ctx = new XmlWebApplicationContext();
				ctx.setConfigLocation("classpath:/api-security-config.xml");
				FilterHolder fh = new FilterHolder(new DelegatingFilterProxy("springSecurityFilterChain",ctx));
				jettyServletContext.addFilter(fh,"/api/*",EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));
			}
			// Create the SessionHandler (wrapper) to handle the sessions
			HashSessionManager manager = new HashSessionManager();
			SessionHandler sessions = new SessionHandler(manager);
			jettyServletContext.setHandler(sessions);

		}
        return jettyServletContext;
    }

}
