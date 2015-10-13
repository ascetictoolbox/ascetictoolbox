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

package integratedtoolkit.nio.master;

import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.types.data.location.URI;

public class NIOLogicalData extends LogicalData {

    public NIOLogicalData() {
        super();
    }

    public NIOLogicalData(String name) {
        super(name);
    }

    public void replicate(URI dest) {
        //Do nothing. It will be done later on, during the task execution
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Logical Data name: ").append(this.name).append(":\n");
        sb.append("\t Locations:\n");
        for (DataLocation dl : locations) {
            sb.append("\t\t * ").append(dl).append("\n");
        }
        return sb.toString();
    }
}
