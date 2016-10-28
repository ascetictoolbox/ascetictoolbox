package es.bsc.comm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import es.bsc.comm.nio.NIOException;


public class TransferManager extends Thread {

    public static final String LOGGER_NAME = "Communication";
    private static final Logger LOGGER = LogManager.getLogger(LOGGER_NAME);

    private static final String PROPERTIES_ERROR = "Error loading properties file";

    private EventManager<?> em;


    public TransferManager() {
        super("TransferManager");
    }

    /**
     *
     * Initializes the Transfer manager to use a protocol to open connections.
     *
     * @param protocol
     *            Protocol to be used
     * @param properties
     *            Location of the properties files
     * @param mh
     *            Handler for the events produced by the connections
     */
    public void init(String eventManagerClassName, String properties, MessageHandler mh) throws CommException {
        LOGGER.debug("TransferManager init");
        // Name thread
        Thread.currentThread().setName("TransferManager");

        // Get Event Manager for reflection
        try {
            Constructor<?> constrEventManager = Class.forName(eventManagerClassName).getConstructor(MessageHandler.class);
            this.em = (EventManager<?>) constrEventManager.newInstance(mh);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Can not find adaptor class " + eventManagerClassName + ".", e);
            throw new NIOException(NIOException.ErrorType.LOADING_EVENT_MANAGER_CLASS, e);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Class " + eventManagerClassName + " has no valid constructor.", e);
            throw new NIOException(NIOException.ErrorType.LOADING_EVENT_MANAGER_CLASS, e);
        } catch (InstantiationException e) {
            LOGGER.error("Can not instantiate adaptor " + eventManagerClassName + ".", e);
            throw new NIOException(NIOException.ErrorType.LOADING_EVENT_MANAGER_CLASS, e);
        } catch (IllegalAccessException e) {
            LOGGER.error("Illegal access on adaptor " + eventManagerClassName + " creation", e);
            throw new NIOException(NIOException.ErrorType.LOADING_EVENT_MANAGER_CLASS, e);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Illegal argument on adaptor " + eventManagerClassName + " creation", e);
            throw new NIOException(NIOException.ErrorType.LOADING_EVENT_MANAGER_CLASS, e);
        } catch (InvocationTargetException e) {
            LOGGER.error("Wrong target for " + eventManagerClassName + " invocation", e);
            throw new NIOException(NIOException.ErrorType.LOADING_EVENT_MANAGER_CLASS, e);
        } catch (Exception e) {
            LOGGER.error(e);
            throw new NIOException(NIOException.ErrorType.LOADING_EVENT_MANAGER_CLASS, e);
        }

        // Initialize the EventManager
        try {
            em.init(properties);
        } catch (Exception e) {
            LOGGER.error(PROPERTIES_ERROR, e);
            throw new NIOException(NIOException.ErrorType.EVENT_MANAGER_INIT, e);
        }

        // Initialize the MessageHandler
        try {
            mh.init();
        } catch (Exception e) {
            LOGGER.error(PROPERTIES_ERROR, e);
            throw new NIOException(NIOException.ErrorType.MESSAGE_HANDLER_INIT, e);
        }
    }

    /**
     *
     * Starts the Transfer Manager with the given properties
     *
     */
    @Override
    public void run() {
        em.run();
    }

    /**
     * Start a new server on the local node
     *
     * @param n
     *            Local node representation where to open a connection
     * @throws es.bsc.comm.CommException
     *             Exception during the server socket creation. Further details on the exception cause.
     */
    public void startServer(Node n) throws CommException {
        LOGGER.debug("TransferManager Start Server");
        em.startServer(n);
    }

    /**
     * Opens and starts a client connection to a remote node to transfer data through
     *
     * @param node
     *            Remote node
     * @return Connection between both nodes
     * @throws CommException
     */
    public Connection startConnection(Node node) {
        return em.startConnection(node);
    }

    /**
     * Shuts down all the communication platform.
     *
     *
     * @param notifyTo
     *            Connection to let a remote node know that the node has been shut down.
     */
    public void shutdown(Connection notifyTo) {
        LOGGER.debug("TransferManager shutdown");
        em.shutdown(notifyTo);
    }

}
