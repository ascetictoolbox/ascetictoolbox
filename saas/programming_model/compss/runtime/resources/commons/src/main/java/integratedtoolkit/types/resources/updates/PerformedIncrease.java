package integratedtoolkit.types.resources.updates;

import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.resources.ResourceDescription;
import java.util.LinkedList;

public class PerformedIncrease<T extends ResourceDescription> extends ResourceUpdate<T> {

    public PerformedIncrease(T increase, LinkedList<Implementation> compatibleImplementations) {
        super(increase, compatibleImplementations);
    }

    @Override
    public Type getType() {
        return Type.INCREASE;
    }

    @Override
    public boolean checkCompleted() {
        return true;
    }

    @Override
    public void waitForCompletion() throws InterruptedException {
        //Do nothing. Already completed
    }

    public void notifyCompletion() {
        //Do nothing. Already completed.
    }
}
