package es.bsc.comm.stage;

import es.bsc.comm.CommException;
import es.bsc.comm.Connection;
import es.bsc.comm.EventManager;
import es.bsc.comm.InternalConnection;
import es.bsc.comm.TransferManager;
import es.bsc.comm.nio.NIOException;
import es.bsc.comm.nio.NIOProperties;
import es.bsc.comm.util.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Reception extends Transfer {
    
    private static final Logger LOGGER = LogManager.getLogger(TransferManager.LOGGER_NAME);

    private static Integer tokens = NIOProperties.getMaxReceives();
    private static LinkedList<InternalConnection> pausedConnections = new LinkedList<InternalConnection>();

    // To know whether the destination has been initialized or not
    private boolean destInit;
    private OutputStream streamOut;
    private boolean hasToken = false;
    private Token token;


    // New file/object to receive
    public Reception() {
        super(true);
        remainingSize = 0;
        destInit = false;
    }

    public Reception(boolean notifyErrors) {
        super(notifyErrors);
        remainingSize = 0;
        destInit = false;
    }

    // New object to receive
    public Reception(Destination destination) {
        super(true);
        this.type = Type.DATA;
        this.destination = destination;
        this.destInit = true;
    }

    public Reception(Destination destination, boolean notifyErrors) {
        super(notifyErrors);
        this.type = Type.DATA;
        this.destination = destination;
        this.destInit = true;
    }

    // New unexpected command to receive
    public Reception(Type type) {
        super(true);
        this.type = type;
        this.destination = Destination.OBJECT;
        destInit = false;
    }

    public Reception(Type type, boolean notifyErrors) {
        super(notifyErrors);
        this.type = type;
        this.destination = Destination.OBJECT;
        destInit = false;
    }

    private static void increaseTokens() {
        synchronized (tokens) {
            ++tokens;
        }
    }

    private static void decreaseTokens() {
        synchronized (tokens) {
            --tokens;
        }
    }

    // Initialize the new transfer
    public void initTransfer(Token t) throws FileNotFoundException {
        byte[] header = t.get(HEADER_SIZE);
        ByteBuffer bb = ByteBuffer.wrap(header);
        long size = bb.getLong();
        if (!destInit) {
            destInit = true;
            type = Type.values()[bb.getInt()];
            destination = Destination.values()[bb.getInt()];
        }
        setSize(size);
        openStream();
    }

    // Open the receive stream
    private void openStream() throws FileNotFoundException {
        // Open a stream to the destination
        if (destination == Destination.FILE) {
            // destination file
            streamOut = (OutputStream) new FileOutputStream(getFileName(), true);
        } else {
            // destination byte array
            streamOut = (OutputStream) new ByteArrayOutputStream((int) totalSize);
        }
    }

    // Write the buffer to the stream
    private void write(Token t) throws IOException, ClassNotFoundException {
        long length = Math.min(remainingSize, t.length());
        byte[] content = t.get((int) length);
        streamOut.write(content);
        remainingSize -= content.length;
        if (remainingSize == 0) {
            closeStream();
        }
    }

    private void closeStream() throws ClassNotFoundException, IOException {
        if (destination != Destination.FILE) {
            // Obtain the received byte array
            ByteArrayOutputStream baos = (ByteArrayOutputStream) streamOut;
            array = baos.toByteArray();
            if (destination == Destination.OBJECT) {
                // Deserialize the byte array into an object
                object = Serializer.deserialize(array);
            }
        }
        try {
            // Close the file stream
            streamOut.close();
        } catch (IOException e) {
            LOGGER.error("Error closing output stream on connection " + this);
        }

    }

    @Override
    public Direction getDirection() {
        return Direction.RECEIVE;
    }

    public void setReceptionDefaultFileName(String fname) {
        this.fileName = fname;
    }

    @Override
    public void start(InternalConnection connection, List<ByteBuffer> received, List<ByteBuffer> transmit) {
        // The initialization will be done when the first packet arrives. Not when
        // the transfer is started
        token = new Token();
        decreaseTokens();
        hasToken = true;
    }

    @Override
    public void progress(InternalConnection connection, List<ByteBuffer> received, List<ByteBuffer> transmit)
            throws IOException, ClassNotFoundException {
        while (!token.isCompletelyFilled() && !received.isEmpty()) {
            this.loadToken(token, received);
            if (token.isCompletelyFilled()) {
                if (!destInit || !sizeInit) {
                    initTransfer(token);
                }
                write(token);
                if (remainingSize > 0) {
                    token = new Token();
                }
            }
        }
    }

    @Override
    public boolean checkViability(boolean closedCommunication, List<ByteBuffer> received, List<ByteBuffer> transmit) throws NIOException {
        if (!closedCommunication || !received.isEmpty()) {
            return tokens > 0;
        } else {
            throw new NIOException(NIOException.ErrorType.CLOSED_CONNECTION,
                    new Exception("Channel was already closed for connection " + this));
        }
    }

    @Override
    public void pause(InternalConnection ic) {
        pausedConnections.add(ic);
    }

    @Override
    public boolean isComplete(List<ByteBuffer> received, List<ByteBuffer> transmit) {
        return sizeInit && remainingSize == 0;
    }

    @Override
    public void notifyCompletion(Connection c, EventManager<?> em) {
        if (isData()) { // DataTransfer
            em.dataReceived(c, this);
        } else {
            if (isCommand()) { // CommandTransfer
                em.commandReceived(c, this);
            }
        }
        increaseTokens();
        hasToken = false;
        if (!pausedConnections.isEmpty()) {
            InternalConnection ic = pausedConnections.removeFirst();
            ic.resume();
        }
    }

    @Override
    public void notifyError(Connection c, EventManager<?> em, CommException exc) {
        if (notifyErrors) {
            em.notifyError(c, this, exc);
        }
        if (hasToken) {
            increaseTokens();
            hasToken = false;
            if (!pausedConnections.isEmpty()) {
                InternalConnection ic = pausedConnections.removeFirst();
                ic.resume();
            }
        }

    }

}
