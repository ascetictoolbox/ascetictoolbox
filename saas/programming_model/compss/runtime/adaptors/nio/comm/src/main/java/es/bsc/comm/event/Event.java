package es.bsc.comm.event;

import es.bsc.comm.EventManager;


public interface Event {

    public enum EventType {
        NEW_TRANSFER, 
        CONNECTION_ESTABLISHED, 
        PACKET_ENTRY, 
        LOW_BUFFER,
        EMPTY_BUFFER, 
        CLOSED_CHANNEL, 
        ERROR
    }


    public EventType getEventType();

    public void processEventOnConnection(EventManager<?> ntm);

}
