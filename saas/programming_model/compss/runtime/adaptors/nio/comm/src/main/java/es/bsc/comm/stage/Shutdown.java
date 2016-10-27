package es.bsc.comm.stage;

import es.bsc.comm.CommException;
import es.bsc.comm.Connection;
import es.bsc.comm.EventManager;
import es.bsc.comm.InternalConnection;
import java.nio.ByteBuffer;
import java.util.List;


public class Shutdown extends Stage {

    public Shutdown() {
        super(false);
    }

    @Override
    public boolean isShutdown() {
        return true;
    }

    @Override
    public boolean checkViability(boolean closedCommunication, List<ByteBuffer> received, List<ByteBuffer> transmit) {
        return true;
    }

    @Override
    public void start(InternalConnection connection, List<ByteBuffer> received, List<ByteBuffer> transmit) {
        // It's a phantom task to shutdown the connection no need to do anything
    }

    @Override
    public void progress(InternalConnection connection, List<ByteBuffer> received, List<ByteBuffer> transmit) {
        // Progress is defined by the EM
    }

    @Override
    public boolean isComplete(List<ByteBuffer> received, List<ByteBuffer> transmit) {
        return true;
    }

    @Override
    public void notifyCompletion(Connection c, EventManager<?> em) {
        // No need to confirm that the transfer is completed
    }

    @Override
    public void notifyError(Connection c, EventManager<?> em, CommException exc) {
        // There can't be errors
    }

    @Override
    public void pause(InternalConnection ic) {
        // Cannot be paused
    }

}
