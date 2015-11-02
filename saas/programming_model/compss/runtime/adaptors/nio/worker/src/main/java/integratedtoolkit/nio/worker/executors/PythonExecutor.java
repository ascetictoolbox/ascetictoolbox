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


public class PythonExecutor extends ExternalExecutor {

	@Override
	public ArrayList<String> getLaunchCommand(NIOTask nt) {
		ArrayList<String> lArgs = new ArrayList<String>();
		String pycompssHome = nt.installDir + "/../../../Bindings/python";
		/*lArgs.add("/bin/bash");
		lArgs.add("-e");
		lArgs.add("-c");*/
		lArgs.add("python");
		lArgs.add("-u");
		lArgs.add(pycompssHome + "/pycompss/worker/worker.py");
		return lArgs;
	}

	@Override
	public Map<String, String> getEnvironment(NIOTask nt) {
		/*
		 * export PYCOMPSS_HOME=`dirname $0`/../../bindings/pythonport
		 * PYTHONPATH=$app_dir:$py_path:$PYCOMPSS_HOME
		 */
		Map<String, String> env = new HashMap<String, String>();
		String pycompssHome = nt.installDir + "/../../../Bindings/python";
		env.put("PYCOMPSS_HOME", pycompssHome);
		String pythonPath = System.getenv("PYTHONPATH");
		if (pythonPath == null) {
			pythonPath = nt.appDir + ":" + nt.classPath + ":" + pycompssHome;
		} else {
			pythonPath = pythonPath.concat(":" + nt.appDir + ":" + nt.classPath + ":" + pycompssHome);
		}
		env.put("PYTHONPATH", pythonPath);
		String ldLibraryPath = System.getenv("LD_LIBRARY_PATH");
		if (ldLibraryPath == null) {
			ldLibraryPath = nt.libPath;
		} else {
			ldLibraryPath = ldLibraryPath.concat(":" + nt.libPath);
		}
		env.put("LD_LIBRARY_PATH", ldLibraryPath);
               
                
                for (java.util.Map.Entry<String,String> entry: env.entrySet()){
                    System.out.println(entry.getKey()+"-->"+entry.getValue());
                }
		return env;
	}

}
