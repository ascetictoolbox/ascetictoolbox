package es.bsc.comm.nio;

import es.bsc.comm.CommException;
import es.bsc.comm.Connection;
import es.bsc.comm.EventManager;
import es.bsc.comm.InternalConnection;
import es.bsc.comm.Node;
import es.bsc.comm.TransferManager;
import es.bsc.comm.nio.event.NIOEvent;
import es.bsc.comm.nio.event.NewTransferEvent;
import es.bsc.comm.nio.NIOException.ErrorType;
import es.bsc.comm.stage.Stage;
import es.bsc.comm.stage.Reception;
import es.bsc.comm.stage.Submission;
import es.bsc.comm.stage.Shutdown;
import es.bsc.comm.stage.Transfer;

import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


// NIO specific transfer
public class NIOConnection implements Connection, InternalConnection {

    protected static final Logger LOGGER = LogManager.getLogger(TransferManager.LOGGER_NAME);

    private static final LinkedList<NIOConnection> PENDING_ESTABLISH_CONNECTIONS = new LinkedList<NIOConnection>();

    // Socket where transfer is made
    private SocketChannel sc;

    // Status booleans
    private boolean established;
    // To not to overpass the transfer limits
    private boolean paused;

    private boolean closedSocket;
    private boolean closedConnection;

    // Amount of connection drops due to timeout on connect stage
    private int conRetries = 0;

    // Current transfer
    protected Stage currentStage;
    // Queued transfers
    private final LinkedList<Stage> pendingStages;

    // Stored packets for next transfers
    private final LinkedList<ByteBuffer> receiveBuffer;
    private final LinkedList<ByteBuffer> sendBuffer;

    // Connection made to this node
    private final NIONode node;

    protected EventManager<NIOEvent> em;


    // Start a connection
    public NIOConnection(NIOEventManager ntm, SocketChannel sc, NIONode n) {
        this.em = ntm;
        this.node = n;

        currentStage = null;
        pendingStages = new LinkedList<Stage>();

        receiveBuffer = new LinkedList<ByteBuffer>();
        sendBuffer = new LinkedList<ByteBuffer>();

        established = false;
        closedSocket = false;
        closedConnection = false;

        if (sc != null) {
            registerChannel(sc);
        }
    }

    @Override
    public Node getNode() {
        return node;
    }

    /**
     * Sends a command through the connection
     *
     * @param cmd
     *            Command to submit through the connection
     */
    @Override
    public void sendCommand(Object cmd) {
        Transfer t = new Submission(Transfer.Type.COMMAND, cmd);
        em.addEvent(new NewTransferEvent(this, t));
    }

    /**
     * Sends data stored in a file through the connection.
     *
     * @param name
     *            Location of the file that will be submitted
     */
    @Override
    public void sendDataFile(String name) {
        Transfer t = new Submission(name);
        em.addEvent(new NewTransferEvent(this, t));
    }

    /**
     * Sends an object through the connection.
     *
     * @param o
     *            Object that will be submitted
     */
    @Override
    public void sendDataObject(Object o) {
        Transfer t = new Submission(Transfer.Type.DATA, o);
        em.addEvent(new NewTransferEvent(this, t));
    }

    /**
     * Sends the data stored in a byte arrey through the connection.
     *
     * @param array
     *            Data to be transferred through the connection.
     */
    @Override
    public void sendDataArray(byte[] array) {
        Transfer t = new Submission(Transfer.Type.DATA, array);
        em.addEvent(new NewTransferEvent(this, t));
    }

    /**
     * Enables the connection to receive some command or data and notify it
     */
    @Override
    public void receive() {
        Transfer t = new Reception();
        em.addEvent(new NewTransferEvent(this, t));
    }

    /**
     * Enable the connection to receive a command or some data. In case of receiving a data that originally was stored
     * in a file, the data will be saved in a file.
     *
     * @param name
     *            Location of the file where data can potentially be saved
     */
    @Override
    public void receive(String name) {
        Reception t = new Reception();
        t.setReceptionDefaultFileName(name);
        em.addEvent(new NewTransferEvent(this, t));
    }

    /**
     * Enables the connection to receive some data and store it as an object
     */
    @Override
    public void receiveDataObject() {
        Reception t = new Reception(Transfer.Destination.OBJECT);
        em.addEvent(new NewTransferEvent(this, t));
    }

    /**
     * Enables the connection to receive some data and store it as a byte array
     */
    @Override
    public void receiveDataArray() {
        Reception t = new Reception(Transfer.Destination.ARRAY);
        em.addEvent(new NewTransferEvent(this, t));
    }

    /**
     * Enables the connection to receive some data and store it in a file
     *
     * @param name
     *            Location of the file where to save the received data
     */
    @Override
    public void receiveDataFile(String name) {
        Reception t = new Reception(Transfer.Destination.FILE);
        t.setReceptionDefaultFileName(name);
        em.addEvent(new NewTransferEvent(this, t));
    }

    /**
     * Closes the connection after all the previous ordered transfers (Receives and Transmit) have been processed.
     */
    @Override
    public void finishConnection() {
        Stage t = new Shutdown();
        em.addEvent(new NewTransferEvent(this, t));
    }

    /*
     ************************************
     ************************************
     *********** Internal use ***********
     ************************************
     ************************************
     */
    // Enqueue a new transfer to this connection
    @Override
    public void requestStage(Stage t) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Requesting transfer " + t + " on connection " + this.hashCode());
        }

        pendingStages.add(t);
        if (currentStage == null) {
            handleNextTransfer();
        }
    }

    public void established() {
        established = true;
        handleNextTransfer();
    }

    @Override
    public void receivedPacket(ByteBuffer buffer) {
        receiveBuffer.add(buffer);
        if (currentStage != null) {
            progressCurrentTransfer();
        }
    }

    @Override
    public void lowSendBuffer() {
        if (currentStage != null) {
            progressCurrentTransfer();
        }
    }

    @Override
    public void emptySendBuffer() {
        if (currentStage != null && currentStage.isComplete(receiveBuffer, sendBuffer)) {
            currentStage.notifyCompletion(this, em);
            // Handle the next entry in the queue
            handleNextTransfer();
        }
    }

    @Override
    public void closedChannel() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Channel on connection " + this.hashCode() + " has been closed");
        }

        closedSocket = true;
        if (currentStage != null && !paused) {
            currentStage.notifyError(this, em, new NIOException(ErrorType.CLOSED_CONNECTION,
                    new Exception("Channel was already closed for connection " + this.hashCode())));
            handleNextTransfer();
        }
        if (closedConnection) {
            unregisterChannel();
            em.connectionFinished(this);
        }
    }

    // Close this connection
    public void closeConnection() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Requesting connection " + this.hashCode() + " closure");
        }

        if (!closedSocket) {
            NIOListener.closeSocket(this, sc);
        } else {
            if (!closedConnection) {
                unregisterChannel();
                em.connectionFinished(this);
            }
        }
        closedConnection = true;
        handleNextTransfer();
    }

    private void registerChannel(SocketChannel socket) {
        sc = socket;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Associating Socket: " + sc.hashCode() + " with connection: " + this.hashCode());
        }
    }

    protected void unregisterChannel() {
        sc = null;
    }

    public void replaceChannel(SocketChannel newSocket) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Re-Associating Socket: " + sc.hashCode() + " -> " + newSocket.hashCode() + " with connection: " + this.hashCode());
        }

        sc = newSocket;
    }

    private void pause() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Transfer " + currentStage + " on connection " + this.hashCode() + " is paused.");
        }

        paused = true;
        currentStage.pause(this);
    }

    @Override
    public void resume() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Resume transfer " + currentStage + " on connection " + this.hashCode());
        }

        paused = false;
        startCurrentTransfer();
    }

    private void handleNextTransfer() {
        if (paused || !established) {
            return;
        }
        if (!pendingStages.isEmpty()) {
            // Update the previous and actual states
            currentStage = pendingStages.removeFirst();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("NIOConnection " + this.hashCode() + " takes " + currentStage);
            }
            // Check if it is a shutdown, a send or a receive
            if (currentStage.isShutdown()) {
                closeConnection();
            } else {
                startCurrentTransfer();
            }
        } else {
            currentStage = null;
        }
    }

    protected void startCurrentTransfer() {
        if (paused) {
            return;
        }
        try {
            if (currentStage.checkViability(closedSocket, receiveBuffer, sendBuffer)) {
                // The transfer can potentially success. If it is a submission
                // transfer, the channel is still open to send the data. In
                // case of a Receive transfer, although the channel is closed,
                // some data can be held in the receive buffer.

                // The transfer is initialized
                try {
                    currentStage.start(this, receiveBuffer, sendBuffer);
                    // And processed as much as possible given the current buffers
                    progressCurrentTransfer();
                } catch (Exception e) {
                    currentStage.notifyError(this, em, new NIOException(ErrorType.CLOSED_CONNECTION, e));
                    handleNextTransfer();
                }
            } else {
                pause();
            }
        } catch (CommException ne) {
            // There is no way to accomplish the transfer. The error has to be notified to the message handler
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Connection closed before completing " + currentStage);
            }
            currentStage.notifyError(this, em, new NIOException(ErrorType.CLOSED_CONNECTION,
                    new Exception("Channel was already closed for connection " + this.hashCode())));
            // We try to handle the next transfer in the queue
            handleNextTransfer();
        }
    }

    public void progressCurrentTransfer() {
        if (paused) {
            return;
        }
        try {
            currentStage.progress(this, receiveBuffer, sendBuffer);
            if (currentStage.isComplete(receiveBuffer, sendBuffer)) {
                // The transfer has been completed.
                // The transfer completion is notified
                currentStage.notifyCompletion(this, em);
                // We deal with the next transfer
                handleNextTransfer();
            } else {
                if (closedSocket) {
                    // The socket may have been closed and the transfer would be impossible to end
                    // Checking viability
                    if (!currentStage.checkViability(closedSocket, receiveBuffer, sendBuffer)) {
                        // It won't be possible to finish.
                        // NOtify the error
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Connection closed before completing " + currentStage);
                        }
                        currentStage.notifyError(this, em, new NIOException(ErrorType.CLOSED_CONNECTION,
                                new Exception("Channel was already closed for connection " + this.hashCode())));
                        // Deal with the following transfer transfer in the queue.
                        handleNextTransfer();
                    }
                } else {
                    if (!sendBuffer.isEmpty()) {
                        NIOListener.changeInterest(this, sc, SelectionKey.OP_WRITE);
                    }
                }
            }
        } catch (Exception e) {
            currentStage.notifyError(this, em, new NIOException(ErrorType.CLOSED_CONNECTION, e));
            handleNextTransfer();
        }
    }

    @Override
    public void error(CommException exception) {
        LOGGER.debug("Processing error on connection " + this.hashCode());
        NIOException ne = (NIOException) exception;
        if ((ne.getError() == ErrorType.FINISHING_CONNECTION) || (ne.getError() == ErrorType.STARTING_CONNECTION)
                || (ne.getError() == ErrorType.RESTARTING_CONNECTION)) {

            conRetries++;
            if (conRetries < NIOProperties.getMaxRetries()) {
                if (ne.getCause() instanceof SocketTimeoutException) {
                    reestablishConnection();
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Connection " + this.hashCode() + " will try to be reestablish in the future.");
                    }
                    PENDING_ESTABLISH_CONNECTIONS.add(this);
                }
            } else {
                closedSocket = true;
                unregisterChannel();
                handleNextTransfer();
            }
        } else {
            currentStage.notifyError(this, em, exception);
            handleNextTransfer();
        }
    }

    public static void establishPendingConnections() {
        for (NIOConnection nc : PENDING_ESTABLISH_CONNECTIONS) {
            nc.reestablishConnection();
        }
        PENDING_ESTABLISH_CONNECTIONS.clear();
    }

    private void reestablishConnection() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Reestablishing connection " + this.hashCode());
        }
        NIOListener.restartConnection(this, node);
    }

    public static void abortPendingConnections() {
        PENDING_ESTABLISH_CONNECTIONS.clear();
    }

    public LinkedList<ByteBuffer> getSendBuffer() {
        return sendBuffer;
    }

    public Stage getCurrentTransfer() {
        return currentStage;
    }

    public SocketChannel getSocket() {
        return sc;
    }

}
