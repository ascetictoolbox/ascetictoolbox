package eu.ascetic.asceticarchitecture.paas.component.loadinjector.service;

import java.io.FileInputStream;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.loadinjector.datatype.HttpMethodType;
import eu.ascetic.asceticarchitecture.paas.component.loadinjector.interfaces.LoadInjectorInterface;

public class LoadInjectorService implements LoadInjectorInterface {

	
	private static final Logger logger = Logger.getLogger(LoadInjectorService.class);
	private static final String log_pre = "# PaaS Load Injector# ";
	private String serverPath;
	private String serverurl;
	private String propertyFile;
	private String jmxFilePath;
	
	@Override
	public void configureLoadInjector(String serverPath, String serverurl, String propertyFile,
			String jmxFilePath) {
		this.serverPath=serverPath;
		this.serverurl=serverurl;
		this.propertyFile=propertyFile;
		this.jmxFilePath=jmxFilePath;

	}

	@Override
	public boolean runTestFromFile(String filename) {
		
		logwrite("Running test from file ");
        StandardJMeterEngine jmeterEngine = new StandardJMeterEngine(serverurl);
        
        JMeterUtils.loadJMeterProperties(serverPath+propertyFile);
        JMeterUtils.setJMeterHome(serverPath);
        logwrite("Settings loaded ");
        //JMeterUtils.initLogging();
        JMeterUtils.initLocale();
        try {
			SaveService.loadProperties();
			 // TODO load file from stream?? as it is xml it could be retrieved from a special service
	        FileInputStream in = new FileInputStream(jmxFilePath+"/"+propertyFile);
	        HashTree testPlanTree = SaveService.loadTree(in);
	        in.close();
	        logwrite("Stript loaded");
	        // Run JMeter Test
	        jmeterEngine.configure(testPlanTree);
	        jmeterEngine.run();
	        logwrite("Scrit exectution started");
	        
		} catch (Exception e) {
			logwrite("Something went wrong while loading the test plan file ");
			e.printStackTrace();
			return false;
		}
     

		return true;
	}

	@Override
	public boolean runTestFromJunitClass(String classname) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean runHttpRequestTest(String ip, String port, String path,
			HttpMethodType method) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void logwrite(String msg){
		logger.info(log_pre+msg);
	}

	@Override
	public boolean deployTrainingForApplication(String applicationid,
			String deploymentid) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean deployTrainingForApplicationEvent(String applicationid,
			String deploymentid, String eventid) {
		// TODO Auto-generated method stub
		return true;
	}

}
