package es.bsc.comm.nio.event;

import es.bsc.comm.EventManager;
import es.bsc.comm.nio.NIOConnection;


public class ClosedChannelEvent extends NIOEvent {

    public ClosedChannelEvent(NIOConnection nc) {
        super(nc);
    }

    @Override
    public EventType getEventType() {
        return EventType.CLOSED_CHANNEL;
    }

    @Override
    public void processEventOnConnection(EventManager<?> ntm) {
        getConnection().closedChannel();
    }

    @Override
    public String toString() {
        return "New closedChannel event for connection " + getConnection().hashCode();
    }
    
}
