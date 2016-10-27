package es.bsc.comm.nio.event;

import es.bsc.comm.event.Event;
import es.bsc.comm.nio.NIOConnection;


public abstract class NIOEvent implements Event {

    private final NIOConnection connection;


    public NIOEvent(NIOConnection c) {
        this.connection = c;
    }

    public NIOConnection getConnection() {
        return connection;
    }
}
