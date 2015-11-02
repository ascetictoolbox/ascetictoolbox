/**
 *
 *   Copyright 2014-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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
