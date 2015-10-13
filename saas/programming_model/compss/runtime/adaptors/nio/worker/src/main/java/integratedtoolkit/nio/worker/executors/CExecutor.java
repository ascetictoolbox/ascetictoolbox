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

package integratedtoolkit.nio.worker.executors;

import integratedtoolkit.nio.NIOTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CExecutor extends ExternalExecutor {

    public ArrayList<String> getLaunchCommand(NIOTask nt) {
        ArrayList<String> lArgs = new ArrayList<String>();
        lArgs.add(nt.appDir + "/worker_c");
        lArgs.add(nt.appDir);
        lArgs.add(nt.classPath);
        return lArgs;
    }

    public Map<String, String> getEnvironment(NIOTask nt) {
        //export LD_LIBRARY_PATH=$scriptDir/../../bindings/c/lib:$scriptDir/../../bindings/bindings-common/lib:$LD_LIBRARY_PATH// TODO Auto-generated method stub
        Map<String, String> env = new HashMap<String, String>();
        String ldLibraryPath = System.getenv("LD_LIBRARY_PATH");
        if (ldLibraryPath == null) {
            ldLibraryPath = nt.libPath;
        } else {
            ldLibraryPath = ldLibraryPath.concat(":" + nt.libPath);
        }
        env.put("LD_LIBRARY_PATH", nt.installDir + "/../../bindings/c/lib:" + nt.installDir + "/../../bindings/bindings-common/lib:" + System.getenv("LD_LIBRARY_PATH") + ":" + nt.libPath);
        return env;
    }
}
