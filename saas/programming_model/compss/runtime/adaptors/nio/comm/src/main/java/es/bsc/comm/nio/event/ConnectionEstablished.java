package es.bsc.comm.nio.event;

import es.bsc.comm.Connection;
import es.bsc.comm.EventManager;
import es.bsc.comm.InternalConnection;
import es.bsc.comm.nio.NIOConnection;


public class ConnectionEstablished<T extends Connection & InternalConnection> extends NIOEvent {

    public ConnectionEstablished(NIOConnection nc) {
        super(nc);
    }

    @Override
    public EventType getEventType() {
        return EventType.CONNECTION_ESTABLISHED;
    }

    @Override
    public void processEventOnConnection(EventManager<?> ntm) {
        getConnection().established();
    }

    @Override
    public String toString() {
        return "Connection actually established for socket@" + getConnection().hashCode();
    }

}
