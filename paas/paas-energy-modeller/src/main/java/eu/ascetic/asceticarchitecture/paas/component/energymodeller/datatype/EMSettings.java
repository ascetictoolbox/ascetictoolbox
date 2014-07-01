package eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype;

import java.util.Properties;

public class EMSettings {

	private String iaasdb="localhost";
	private String iaasportport="3306";
	private String iaasdbuser="user";
	private String iaasdbpassword="user";
	private String iaasdatabase="iaasdb";
	
	private String paasdb="localhost";
	private String paasportport="3306";
	private String paasdbuser="user";
	private String paasdbpassword="user";
	private String paasdatabase="paasdb";
	
	public EMSettings() {
		
	}
	
	
	public EMSettings(Properties props) {
		
		this.setPaasdb(props.getProperty("paasdb"));
		this.setPaasportport(props.getProperty("paasportport"));
		this.setPaasdbpassword(props.getProperty("paasdbuser"));
		this.setPaasdbuser(props.getProperty("paasdbpassword"));
		this.setPaasportport(props.getProperty("paasdatabase"));
		
		props.getProperty("iaasdb");
		props.getProperty("iaasportport");
		props.getProperty("iaasdbuser");
		props.getProperty("iaasdbpassword");
		props.getProperty("iaasdatabase");
		
	}

	public String getIaasdb() {
		return iaasdb;
	}

	public void setIaasdb(String iaasdb) {
		this.iaasdb = iaasdb;
	}

	public String getIaasportport() {
		return iaasportport;
	}

	public void setIaasportport(String iaasportport) {
		this.iaasportport = iaasportport;
	}

	public String getIaasdbuser() {
		return iaasdbuser;
	}

	public void setIaasdbuser(String iaasdbuser) {
		this.iaasdbuser = iaasdbuser;
	}

	public String getIaasdbpassword() {
		return iaasdbpassword;
	}

	public void setIaasdbpassword(String iaasdbpassword) {
		this.iaasdbpassword = iaasdbpassword;
	}

	public String getIaasdatabase() {
		return iaasdatabase;
	}

	public void setIaasdatabase(String iaasdatabase) {
		this.iaasdatabase = iaasdatabase;
	}

	public String getPaasdb() {
		return paasdb;
	}

	public void setPaasdb(String paasdb) {
		this.paasdb = paasdb;
	}

	public String getPaasportport() {
		return paasportport;
	}

	public void setPaasportport(String paasportport) {
		this.paasportport = paasportport;
	}

	public String getPaasdbuser() {
		return paasdbuser;
	}

	public void setPaasdbuser(String paasdbuser) {
		this.paasdbuser = paasdbuser;
	}

	public String getPaasdbpassword() {
		return paasdbpassword;
	}

	public void setPaasdbpassword(String paasdbpassword) {
		this.paasdbpassword = paasdbpassword;
	}

	public String getPaasdatabase() {
		return paasdatabase;
	}

	public void setPaasdatabase(String paasdatabase) {
		this.paasdatabase = paasdatabase;
	}
	
	
}
