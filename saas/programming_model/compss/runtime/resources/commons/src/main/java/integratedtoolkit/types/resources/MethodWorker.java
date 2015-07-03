package integratedtoolkit.types.resources;

import integratedtoolkit.types.COMPSsWorker;
import integratedtoolkit.types.Implementation;

import java.util.HashMap;

public class MethodWorker extends Worker<MethodResourceDescription> {

    private String name;

    // Available resource capabilities
    protected final MethodResourceDescription available;

    public MethodWorker(String name, MethodResourceDescription description, COMPSsWorker worker, Integer maxTaskCount) {
        super(name, description, worker, maxTaskCount);
        this.name = name;
        available = new MethodResourceDescription(description);

    }

    public MethodWorker(String name, MethodResourceDescription description, String adaptor, HashMap<String, String> properties, Integer maxTaskCount) throws Exception {
        super(name, description, adaptor, properties, maxTaskCount);
        this.name = name;
        if (description != null) {
            this.description.setSlots(maxTaskCount);
        }
        this.available = new MethodResourceDescription(description); // clone
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean reserveResource(MethodResourceDescription consumption) {
        available.setProcessorCoreCount(available.getProcessorCoreCount() - consumption.getProcessorCoreCount());
        available.setMemoryPhysicalSize(available.getMemoryPhysicalSize() - consumption.getMemoryPhysicalSize());
        return true;
    }

    @Override
    public void releaseResource(MethodResourceDescription consumption) {
        available.setProcessorCoreCount(available.getProcessorCoreCount() + consumption.getProcessorCoreCount());
        available.setMemoryPhysicalSize(available.getMemoryPhysicalSize() + consumption.getMemoryPhysicalSize());
    }

    @Override
    public boolean hasAvailable(MethodResourceDescription consumption) {
        return !(available.getProcessorCoreCount() < consumption.getProcessorCoreCount() || available.getMemoryPhysicalSize() < consumption.getMemoryPhysicalSize());
    }

    @Override
    public Integer fitCount(Implementation impl) {
        if (impl.getType() == Implementation.Type.SERVICE) {
            return null;
        }
        MethodResourceDescription ctrs = (MethodResourceDescription) impl.getRequirements();
        return description.canHostSimultaneously(ctrs);
    }

    @Override
    public Type getType() {
        return Type.WORKER;
    }

    @Override
    public String getMonitoringData(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("<CPU>").append(description.getProcessorCPUCount()).append("</CPU>").append("\n");
        sb.append(prefix).append("<Core>").append(description.getProcessorCoreCount()).append("</Core>").append("\n");
        sb.append(prefix).append("<Memory>").append(description.getMemoryPhysicalSize()).append("</Memory>").append("\n");
        sb.append(prefix).append("<Disk>").append(description.getStorageElemSize()).append("</Disk>").append("\n");
        return sb.toString();
    }

    private Float getValue() {
        return description.value;
    }

    @Override
    public int compareTo(Resource t) {
        if (t == null) {
            throw new NullPointerException();
        }
        switch (t.getType()) {
            case SERVICE:
                return 1;
            case WORKER:
                MethodWorker w = (MethodWorker) t;
                if (description.getValue() == null) {
                    if (w.getValue() == null) {
                        return w.getName().compareTo(getName());
                    }
                    return 1;
                }
                if (w.getValue() == null) {
                    return -1;
                }
                float dif = w.getValue() - description.getValue();
                if (dif > 0) {
                    return -1;
                }
                if (dif < 0) {
                    return 1;
                }
                return getName().compareTo(w.getName());
            case MASTER:
                return -1;
            default:
                return getName().compareTo(t.getName());
        }
    }

    @Override
    public boolean canRun(Implementation implementation) {
        switch (implementation.getType()) {
            case METHOD:
                MethodResourceDescription ctrs = (MethodResourceDescription) implementation.getRequirements();
                return description.contains(ctrs);
            default:
                return false;
        }
    }

    @Override
    public String getResourceLinks(String prefix) {
        StringBuilder sb = new StringBuilder(super.getResourceLinks(prefix));
        sb.append(prefix).append("TYPE = WORKER").append("\n");
        sb.append(prefix).append("CPU = ").append(description.getProcessorCPUCount()).append("\n");
        sb.append(prefix).append("MEMORY = ").append(description.getMemoryPhysicalSize()).append("\n");

        return sb.toString();
    }

}
