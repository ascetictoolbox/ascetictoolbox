/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao;

import java.util.Properties;

import javax.jms.Session;

public class EMSettings {

	private String iaasdriver="com.mysql.jdbc.Driver";
	private String iaasurl="jdbc:mysql://localhost/paasemdb";
	private String iaasdbuser="user";
	private String iaasdbpassword="user";
	
	private String paasdriver="com.mysql.jdbc.Driver";
	private String paasurl="jdbc:mysql://localhost/iaasemdb";
	private String paasdbuser="user";
	private String paasdbpassword="user";
	
	private String serverPath="C:/Users/sommacam/Desktop/apache-jmeter-2.11";
	private String serverurl="10.15.5.110";
	private String propertyFile="C:/Users/sommacam/Desktop/apache-jmeter-2.11/bin/jmeter.properties";
	private String jmxFilePath="c:/test";
	
	private String appmonitor="http://localhost:9000/query";
	
	// Queue management
	private String enableQueue="false";
	
	private String monitoringQueueTopic="PEM.ENERGY";
	
	private String amqpUser="admin";
	private String amqpPassword="admin";
	private String amqpUrl="tcp://localhost:61616";
	
	

	public EMSettings() {
		
	}

	
	public EMSettings(Properties props) {
		
		this.setIaasdriver(props.getProperty("iaasdriver"));
		this.setIaasurl(props.getProperty("iaasurl"));
		this.setIaasdbpassword(props.getProperty("iaasdbpassword"));
		this.setIaasdbuser(props.getProperty("iaasdbuser"));
		this.setPaasdriver(props.getProperty("paasdriver"));
		this.setPaasurl(props.getProperty("paasurl"));
		this.setPaasdbpassword(props.getProperty("paasdbpassword"));
		this.setPaasdbuser(props.getProperty("paasdbuser"));
		this.setServerPath(props.getProperty("serverpath"));
		this.setServerurl(props.getProperty("serverurl"));
		this.setPropertyFile((props.getProperty("propertyfile")));
		this.setJmxFilePath((props.getProperty("jmxfilepath")));
		this.setAppmonitor((props.getProperty("appmonitor")));
		this.setAmqpUser(props.getProperty("amqpUser"));
		this.setAmqpPassword(props.getProperty("amqpPassword"));		
		this.setAmqpUrl(props.getProperty("amqpUrl"));
		this.setMonitoringQueueTopic(props.getProperty("monitoringQueueTopic"));
		this.setEnableQueue(props.getProperty("enableQueue"));
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


	public String getIaasdriver() {
		return iaasdriver;
	}


	public void setIaasdriver(String iaasdriver) {
		this.iaasdriver = iaasdriver;
	}


	public String getIaasurl() {
		return iaasurl;
	}


	public void setIaasurl(String iaasurl) {
		this.iaasurl = iaasurl;
	}


	public String getPaasdriver() {
		return paasdriver;
	}


	public void setPaasdriver(String paasdriver) {
		this.paasdriver = paasdriver;
	}


	public String getPaasurl() {
		return paasurl;
	}


	public void setPaasurl(String paasurl) {
		this.paasurl = paasurl;
	}


	public String getServerPath() {
		return serverPath;
	}


	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}


	public String getServerurl() {
		return serverurl;
	}


	public void setServerurl(String serverurl) {
		this.serverurl = serverurl;
	}


	public String getPropertyFile() {
		return propertyFile;
	}


	public void setPropertyFile(String propertyFile) {
		this.propertyFile = propertyFile;
	}


	public String getJmxFilePath() {
		return jmxFilePath;
	}


	public void setJmxFilePath(String jmxFilePath) {
		this.jmxFilePath = jmxFilePath;
	}


	public String getAppmonitor() {
		return appmonitor;
	}


	public void setAppmonitor(String appmonitor) {
		this.appmonitor = appmonitor;
	}


	public String getMonitoringQueueTopic() {
		return monitoringQueueTopic;
	}


	public void setMonitoringQueueTopic(String monitoringQueueTopic) {
		this.monitoringQueueTopic = monitoringQueueTopic;
	}

	public String getAmqpUser() {
		return amqpUser;
	}


	public void setAmqpUser(String amqpUser) {
		this.amqpUser = amqpUser;
	}


	public String getAmqpPassword() {
		return amqpPassword;
	}


	public void setAmqpPassword(String amqpPassword) {
		this.amqpPassword = amqpPassword;
	}


	public String getAmqpUrl() {
		return amqpUrl;
	}


	public void setAmqpUrl(String amqpUrl) {
		this.amqpUrl = amqpUrl;
	}


	public String getEnableQueue() {
		return enableQueue;
	}


	public void setEnableQueue(String enableQueue) {
		this.enableQueue = enableQueue;
	}

	
	
	
}
