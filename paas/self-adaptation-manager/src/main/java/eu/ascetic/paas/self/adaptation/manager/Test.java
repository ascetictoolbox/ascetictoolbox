package eu.ascetic.paas.self.adaptation.manager;

import eu.ascetic.paas.self.adaptation.manager.activemq.actuator.ActionRequester;
import eu.ascetic.paas.self.adaptation.manager.activemq.listener.SlaManagerListener;

/**
 * Hello world!
 */
@Deprecated
public class Test {

    public static void main(String[] args) throws Exception {
        startThread(new ActionRequester(), false);
        startThread(new ActionRequester(), false);
        startThread(new SlaManagerListener(), false);
        Thread.sleep(1000);
        startThread(new SlaManagerListener(), false);
        startThread(new ActionRequester(), false);
        startThread(new SlaManagerListener(), false);
        startThread(new ActionRequester(), false);
        Thread.sleep(1000);
        startThread(new SlaManagerListener(), false);
        startThread(new ActionRequester(), false);
        startThread(new SlaManagerListener(), false);
        startThread(new SlaManagerListener(), false);
        startThread(new ActionRequester(), false);
        startThread(new ActionRequester(), false);
        Thread.sleep(1000);
        startThread(new ActionRequester(), false);
        startThread(new SlaManagerListener(), false);
        startThread(new SlaManagerListener(), false);
        startThread(new ActionRequester(), false);
        startThread(new SlaManagerListener(), false);
        startThread(new ActionRequester(), false);
        startThread(new SlaManagerListener(), false);
        startThread(new ActionRequester(), false);
        startThread(new SlaManagerListener(), false);
        startThread(new SlaManagerListener(), false);
        startThread(new ActionRequester(), false);
    }

    /**
     * This starts a classes that implements the runnable interface as a thread.
     * @param runnable The runnable instance to start
     * @param daemon If it should be started as a daemon thread or not
     */
    public static void startThread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

}
