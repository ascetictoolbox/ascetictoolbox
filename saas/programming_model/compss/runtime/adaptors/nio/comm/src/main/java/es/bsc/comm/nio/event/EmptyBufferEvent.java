package es.bsc.comm.nio.event;

import es.bsc.comm.EventManager;
import es.bsc.comm.nio.NIOConnection;


public class EmptyBufferEvent extends NIOEvent {

    public EmptyBufferEvent(NIOConnection nc) {
        super(nc);
    }

    @Override
    public EventType getEventType() {
        return EventType.EMPTY_BUFFER;
    }

    @Override
    public void processEventOnConnection(EventManager<?> ntm) {
        getConnection().emptySendBuffer();
    }

    @Override
    public String toString() {
        return "Write Empty Event for socket@" + getConnection().hashCode();
    }

}
