package es.bsc.demiurge;

import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.ws.container.EmbeddedJetty;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
public class DemiurgeApp {
    public static void main(String[] args) throws InterruptedException {
        //System.out.println("Waiting initialization...");
        //Thread.sleep(20000);
        //System.out.println("Start initialization.");
        Config.INSTANCE.loadBeansConfig();
        try {
            EmbeddedJetty jetty = new EmbeddedJetty();
			jetty.startJetty(Config.INSTANCE.connectionPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
