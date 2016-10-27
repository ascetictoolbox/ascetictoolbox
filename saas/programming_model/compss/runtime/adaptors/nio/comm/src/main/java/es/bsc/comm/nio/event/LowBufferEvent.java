package es.bsc.comm.nio.event;

import es.bsc.comm.Connection;
import es.bsc.comm.EventManager;
import es.bsc.comm.InternalConnection;
import es.bsc.comm.nio.NIOConnection;


public class LowBufferEvent<T extends Connection & InternalConnection> extends NIOEvent {

    public LowBufferEvent(NIOConnection nc) {
        super(nc);
    }

    @Override
    public EventType getEventType() {
        return EventType.LOW_BUFFER;
    }

    @Override
    public void processEventOnConnection(EventManager<?> ntm) {
        getConnection().lowSendBuffer();
    }

    @Override
    public String toString() {
        return "More To Write Event for connection@" + getConnection().hashCode();
    }

}
