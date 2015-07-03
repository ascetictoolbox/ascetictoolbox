package integratedtoolkit.types.parameter;

import java.io.Serializable;

import integratedtoolkit.api.ITExecution.*;
import integratedtoolkit.types.data.DataAccessId;
import integratedtoolkit.types.data.Transferable;

public class Parameter implements Serializable {

    // Parameter fields
    private ParamType type;
    private ParamDirection direction;

    public Parameter(ParamType type, ParamDirection direction) {
        this.type = type;
        this.direction = direction;
    }

    public ParamType getType() {
        return type;
    }

    public ParamDirection getDirection() {
        return direction;
    }

    public static class DependencyParameter extends Parameter implements Transferable {

        private DataAccessId daId;
        private Object dataSource;
        private String dataTarget;

        public DependencyParameter(ParamType type, ParamDirection direction) {
            super(type, direction);
        }

        public DataAccessId getDataAccessId() {
            return daId;
        }

        public void setDataAccessId(DataAccessId daId) {
            this.daId = daId;
        }

        public Object getDataSource() {
            return dataSource;
        }

        public void setDataSource(Object dataSource) {
            this.dataSource = dataSource;
        }

        public String getDataTarget() {
            return this.dataTarget;
        }

        public void setDataTarget(String target) {
            this.dataTarget = target;
        }

    }

    public static class BasicTypeParameter extends Parameter {
        /* Basic type parameter can be:
         * - boolean
         * - char
         * - String
         * - byte
         * - short
         * - int
         * - long
         * - float
         * - double
         */

        private Object value;

        public BasicTypeParameter(ParamType type,
                ParamDirection direction,
                Object value) {
            super(type, direction);
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String toString() {
            return value + " "
                    + getType() + " "
                    + getDirection();
        }
    }
}
