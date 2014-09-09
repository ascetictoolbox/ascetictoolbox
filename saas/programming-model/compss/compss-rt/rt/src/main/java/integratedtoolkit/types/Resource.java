package integratedtoolkit.types;

import integratedtoolkit.types.Implementation;
import integratedtoolkit.util.CoreManager;
import java.util.LinkedList;

public abstract class Resource implements Comparable<Resource> {

    public enum Type {

        WORKER,
        SERVICE
    }

    // Max number of tasks
    int maxTaskCount;
    // Number of tasks assigned to the resource
    int taskCount;
    // CoreIds that can be executed by this resource
    LinkedList<Integer> executableCores;
    // Number of tasks that can be run simultaneously per core id
    int[] coreSimultaneousTasks;
    // Number of tasks that can be run simultaneously per core id (maxTaskCount not considered)
    int[] idealSimultaneousTasks;

    public Resource() {
        this.coreSimultaneousTasks = new int[CoreManager.coreCount];
        this.idealSimultaneousTasks = new int[CoreManager.coreCount];
        this.executableCores = new LinkedList<Integer>();
        this.taskCount = 0;
        this.maxTaskCount = 0;
    }

    public Resource(Integer maxTaskCount) {
        this.coreSimultaneousTasks = new int[CoreManager.coreCount];
        this.idealSimultaneousTasks = new int[CoreManager.coreCount];
        this.maxTaskCount = maxTaskCount;
        this.taskCount = 0;
        this.executableCores = new LinkedList<Integer>();

    }

    public abstract void setName(String name);

    public abstract String getName();

    public void setMaxTaskCount(int count) {
        maxTaskCount = count;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public int getMaxTaskCount() {
        return maxTaskCount;
    }

    public void setExecutableCores(LinkedList<Integer> executableCores) {
        this.executableCores = executableCores;
    }

    public LinkedList<Integer> getExecutableCores() {
        return executableCores;
    }

    public int[] newCoreElementsDetected(LinkedList<Integer> newCores) {
        int[] coreSimultaneousTasks = new int[CoreManager.coreCount];
        System.arraycopy(this.coreSimultaneousTasks, 0, coreSimultaneousTasks, 0, this.coreSimultaneousTasks.length);
        int[] idealSimultaneousTasks = new int[CoreManager.coreCount];
        System.arraycopy(this.idealSimultaneousTasks, 0, idealSimultaneousTasks, 0, this.idealSimultaneousTasks.length);

        for (Integer coreId : newCores) {
            for (Implementation impl : CoreManager.getCoreImplementations(coreId)) {
                if (canRun(impl)) {
                    idealSimultaneousTasks[coreId] = Math.max(idealSimultaneousTasks[coreId], simultaneousCapacity(impl));
                }
            }
            coreSimultaneousTasks[coreId] = Math.min(maxTaskCount, idealSimultaneousTasks[coreId]);
            if (coreSimultaneousTasks[coreId] > 0) {
                executableCores.add(coreId);
            }
        }
        this.idealSimultaneousTasks = idealSimultaneousTasks;
        this.coreSimultaneousTasks = coreSimultaneousTasks;
        return this.coreSimultaneousTasks;
    }

    public void setResourceCoreLinks(int[] idealSimTasks) {
        idealSimultaneousTasks = idealSimTasks;
        executableCores.clear();
        for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
            coreSimultaneousTasks[coreId] = Math.min(maxTaskCount, idealSimTasks[coreId]);
            if (coreSimultaneousTasks[coreId] > 0) {
                executableCores.add(coreId);
            }
        }
    }

    public int[] getIdealSimultaneousTasks() {
        return this.idealSimultaneousTasks;
    }

    public int[] getSimultaneousTasks() {
        return coreSimultaneousTasks;
    }

    public boolean canRunNow(ResourceDescription consumption) {
        return taskCount < maxTaskCount && this.checkResource(consumption);
    }

    public void endTask(ResourceDescription consumption) {
        taskCount--;
        releaseResource(consumption);
    }

    public void runTask(ResourceDescription consumption) {
        taskCount++;
        reserveResource(consumption);
    }

    public Integer simultaneousCapacity(Implementation impl) {
        return Math.min(fitCount(impl), maxTaskCount);
    }

    public abstract int compareTo(Resource t);

    public abstract Type getType();

    public abstract String getMonitoringData(String prefix);

    public abstract boolean canRun(Implementation implementation);

    public abstract void update(ResourceDescription resDesc);

    public abstract boolean isAvailable(ResourceDescription rd);

    public abstract boolean markToRemove(ResourceDescription rd);

    public abstract void confirmRemoval(ResourceDescription modification);

    //Internal private methods depending on the resourceType
    abstract Integer fitCount(Implementation impl);

    abstract boolean checkResource(ResourceDescription consumption);

    abstract void reserveResource(ResourceDescription consumption);

    abstract void releaseResource(ResourceDescription consumption);

    public String getResourceLinks(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(getName()).append("\n");
        sb.append(prefix).append("\t Executable Cores:").append(executableCores).append("\n");
        sb.append(prefix).append("\t coreSimultaneousTasks: [").append(coreSimultaneousTasks[0]);
        for (int i = 1; i < CoreManager.coreCount; i++) {
            sb.append(", ").append(coreSimultaneousTasks[i]);
        }
        sb.append("]\n");
        return sb.toString();
    }
}
