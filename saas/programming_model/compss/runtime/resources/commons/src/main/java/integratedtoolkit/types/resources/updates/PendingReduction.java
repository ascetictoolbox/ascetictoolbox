package integratedtoolkit.types.resources.updates;

import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.resources.ResourceDescription;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class PendingReduction<T extends ResourceDescription> extends ResourceUpdate< T> {

    private final Semaphore sem;

    public PendingReduction(T reduction, LinkedList<Implementation> impls) {
        super(reduction, impls);
        this.sem = new Semaphore(0);
    }

    @Override
    public Type getType() {
        return Type.REDUCE;
    }

    @Override
    public boolean checkCompleted() {
        return sem.tryAcquire();
    }

    @Override
    public void waitForCompletion() throws InterruptedException {
        sem.acquire();
    }

    public void notifyCompletion() {
        sem.release();
    }
}
