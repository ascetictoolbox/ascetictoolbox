package es.bsc.comm.nio;

import es.bsc.comm.CommException;


public class NIOException extends CommException {

    private static final long serialVersionUID = 1L;


    public enum ErrorType {
        LOADING_EVENT_MANAGER_CLASS,
        LOADING_LISTENER,
        EVENT_MANAGER_INIT,
        MESSAGE_HANDLER_INIT,
        STARTING_SERVER,
        STARTING_CONNECTION,
        RESTARTING_CONNECTION,
        ACCEPTING_CONNECTION,
        FINISHING_CONNECTION,
        READ,
        WRITE,
        CLOSED_CONNECTION
    }


    private final ErrorType error;


    public NIOException(ErrorType error, Throwable t) {
        super(t);
        this.error = error;
    }

    public NIOException(ErrorType error, Exception e) {
        super(e);
        this.error = error;
    }

    public ErrorType getError() {
        return error;
    }

}
