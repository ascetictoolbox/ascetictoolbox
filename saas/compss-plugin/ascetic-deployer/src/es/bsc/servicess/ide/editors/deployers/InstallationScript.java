/*
 *  Copyright 2013-2014 Barcelona Supercomputing Center (www.bsc.es)
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

package es.bsc.servicess.ide.editors.deployers;

public class InstallationScript {

	private String script;
	
	public InstallationScript(){
		//script = new String("#!/bin/sh\n");
		script = new String();
	}
	
	public InstallationScript(String imageDeploymentFolder) {
		//script = new String("#!/bin/sh\n");
		script = new String();
		script = script.concat("sudo mkdir -p "+ imageDeploymentFolder + ";");		
	}

	public void addPermission(String file, String permission, boolean recursive) {
		//script = script.concat("sudo find . -name \""+file+"\" -exec chmod ");
		script = script.concat("sudo chmod ");
		if (recursive){
			script = script.concat("-R ");
		}
		//script = script.concat(permission +" {};");
		script = script.concat(permission +" "+ file+";");
		
	}
	

	public void addUnZip(String file, String imageDeploymentFolder) {
		script = script.concat("sudo unzip -o "+ file +" -d "+imageDeploymentFolder+";");
		
	}

	public void addCopy(String file, String destFolder) {
		script = script.concat("sudo cp "+ file +" "+destFolder+";");
		
	}

	public String getCommand() {
		return script;
	}
	
	public void setScript(String script){
		this.script = script;
	}
	
	
}
