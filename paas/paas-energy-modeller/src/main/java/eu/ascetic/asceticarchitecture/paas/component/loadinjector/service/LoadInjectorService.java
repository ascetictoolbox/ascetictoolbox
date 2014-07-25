package eu.ascetic.asceticarchitecture.paas.component.loadinjector.service;

import java.io.FileInputStream;

import org.apache.jmeter.engine.ClientJMeterEngine;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.loadinjector.interfaces.LoadInjectorInterface;

public class LoadInjectorService implements LoadInjectorInterface {

	
	private static final Logger logger = Logger.getLogger(LoadInjectorService.class);
	private static final String log_pre = "# PaaS Load Injector log system: ";
	private String serverPath="C:/Users/sommacam/Desktop/apache-jmeter-2.11";
	private String serverurl="10.15.5.115";
	private String propertyFile="/bin/jmeter.properties";
	private String jmxFilePath="c:/test";
	
	@Override
	public void configureLoadInjector(String serverPath, String serverurl, String propertyFile,
			String jmxFilePath) {
		this.serverPath=serverPath;
		this.serverurl=serverurl;
		this.propertyFile=propertyFile;
		this.jmxFilePath=jmxFilePath;

	}

	@Override
	public boolean runTestFromFile(String filename, long timeout) {
		
		logwrite("Running test from file ");
        try {

            ClientJMeterEngine client = new ClientJMeterEngine(serverurl);	
	        JMeterUtils.loadJMeterProperties(serverPath+propertyFile);
	        JMeterUtils.setJMeterHome(serverPath);	        
	        logwrite("Settings loaded ");       
	        JMeterUtils.initLogging();
	        JMeterUtils.initLocale();
	        
	        logwrite("Init ok ");
	        
			SaveService.loadProperties();
			logwrite("Loading test ");
	        FileInputStream in = new FileInputStream(jmxFilePath+"/"+filename);

	        HashTree testPlanTree = SaveService.loadTree(in);
	       
	        in.close();
	      
	        client.configure(testPlanTree);
	        client.runTest();
	        
	        logwrite("Scrit exectution started, waiting its termination");
	        
	        try {
	            Thread.sleep(timeout);
	        } catch(InterruptedException ex) {
	            Thread.currentThread().interrupt();
	        }

	        logwrite("Script exectution time elapsed");
	        client.stopTest(true);
	        logwrite("Script terminated");
	        
	        
        } catch (Exception e) {
			logwrite("Something went wrong while loading the test plan file ");
			e.printStackTrace();
			return false;
		}
     

		return true;
	}
	
	@Override
	public boolean deployTrainingForApplication(String applicationid,String deploymentid) {
		// TODO Auto-generated method stub
		//runTestFromFile();
		return true;
	}

	@Override
	public boolean deployTrainingForApplicationEvent(String applicationid,String deploymentid, String eventid) {
		// TODO Auto-generated method stub
		//runTestFromFile();
		return true;
	}


	@Override
	public boolean runTestFromJunitClass(String classname) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean runHttpRequestTest(String ip, String port, String path) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void logwrite(String msg){
		logger.info(log_pre+msg);
	}


}
