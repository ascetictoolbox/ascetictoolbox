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
