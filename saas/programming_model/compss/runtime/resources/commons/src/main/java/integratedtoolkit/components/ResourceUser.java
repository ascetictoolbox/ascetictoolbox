package integratedtoolkit.components;

import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.types.resources.updates.ResourceUpdate;

public interface ResourceUser {

    public void updatedResource(Worker<?> r, ResourceUpdate modification);

}
