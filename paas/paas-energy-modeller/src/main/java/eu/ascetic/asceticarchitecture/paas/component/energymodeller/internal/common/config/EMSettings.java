/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.config;

import java.util.Properties;

public class EMSettings {

	
	// PaaS DB params
	private String paasdriver="com.mysql.jdbc.Driver";
	private String paasurl="jdbc:mysql://localhost/paasemdb";
	private String paasdbuser="user";
	private String paasdbpassword="user";
	
	// app monitor setting
	private String appmonitor="http://localhost:9000/query";
	
	// PaaS activemq queue management
	private String enableQueue="false";
		
	private String amqpUser="admin";
	private String amqpPassword="admin";
	private String amqpUrl="tcp://localhost:61616";
	// EM topic I publish
	private String monitoringQueueTopic="PEM.ENERGY";
	// AM topics I subscribe
	private String amanagertopic="APPLICATION.*.DEPLOYMENT.*.VM.*.*";
	// PaaS energy measurement I subscribe
	// private String powertopic="vm.*.item.*";
	private String powertopic="APPLICATION.*.DEPLOYMENT.*.VM.*.METRIC.*";	
	private String powerfromvmtopic="PROVIDER.*.APPLICATION.*.DEPLOYMENT.*.VM.*.FROMVM";	
	
	// 	temporarly for IaaS connection
	private String enableIaasQueue="false";
	private String iaasAmqpUser="admin";
	private String iaasAmqpPassword="admin";
	private String iaasAmqpUrl="tcp://localhost:61616";	
	
	// PaaS Predictor
	private String predictorType="basic";
	
	// PaaS params
	private String providerIdDefault="00000";

	// IaaS power data
	private String enablePowerFromIaas="true";	
	
	
	public EMSettings() {
		
	}

	
	public EMSettings(Properties props) {

		this.setPaasdriver(props.getProperty("paasdriver"));
		this.setPaasurl(props.getProperty("paasurl"));
		this.setPaasdbpassword(props.getProperty("paasdbpassword"));
		this.setPaasdbuser(props.getProperty("paasdbuser"));
		this.setAppmonitor((props.getProperty("appmonitor")));
		this.setAmqpUser(props.getProperty("amqpUser"));
		this.setAmqpPassword(props.getProperty("amqpPassword"));		
		this.setAmqpUrl(props.getProperty("amqpUrl"));
		this.setMonitoringQueueTopic(props.getProperty("monitoringQueueTopic"));
		this.setEnableQueue(props.getProperty("enableQueue"));
		this.setAmanagertopic(props.getProperty("amanagertopic"));
		this.setPowertopic(props.getProperty("powertopic"));
		this.setPowerFromVMtopic(props.getProperty("powerfromvmtopic"));
		this.setEnableIaasQueue(props.getProperty("enableIaasQueue"));
		this.setIaasAmqpUser(props.getProperty("iaasAmqpUser"));
		this.setIaasAmqpPassword(props.getProperty("iaasAmqpPassword"));
		this.setIaasAmqpUrl(props.getProperty("iaasAmqpUrl"));		
		this.setPredictorType(props.getProperty("predictorType"));
		this.setProviderIdDefault(props.getProperty("providerIdDefault"));
		this.setEnablePowerFromIaas(props.getProperty("enablePowerFromIaas"));		
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


	public String getAmanagertopic() {
		return amanagertopic;
	}


	public void setAmanagertopic(String amanagertopic) {
		this.amanagertopic = amanagertopic;
	}


	public String getPowertopic() {
		return powertopic;
	}


	public void setPowertopic(String powertopic) {
		this.powertopic = powertopic;
	}


	public String getPowerFromVMtopic() {
		return powerfromvmtopic;
	}


	public void setPowerFromVMtopic(String powerfromvmtopic) {
		this.powerfromvmtopic = powerfromvmtopic;
	}
	
	
	public String getEnableIaasQueue() {
		return enableIaasQueue;
	}


	public void setEnableIaasQueue(String enableIaasQueue) {
		this.enableIaasQueue = enableIaasQueue;
	}


	public String getIaasAmqpUser() {
		return iaasAmqpUser;
	}


	public void setIaasAmqpUser(String iaasAmqpUser) {
		this.iaasAmqpUser = iaasAmqpUser;
	}


	public String getIaasAmqpPassword() {
		return iaasAmqpPassword;
	}


	public void setIaasAmqpPassword(String iaasAmqpPassword) {
		this.iaasAmqpPassword = iaasAmqpPassword;
	}


	public String getIaasAmqpUrl() {
		return iaasAmqpUrl;
	}


	public void setIaasAmqpUrl(String iaasAmqpUrl) {
		this.iaasAmqpUrl = iaasAmqpUrl;
	}
		
	
	public String getPredictorType() {
		return predictorType;
	}


	public void setPredictorType(String predictorType) {
		this.predictorType = predictorType;
	}

	
	public String getProviderIdDefault() {
		return providerIdDefault;
	}
	

	public void setProviderIdDefault(String providerIdDefault) {
		this.providerIdDefault = providerIdDefault;
	}
	
	
	public String getEnablePowerFromIaas() {
		return enablePowerFromIaas;
	}
	

	public void setEnablePowerFromIaas(String enablePowerFromIaas) {
		this.enablePowerFromIaas = enablePowerFromIaas;
	}
}
