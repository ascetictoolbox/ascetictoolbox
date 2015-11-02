package eu.ascetic.paas.slam.pac.applicationmanager.model;

import static eu.ascetic.paas.slam.pac.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Basic XML representation for any Application Sample coming from the ASCETiC Energy Modeller
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "application-sample", namespace = APPLICATION_MANAGER_NAMESPACE)
public class ApplicationSample {
	@XmlElement(name = "orderID", namespace = APPLICATION_MANAGER_NAMESPACE)
	private int orderID;
	@XmlElement(name = "vmid", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String vmid;
	@XmlElement(name = "appid", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String appid;
	@XmlElement(name = "time", namespace = APPLICATION_MANAGER_NAMESPACE)
	private long time;
	@XmlElement(name = "evalue", namespace = APPLICATION_MANAGER_NAMESPACE)
	private double eValue;
	@XmlElement(name = "pvalue", namespace = APPLICATION_MANAGER_NAMESPACE)
	private double pValue;
	@XmlElement(name = "cvalue", namespace = APPLICATION_MANAGER_NAMESPACE)
	private double cValue;
	
	public int getOrderID() {
		return orderID;
	}
	public void setOrderID(int orderID) {
		this.orderID = orderID;
	}
	
	public String getVmid() {
		return vmid;
	}
	public void setVmid(String vmid) {
		this.vmid = vmid;
	}
	
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
	public double geteValue() {
		return eValue;
	}
	public void seteValue(double eValue) {
		this.eValue = eValue;
	}
	
	public double getpValue() {
		return pValue;
	}
	public void setpValue(double pValue) {
		this.pValue = pValue;
	}
	
	public double getcValue() {
		return cValue;
	}
	public void setcValue(double cValue) {
		this.cValue = cValue;
	}
}
