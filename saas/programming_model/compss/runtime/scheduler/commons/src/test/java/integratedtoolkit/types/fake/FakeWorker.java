package integratedtoolkit.types.fake;

import java.util.HashMap;

import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.resources.Resource;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.types.resources.WorkerResourceDescription;

public class FakeWorker<T extends WorkerResourceDescription> extends Worker<T> {

    public FakeWorker(WorkerResourceDescription description, int limitOfTasks) {
        super("a", (T) description, new FakeNode(), limitOfTasks, null);
    }

    @Override
    public Resource.Type getType() {
        return Resource.Type.WORKER;
    }

    @Override
    public int compareTo(Resource rsrc) {
        return 0;
    }

    @Override
    public String getMonitoringData(String prefix) {
        return "";
    }

    @Override
    public boolean canRun(Implementation<?> implementation) {
        return true;
    }

    @Override
    public Integer fitCount(Implementation<?> impl) {
        return 10;
    }

    @Override
    public boolean hasAvailable(WorkerResourceDescription consumption) {
        return true;
    }

    @Override
    public T reserveResource(T consumption) {
        return consumption;
    }

    @Override
    public void releaseResource(WorkerResourceDescription consumption) {

    }

    @Override
    public void releaseAllResources() {

    }

    @Override
    public Worker<T> getSchedulingCopy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
