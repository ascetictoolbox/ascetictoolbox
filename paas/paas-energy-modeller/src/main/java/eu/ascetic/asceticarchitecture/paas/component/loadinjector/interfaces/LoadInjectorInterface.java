package eu.ascetic.asceticarchitecture.paas.component.loadinjector.interfaces;

import eu.ascetic.asceticarchitecture.paas.component.loadinjector.datatype.HttpMethodType;

public interface LoadInjectorInterface {
	
	public boolean deployTrainingForApplication(String applicationid, String deploymentid);
	
	public boolean deployTrainingForApplicationEvent(String applicationid, String deploymentid, String eventid);

	public void configureLoadInjector(String serverPath,String serverurl, String propertyFile,String jmxFilePath);
	
	public boolean runTestFromFile(String filename);
	
	public boolean runTestFromJunitClass(String classname);
	
	// TODO this data will be directly retrieveb by PaaS LI
	public boolean runHttpRequestTest(String ip, String port, String path,HttpMethodType method);
	
}
