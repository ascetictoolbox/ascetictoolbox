package es.bsc.comm;

import es.bsc.comm.stage.Transfer;


public interface MessageHandler {

    // Initialization
    public void init() throws CommException;

    // The transfer could not be completed
    public void errorHandler(Connection c, Transfer t, CommException e);

    // New data received
    public void dataReceived(Connection c, Transfer t);

    // New command received
    public void commandReceived(Connection c, Transfer t);

    // A send transfer has finished
    public void writeFinished(Connection c, Transfer t);

    // A connection has finished
    public void connectionFinished(Connection c);

    // Shutdown method
    public void shutdown();
}
