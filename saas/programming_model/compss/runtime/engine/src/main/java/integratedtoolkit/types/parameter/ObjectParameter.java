package integratedtoolkit.types.parameter;

import integratedtoolkit.api.ITExecution;
import integratedtoolkit.types.parameter.Parameter.DependencyParameter;

public class ObjectParameter extends DependencyParameter {

    private int hashCode;
    private Object value;

    public ObjectParameter(ITExecution.ParamDirection direction,
            Object value,
            int hashCode) {
        super(ITExecution.ParamType.OBJECT_T, direction);
        this.value = value;
        this.hashCode = hashCode;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getCode() {
        return hashCode;
    }

    public String toString() {
        return "OBJECT: hash code " + hashCode;
    }
}
