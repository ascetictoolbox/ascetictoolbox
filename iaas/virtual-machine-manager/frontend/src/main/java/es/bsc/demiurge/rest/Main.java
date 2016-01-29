/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.rest;

import es.bsc.demiurge.core.configuration.Config;
import org.eclipse.jetty.rewrite.handler.RedirectPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * 
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Config.INSTANCE.loadBeansConfig();

		// Configure jersey servlet for rest services
		ServletContextHandler restContexthandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		restContexthandler.setContextPath("/api");

		ServletHolder jerseyServlet = restContexthandler.addServlet(
				org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		jerseyServlet.setInitOrder(0);

		// Tells the Jersey Servlet which REST service/class to load.
//		jerseyServlet.setInitParameter("jersey.config.server.provider.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
		jerseyServlet.setInitParameter(
				"jersey.config.server.provider.classnames",DemiurgeRestV1.class.getCanonicalName());

		// Tells the Jersey Servlet which REST service/class to load.

		// Configure static assets
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setWelcomeFiles(new String[] {"index.html"});
		resourceHandler.setResourceBase(Main.class.getClassLoader().getResource("webapp").toExternalForm());
		resourceHandler.setDirectoriesListed(true);

		ContextHandler resourceCtxHandler = new ContextHandler("/gui");
		resourceCtxHandler.setHandler(resourceHandler);

		HandlerCollection handlerCollection = new HandlerCollection();
		handlerCollection.addHandler(restContexthandler);
		handlerCollection.addHandler(resourceCtxHandler);

		// Redirect from /gui to /gui/
		RewriteHandler rewriteHandler = new RewriteHandler();
		RedirectPatternRule rpr = new RedirectPatternRule();
		rpr.setPattern("/gui");
		rpr.setLocation("/gui/");
		rewriteHandler.addRule(rpr);

		handlerCollection.addHandler(rewriteHandler);


		final Server jettyServer = new Server(Config.INSTANCE.connectionPort);
		jettyServer.setHandler(handlerCollection);

		jettyServer.start();
		jettyServer.join();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("Stopping jetty");
				try {
					jettyServer.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
				jettyServer.destroy();
			}
		}));
	}

}