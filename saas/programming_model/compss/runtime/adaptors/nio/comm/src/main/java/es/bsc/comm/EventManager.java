package es.bsc.comm;

import es.bsc.comm.event.Event;
import es.bsc.comm.stage.Reception;
import es.bsc.comm.stage.Submission;
import es.bsc.comm.stage.Transfer;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public abstract class EventManager<T extends Event> {

    protected static final Logger LOGGER = LogManager.getLogger(TransferManager.LOGGER_NAME);

    private boolean isBlocked;
    protected List<T> events;

    private boolean stopReceived;

    private final MessageHandler mh;


    public EventManager(MessageHandler messageReceiver) {
        stopReceived = false;
        events = new LinkedList<T>();
        isBlocked = false;
        mh = messageReceiver;
    }

    // While worker is running
    public void run() {
        LOGGER.info("Event Manager started");

        LinkedList<T> privateEvents = new LinkedList<T>();
        while (!stopReceived || !events.isEmpty()) {
            // Handle specific implementation actions
            specificActions();

            // Copy the events to a private list
            synchronized (this) {
                LinkedList<T> list = (LinkedList<T>) events;
                events = privateEvents;
                privateEvents = list;
            }

            // Process the events
            while (!privateEvents.isEmpty()) {
                Event event = privateEvents.removeFirst();
                LOGGER.debug("Processing event " + event.getEventType());
                processEvent(event);
            }

            LOGGER.debug("Waiting for events");
            waitForEvents();
        }

        LOGGER.info("Event Manager begins shutdown process");
        handleSpecificStop();
        mh.shutdown();
        LOGGER.info("Event Manager stopped");
    }

    protected void waitForEvents() {
        // Sleep since there is nprocessEventothing to process
        if (events.isEmpty() && !stopReceived) {
            synchronized (this) {
                if (events.isEmpty() && !stopReceived) {
                    isBlocked = true;
                    try {
                        LOGGER.debug("Event Manager wait");
                        this.wait(waitEventsTimeout());
                    } catch (InterruptedException e) {
                        // No need to handle such exception
                    }
                    isBlocked = false;
                }
            }
        }
    }

    private void processEvent(Event e) {
        LOGGER.debug("Processing " + e);
        e.processEventOnConnection(this);
    }

    public void addEvent(T e) {
        synchronized (this) {
            events.add(e);
            if (isBlocked) {
                this.notify();
            }
        }
    }

    public void shutdown() {
        stopReceived = true;
        synchronized (this) {
            if (isBlocked) {
                this.notify();
            }
        }
    }

    public void notifyError(Connection con, Transfer t, CommException exception) {
        mh.errorHandler(con, t, exception);
    }

    public void dataReceived(Connection c, Reception t) {
        mh.dataReceived(c, t);
    }

    public void commandReceived(Connection c, Reception t) {
        mh.commandReceived(c, t);
    }

    public void writeFinished(Connection c, Submission t) {
        mh.writeFinished(c, t);
    }

    public void connectionFinished(Connection c) {
        mh.connectionFinished(c);
    }

    public abstract void init(String properties) throws CommException;

    public abstract void startServer(Node n) throws CommException;

    public abstract Connection startConnection(Node n);

    protected abstract long waitEventsTimeout();

    protected abstract void specificActions();

    protected abstract void handleSpecificStop();

    public abstract void shutdown(Connection c);

}
