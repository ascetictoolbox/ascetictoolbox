package es.bsc.comm.stage;

import es.bsc.comm.CommException;
import es.bsc.comm.Connection;
import es.bsc.comm.EventManager;
import es.bsc.comm.InternalConnection;
import es.bsc.comm.nio.NIOProperties;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;


public abstract class Stage {

    protected final boolean notifyErrors;


    public Stage(boolean notifyErrors) {
        this.notifyErrors = notifyErrors;
    }

    public boolean isShutdown() {
        return false;
    }

    public abstract boolean checkViability(boolean closedCommunication, List<ByteBuffer> received, List<ByteBuffer> transmit) throws CommException;

    public abstract void start(InternalConnection connection, List<ByteBuffer> received, List<ByteBuffer> transmit) throws Exception;

    public abstract void progress(InternalConnection connection, List<ByteBuffer> received, List<ByteBuffer> transmit) throws Exception;

    // Transfer is complete
    public abstract boolean isComplete(List<ByteBuffer> received, List<ByteBuffer> transmit);

    public abstract void notifyCompletion(Connection c, EventManager<?> em);

    public abstract void notifyError(Connection c, EventManager<?> em, CommException exc);

    public abstract void pause(InternalConnection ic);

    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    protected void sendToken(Token token, List<ByteBuffer> transmit) {
        while (!token.isCompletelyRead() && transmit.size() < NIOProperties.getMaxBufferedPackets()) {
            sendTokenPacket(token, transmit);
        }
    }

    protected void sendTokenPacket(Token t, List<ByteBuffer> transmit) {
        ByteBuffer bb = ByteBuffer.wrap(t.get(NIOProperties.getPacketSize()));
        synchronized (transmit) {
            transmit.add(bb);
        }
    }

    protected void loadToken(Token token, List<ByteBuffer> received) {
        while (!received.isEmpty() && !token.isCompletelyFilled()) {
            loadTokenPacket(token, received);
        }
    }

    protected void loadTokenPacket(Token t, List<ByteBuffer> received) {
        LinkedList<ByteBuffer> rec = (LinkedList<ByteBuffer>) received;
        ByteBuffer bb = rec.removeFirst();
        t.load(bb);
        if (bb.hasRemaining()) {
            rec.addFirst(bb);
        }
    }


    protected static class Token {

        private static final int ONE_BYTE = 8;
        private static final int TWO_BYTES = 16;
        private static final int THREE_BYTES = 24;
        
        private static final int SIZE_LENGTH = 4;
        public static final int MAX_SIZE = 10_240;
        

        private final byte[] sizeArray = new byte[SIZE_LENGTH];
        private byte[] content;

        private int putPosition = 0;
        private int readPosition = 0;


        public Token() {
        }

        public Token(byte[] data) {
            int size = data.length;
            sizeArray[0] = (byte) (size >>> THREE_BYTES);
            sizeArray[1] = (byte) (size >>> TWO_BYTES);
            sizeArray[2] = (byte) (size >>> ONE_BYTE);
            sizeArray[3] = (byte) (size);
            content = data;
            putPosition = SIZE_LENGTH + data.length;
        }

        public void load(ByteBuffer data) {
            if (putPosition < SIZE_LENGTH) {
                // Obtain size of the token content
                for (; putPosition < SIZE_LENGTH && data.remaining() > 0; putPosition++) {
                    sizeArray[putPosition] = data.get();
                }
                if (putPosition == SIZE_LENGTH) {
                    int size = sizeArray[3] & 0xFF | (sizeArray[2] & 0xFF) << ONE_BYTE | (sizeArray[1] & 0xFF) << TWO_BYTES | (sizeArray[0] & 0xFF) << THREE_BYTES;
                    readPosition = SIZE_LENGTH;
                    content = new byte[size];
                }
            }

            if (data.remaining() > 0) {
                int toRead = data.remaining();
                int toWrite = content.length - (putPosition - SIZE_LENGTH);
                int read = Math.min(toRead, toWrite);
                data.get(content, putPosition - SIZE_LENGTH, read);
                putPosition += read;
            }

        }

        public byte[] getArray() {
            return content;
        }

        public int length() {
            if (content != null) {
                return content.length;
            } else {
                return 0;
            }
        }

        public boolean isCompletelyFilled() {
            return content != null && putPosition == content.length + SIZE_LENGTH;
        }

        public boolean isCompletelyRead() {
            return content != null && readPosition == content.length + SIZE_LENGTH;
        }

        public byte[] get(int size) {
            int arraySize = putPosition - readPosition;
            arraySize = Math.min(size, arraySize);
            byte[] b = new byte[arraySize];
            int idx = 0;
            for (; readPosition < SIZE_LENGTH; readPosition++) {
                b[idx] = sizeArray[readPosition];
                idx++;
            }
            if (idx < arraySize && readPosition < putPosition) {
                System.arraycopy(content, readPosition - SIZE_LENGTH, b, idx, arraySize - idx);
                readPosition += arraySize - idx;
            }
            return b;
        }
    }

}
