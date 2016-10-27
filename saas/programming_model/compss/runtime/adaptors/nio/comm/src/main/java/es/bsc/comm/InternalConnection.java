package es.bsc.comm;

import es.bsc.comm.stage.Stage;
import java.nio.ByteBuffer;


public interface InternalConnection {

    public Node getNode();

    public void requestStage(Stage t);

    public void established();

    public void receivedPacket(ByteBuffer buffer);

    public void lowSendBuffer();

    public void emptySendBuffer();

    public void closedChannel();

    public void error(CommException exception);

    public void resume();

}
