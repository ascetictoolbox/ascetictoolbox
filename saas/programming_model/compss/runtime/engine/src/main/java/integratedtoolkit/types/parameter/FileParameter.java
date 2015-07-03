package integratedtoolkit.types.parameter;

import integratedtoolkit.api.ITExecution;
import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.types.parameter.Parameter.DependencyParameter;

public class FileParameter extends DependencyParameter {
    // File parameter fields

    DataLocation location;

    public FileParameter(ITExecution.ParamDirection direction, DataLocation location) {

        super(ITExecution.ParamType.FILE_T, direction);
        this.location = location;
    }

    public DataLocation getLocation() {
        return location;
    }

    public String toString() {
        return location + " "
                + getType() + " "
                + getDirection();
    }
}
