package es.bsc.comm.nio;

import es.bsc.comm.stage.Reception;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class NIOServerConnection extends NIOConnection {

    private boolean uselessConnection;


    public NIOServerConnection(NIOEventManager ntm, SocketChannel sc, NIONode n) {
        super(ntm, sc, n);
        uselessConnection = true;
    }

    @Override
    public void receivedPacket(ByteBuffer buffer) {
        if (uselessConnection) {
            uselessConnection = false;
            currentStage = new Reception(false);
            startCurrentTransfer();
        }
        super.receivedPacket(buffer);
    }

    @Override
    public void closedChannel() {
        if (uselessConnection) {
            LOGGER.debug("Closing useless server connection " + this);
            unregisterChannel();
        } else {
            super.closedChannel();
        }
    }
}
