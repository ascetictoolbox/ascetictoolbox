package integratedtoolkit.types.data.operation;

import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.log.Loggers;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;


public abstract class DataOperation {

    private static int opCount = 0;
    private final int operationId;
    // Identifiers of the groups to which the operation belongs
    private final List<EventListener> listeners;
    private OpEndState endState = null;
    private Exception endException = null;

    protected static final Logger logger = Logger.getLogger(Loggers.COMM);
    protected static final boolean debug = logger.isDebugEnabled();

    private String name;

    public enum OpEndState {
        OP_OK,
        OP_IN_PROGRESS,
        OP_FAILED,
        OP_PREPARATION_FAILED,
        OP_WAITING_SOURCES;
    }

    public static abstract class EventListener {

        private static final AtomicInteger nextId = new AtomicInteger(0);
        private int id = nextId.getAndIncrement();

        public Integer getId() {
            return id;
        }

        public abstract void notifyEnd(DataOperation fOp);

        public abstract void notifyFailure(DataOperation fOp, Exception e);
    }

    public DataOperation(LogicalData ld, EventListener listener) {
        try {
            this.name = ld.getName();
        } catch (Exception e) {
        }
        this.listeners = new LinkedList<EventListener>();
        this.listeners.add(listener);
        operationId = opCount;
        opCount++;
    }

    public DataOperation(LogicalData ld, List<EventListener> eventListeners) {
        try {
            this.name = ld.getName();
        } catch (Exception e) {
        }
        this.listeners = eventListeners;
        operationId = opCount;
        opCount++;
    }

    public int getId() {
        return operationId;
    }

    public String getName() {
        return name;
    }

    public List<EventListener> getEventListeners() {
        return listeners;
    }

    public void addEventListener(EventListener eventListener) {
        synchronized (listeners) {
            if (endState == null) {
                this.listeners.add(eventListener);
            } else {
                switch (endState) {
                    case OP_OK:
                        eventListener.notifyEnd(this);
                        break;
                    case OP_IN_PROGRESS:
                        break;
                    case OP_WAITING_SOURCES:
                        break;
                    default: // OP_FAILED or OP_PREPARATION_FAILED
                        eventListener.notifyFailure(this, endException);
                        break;
                }
            }
        }
    }

    public void addEventListeners(List<EventListener> eventListeners) {
        synchronized (listeners) {
            if (endState == null) {
                this.listeners.addAll(eventListeners);
            } else {
                switch (endState) {
                    case OP_OK:
                        for (EventListener eventListener : eventListeners) {
                            eventListener.notifyEnd(this);
                        }
                        break;
                    case OP_IN_PROGRESS:
                        break;
                    case OP_WAITING_SOURCES:
                        break;
                    default: // OP_FAILED or OP_PREPARATION_FAILED
                        for (EventListener eventListener : eventListeners) {
                            eventListener.notifyFailure(this, endException);
                        }
                        break;
                }
            }
        }
    }

    public void end(OpEndState state) {
        notifyEnd(state, null);
    }

    public void end(OpEndState state, Exception e) {
        notifyEnd(state, e);
    }

    private void notifyEnd(OpEndState state, Exception e) {
        synchronized (listeners) {
            endState = state;
            endException = e;
            // Check end state of the operation
            switch (state) {
                case OP_OK:
                    for (EventListener listener : listeners) {
                        listener.notifyEnd(this);
                    }
                    break;
                case OP_IN_PROGRESS:
                    break;
                case OP_WAITING_SOURCES:
                    break;
                default: // OP_FAILED or OP_PREPARATION_FAILED
                    for (EventListener listener : listeners) {
                        listener.notifyFailure(this, e);
                    }
                    break;
            }
        }
    }
}
