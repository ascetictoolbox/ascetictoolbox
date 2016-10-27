package es.bsc.comm.nio.event;

import es.bsc.comm.EventManager;
import es.bsc.comm.nio.NIOConnection;
import es.bsc.comm.stage.Stage;


public class NewTransferEvent extends NIOEvent {

    private final Stage transfer;


    public NewTransferEvent(NIOConnection c, Stage t) {
        super(c);
        this.transfer = t;
    }

    @Override
    public EventType getEventType() {
        return EventType.NEW_TRANSFER;
    }

    @Override
    public void processEventOnConnection(EventManager<?> ntm) {
        getConnection().requestStage(transfer);
    }

    @Override
    public String toString() {
        return "New Transfer Event for connection " + getConnection() + " with transfer " + transfer;
    }

}
