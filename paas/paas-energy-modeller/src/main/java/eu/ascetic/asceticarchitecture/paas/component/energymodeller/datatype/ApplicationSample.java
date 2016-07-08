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
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype;

public class ApplicationSample {

	/**
	 * 
	 *  THis class represent a generic sample where e_value is the energy accumulated, p instant power and c cpu percentage, the sample referes to the time specified
	 *  by the variable time
	 * 
	 */
	
	
	private int orderID;	
	private String provid;	
	private String vmid;
	private String appid;
	private long time;
	private double e_value;
	private double p_value;
	private double c_value;
	

	public int getOrderID() {
		return orderID;
	}
	public void setOrderID(int orderID) {
		this.orderID = orderID;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public double getE_value() {
		return e_value;
	}
	public void setE_value(double e_value) {
		this.e_value = e_value;
	}
	public double getP_value() {
		return p_value;
	}
	public void setP_value(double p_value) {
		this.p_value = p_value;
	}
	public String getProvid() {
		return provid;
	}
	public void setProvid(String provid) {
		this.provid = provid;
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
	public double getC_value() {
		return c_value;
	}
	public void setC_value(double c_value) {
		this.c_value = c_value;
	}	
	public String export(){	
		return this.getProvid() + "," + this.getAppid() + ","+ this.getVmid() + "," +this.getTime()+"," + this.getC_value() + "," + this.getE_value() + "," + this.getP_value() ;	
	}	
	public String toString(){		
		return "Provider  "+ this.getProvid() + "App. Sample  "+ this.getAppid() + " VM "+ this.getVmid() + " Time " +this.getTime()+ " CPU " + this.getC_value() + " Energy " + this.getE_value() + " Power " + this.getP_value() ;	
	}
	
}
