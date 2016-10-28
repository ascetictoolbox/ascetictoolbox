package es.bsc.comm.nio;

import es.bsc.comm.CommException;
import es.bsc.comm.Connection;
import es.bsc.comm.EventManager;
import es.bsc.comm.MessageHandler;
import es.bsc.comm.Node;
import es.bsc.comm.nio.event.NIOEvent;

import java.util.LinkedList;


public class NIOEventManager extends EventManager<NIOEvent> {

    private static final long REESTABLISH_PERIOD = 30_000L;

    private NIOConnection closingConnection;
    private boolean nioListenerOn = false;
    private long lastReestablish;


    public NIOEventManager(MessageHandler receiver) {
        super(receiver);
        lastReestablish = System.currentTimeMillis();
    }

    @Override
    public void init(String propertiesFile) throws CommException {
        // Read the config file
        NIOProperties.importProperties(propertiesFile);

        // NIO init must be executed before starting the NIO thread
        NIOListener.init(this);
    }

    // Start a new NIO server
    @Override
    public void startServer(Node n) throws CommException {
        NIOListener.startServer(n);
    }

    @Override
    public void run() {
        // Start the NIO thread
        (new NIOListener()).start();
        nioListenerOn = true;

        // Start the MessageHandler
        super.run();
    }

    @Override
    public Connection startConnection(Node n) {
        return NIOListener.startConnection(n);
    }

    @Override
    public void shutdown(Connection c) {
        LOGGER.debug("Shutting down the communication platform");

        closingConnection = (NIOConnection) c;
        super.shutdown();
    }

    @Override
    public void handleSpecificStop() {
        LOGGER.debug("NIOEventManager handle specific stop");
        NIOListener.shutdown(closingConnection);

        LinkedList<NIOEvent> privateEvents = new LinkedList<>();
        while (nioListenerOn) {
            // Copy the events to a private list
            synchronized (this) {
                LinkedList<NIOEvent> list = (LinkedList<NIOEvent>) events;
                events = privateEvents;
                privateEvents = list;
            }

            // Process the events
            while (!privateEvents.isEmpty()) {
                NIOEvent event = privateEvents.removeFirst();
                if (event == null) {
                    return;
                }
                event.processEventOnConnection(this);
            }
            waitForEvents();
        }
    }

    public void listennerStopped() {
        nioListenerOn = false;
        addEvent(null);
    }

    @Override
    protected void specificActions() {
        long now = System.currentTimeMillis();
        if (lastReestablish + REESTABLISH_PERIOD > now) {
            return;
        }
        lastReestablish = now;
        NIOConnection.establishPendingConnections();
    }

    @Override
    protected final long waitEventsTimeout() {
        return REESTABLISH_PERIOD;
    }

}
