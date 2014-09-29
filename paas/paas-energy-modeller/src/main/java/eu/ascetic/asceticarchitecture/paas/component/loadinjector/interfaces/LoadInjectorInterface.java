/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.loadinjector.interfaces;


public interface LoadInjectorInterface {
	
	public boolean deployTrainingForApplication(String applicationid, String deploymentid);
	
	public boolean deployTrainingForApplicationEvent(String applicationid, String deploymentid, String eventid);

	public void configureLoadInjector(String serverPath,String serverurl, String propertyFile,String jmxFilePath);
	
	public boolean runTestFromFile(String filename, long timeout);
	
	public boolean runTestFromJunitClass(String classname);

	public boolean runHttpRequestTest(String ip, String port, String path);
	
}
