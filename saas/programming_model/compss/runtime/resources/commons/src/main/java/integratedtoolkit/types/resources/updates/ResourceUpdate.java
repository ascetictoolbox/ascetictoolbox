package integratedtoolkit.types.resources.updates;

import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.resources.ResourceDescription;
import java.util.LinkedList;

public abstract class ResourceUpdate< T extends ResourceDescription> {

    public static enum Type {

        INCREASE,
        REDUCE
    }
    private final T modification;
    private final LinkedList<Implementation> compatibleImplementations;

    protected ResourceUpdate(T modification, LinkedList<Implementation> impls) {
        this.modification = modification;
        this.compatibleImplementations = impls;
    }

    public abstract Type getType();

    public final T getModification() {
        return modification;
    }

    public final LinkedList<Implementation> getCompatibleImplementations() {
        return compatibleImplementations;
    }

    public abstract boolean checkCompleted();

    public abstract void waitForCompletion() throws InterruptedException;

}
