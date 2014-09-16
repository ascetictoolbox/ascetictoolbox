/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
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


public class ExecutionParams implements Serializable {
	
	// Parameters of a concrete execution of a task
	private String user;
	private String host;
	private String installDir;
	private String workingDir;
	private String appDir;
	private String libPath;
	private int cost;
	private String queue;
	
	
	public ExecutionParams(String user,
			   String host,
			   String installDir,
			   String workingDir,
			   String appDir,
			   String libPath) {
		this(user, host, installDir, workingDir, appDir, libPath, 0, null);
	}

	public ExecutionParams(String user,
				   String host,
				   String installDir,
				   String workingDir,
				   String appDir,
				   String libPath,
				   int cost,
				   String queue) {
		this.user = user;
		this.host = host;
		this.installDir = installDir;
		this.workingDir = workingDir;
		this.appDir = appDir;
		this.libPath = libPath;
		this.cost = cost;
		this.queue = queue;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getHost() {
		return host;
	}
	
	public String getInstallDir() {
		return installDir;
	}
	
	public String getWorkingDir() {
		return workingDir;
	}
	
	public String getAppDir() {
		return appDir;
	}
	
	public String getLibPath() {
		return libPath;
	}
	
	public int getCost() {
		return cost;
	}
	
	public String getQueue() {
		return queue;
	}
	
}
