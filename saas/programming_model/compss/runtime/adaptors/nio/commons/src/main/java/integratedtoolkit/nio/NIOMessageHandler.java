package integratedtoolkit.nio;

import es.bsc.comm.CommException;
import es.bsc.comm.Connection;
import es.bsc.comm.MessageHandler;

import es.bsc.comm.TransferManager;
import es.bsc.comm.nio.NIONode;
import es.bsc.comm.transfers.Transfer;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.nio.commands.Command;
import org.apache.log4j.Logger;

public class NIOMessageHandler implements MessageHandler {

    protected static final Logger LOGGER = Logger.getLogger(Loggers.COMM);

    private final NIOAgent agent;
    private final NIONode me;

    public NIOMessageHandler(NIOAgent agent, NIONode node) {
        this.agent = agent;
        this.me = node;
    }

    @Override
    public void init() {
        LOGGER.debug("Starting transfer server...");
        try {
            TransferManager.startServer(me);
        } catch (Exception e) {
            LOGGER.error(e);
            e.printStackTrace();
        }
        LOGGER.debug("Server started");
    }

    @Override
    public void errorHandler(Connection c, Transfer t, CommException ce) {
        String errorText = "Error a NIO:" + ce.getMessage() + " tractant " + t + "\n";
        LOGGER.error(errorText, ce);
    }

    @Override
    public void dataReceived(Connection c, Transfer t) {
        LOGGER.debug("Received data " + (t.isFile() ? t.getFileName() : t.getObject()) + "through connection " + c);
        agent.receivedData(c, t);
    }

    @Override
    public void commandReceived(Connection c, Transfer t) {
        try {
            Command cmd = (Command) t.getObject();
            LOGGER.debug("Received Command " + cmd + " through connection " + c);
            cmd.agent = agent;
            cmd.handle(c);
        } catch (Exception e) {
            e.printStackTrace();
            c.finishConnection();
        }
    }

    @Override
    public void writeFinished(Connection c, Transfer t) {
        LOGGER.debug("Finished sending " + (t.isFile() ? t.getFileName() : t.getObject()) + " through connection " + c);
        agent.releaseSendSlot(c);
    }

    @Override
    public void connectionFinished(Connection c) {
        LOGGER.debug("Connection " + c + "finished");
    }

    @Override
    public void shutdown() {

    }

}