package es.bsc.comm.nio.event;

import es.bsc.comm.EventManager;
import es.bsc.comm.nio.NIOConnection;
import java.nio.ByteBuffer;


public class PacketEntry extends NIOEvent {

    private final ByteBuffer buffer;


    public PacketEntry(NIOConnection nc, ByteBuffer buff) {
        super(nc);
        buffer = buff;
    }

    @Override
    public EventType getEventType() {
        return EventType.PACKET_ENTRY;
    }

    @Override
    public void processEventOnConnection(EventManager<?> ntm) {
        getConnection().receivedPacket(buffer);
    }

    @Override
    public String toString() {
        return "Package received through  connection@" + getConnection().hashCode();
    }

}
