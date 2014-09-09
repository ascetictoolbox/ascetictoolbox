/*
 *  Copyright 2002-2012 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package integratedtoolkit.types;

import java.io.Serializable;

import integratedtoolkit.api.ITExecution.*;
import integratedtoolkit.types.data.DataAccessId;

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

    public static class DependencyParameter extends Parameter {

        private DataAccessId daId;
        private String dataRemotePath;

        public DependencyParameter(ParamType type, ParamDirection direction) {
            super(type, direction);
        }

        public DataAccessId getDataAccessId() {
            return daId;
        }

        public void setDataAccessId(DataAccessId daId) {
            this.daId = daId;
        }

        public String getDataRemotePath() {
            return this.dataRemotePath;
        }

        public void setDataRemotePath(String path) {
            this.dataRemotePath = path;
        }

        public static class FileParameter extends DependencyParameter {
            // File parameter fields

            private String fileName;
            private String path;
            private String host;

            public FileParameter(ParamDirection direction,
                    String name,
                    String path,
                    String host) {

                super(ParamType.FILE_T, direction);

                this.fileName = name;
                this.path = path;
                this.host = host;
            }

            public String getName() {
                return fileName;
            }

            public String getPath() {
                return path;
            }

            public String getHost() {
                return host;
            }

            public void setName(String name) {
                this.fileName = name;
            }

            public String toString() {
                return getHost() + ":"
                        + getPath()
                        + getName() + " "
                        + getType() + " "
                        + getDirection();
            }
        }

        public static class ObjectParameter extends DependencyParameter {

            private int hashCode;
            private Object value;

            public ObjectParameter(ParamDirection direction,
                    Object value,
                    int hashCode) {
                super(ParamType.OBJECT_T, direction);
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
