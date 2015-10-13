/**
 *
 *   Copyright 2013-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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

package integratedtoolkit.types;

import java.util.HashMap;

public abstract class COMPSsWorker extends COMPSsNode {

    public COMPSsWorker(String name, HashMap<String, String> properties) {
        super();
    }

    public abstract String getUser();

    public abstract boolean isTracingReady();

    public abstract void waitForTracingReady();

    public abstract void updateTaskCount(int processorCoreCount);

    public abstract void announceDestruction() throws Exception;

    public abstract void announceCreation() throws Exception;


}
