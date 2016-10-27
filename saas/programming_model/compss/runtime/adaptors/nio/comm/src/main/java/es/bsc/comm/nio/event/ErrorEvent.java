package es.bsc.comm.nio.event;

import es.bsc.comm.CommException;
import es.bsc.comm.EventManager;
import es.bsc.comm.nio.NIOConnection;


public class ErrorEvent extends NIOEvent {

    private final CommException exception;


    public ErrorEvent(NIOConnection nc, CommException e) {
        super(nc);
        exception = e;
    }

    public ErrorEvent(CommException e) {
        super(null);
        exception = e;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public EventType getEventType() {
        return EventType.ERROR;
    }

    @Override
    public void processEventOnConnection(EventManager<?> nem) {
        if (getConnection() != null) {
            getConnection().error(exception);
        } else {
            nem.notifyError(null, null, exception);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append("Error event");
        if (getConnection() != null) {
            sb.append(" on connection@").append(getConnection().hashCode());
        }
        sb.append(" caused by ").append(exception);

        return sb.toString();
    }
    
}
