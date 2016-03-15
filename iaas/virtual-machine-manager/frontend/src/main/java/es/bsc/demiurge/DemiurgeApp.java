package es.bsc.demiurge;

import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.ws.container.EmbeddedJetty;
import es.bsc.demiurge.ws.rest.DemiurgeRestV1;
import org.eclipse.jetty.rewrite.handler.RedirectPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
public class DemiurgeApp {
    public static void main(String[] args) {
        Config.INSTANCE.loadBeansConfig();
        try {
            EmbeddedJetty jetty = new EmbeddedJetty();
			jetty.startJetty(Config.INSTANCE.connectionPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
