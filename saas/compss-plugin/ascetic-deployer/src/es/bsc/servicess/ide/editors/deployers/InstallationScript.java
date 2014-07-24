package es.bsc.servicess.ide.editors.deployers;

public class InstallationScript {

	private String script;
	
	public InstallationScript(String imageDeploymentFolder) {
		script = new String("#!/bin/sh\n");
		script = script.concat("mkdir "+ imageDeploymentFolder + ";");		
	}

	public void addExecutablePermission(String file) {
		script = script.concat("chmod +x "+ file + ";");
	}

	public void addUnZip(String file, String imageDeploymentFolder) {
		script = script.concat("unzip "+ file +" -d "+imageDeploymentFolder+";");
		
	}

	public void addCopy(String file, String destFolder) {
		script = script.concat("cp "+ file +" "+destFolder+";");
		
	}

	public String getCommand() {
		return script;
	}
	
	
}
