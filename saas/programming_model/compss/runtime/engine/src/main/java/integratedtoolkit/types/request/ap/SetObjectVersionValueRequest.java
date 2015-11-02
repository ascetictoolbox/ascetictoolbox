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

package integratedtoolkit.types.request.ap;

import integratedtoolkit.components.impl.DataInfoProvider;
import integratedtoolkit.components.impl.TaskAnalyser;
import integratedtoolkit.components.impl.TaskDispatcher;

public class SetObjectVersionValueRequest extends APRequest {

    private String renaming;
    private Object value;

    public SetObjectVersionValueRequest(String renaming, Object value) {
        this.renaming = renaming;
        this.value = value;
    }

    public String getRenaming() {
        return renaming;
    }

    public void setRenaming(String renaming) {
        this.renaming = renaming;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public void process(TaskAnalyser ta, DataInfoProvider dip, TaskDispatcher td) {
        dip.setObjectVersionValue(renaming, value);
    }

    @Override
    public APRequestType getRequestType() {
        return APRequestType.SET_OBJECT_VERSION_VALUE;
    }

}
