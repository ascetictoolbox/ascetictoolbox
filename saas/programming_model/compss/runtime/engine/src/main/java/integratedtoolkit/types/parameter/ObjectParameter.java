/**
 *
 *   Copyright 2015-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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
