package eu.ascetic.saas.experimentmanager.models;

public class LogicalComponent extends Component {

	public LogicalComponent(){
		
	}
	
	public LogicalComponent(String name, String installationscript){
		super(name);
		this.installationscript= installationscript;
	}
	
	public String getInstallationscript() {
		return installationscript;
	}

	public void setInstallationscript(String installationscript) {
		this.installationscript = installationscript;
	}

	private String installationscript;

	@Override
	public String getType() {
		return "logical";
	}


	
}
