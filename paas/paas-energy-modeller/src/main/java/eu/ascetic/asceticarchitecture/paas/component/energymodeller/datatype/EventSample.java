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

public class EventSample {

	/**
	 * 
	 * This class represent an event registered by the applciation monitor with additional information such as the energy accumulated (e_value), the average instant power (p_value) and the average cpu load (c_value)
	 * it also include the time it begun and the time it ended
	 * 
	 */
	
	private String vmid;
	private String appid;
	private String eventid;
	private long timestampBeging;
	private long timestampEnd;
	private double evalue;
	private double pvalue;
	private double cvalue;

	
	
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getEventid() {
		return eventid;
	}
	public void setEventid(String eventid) {
		this.eventid = eventid;
	}
	public double getEvalue() {
		return evalue;
	}
	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}
	public double getPvalue() {
		return pvalue;
	}
	public void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}
	public double getCvalue() {
		return cvalue;
	}
	public void setCvalue(double cvalue) {
		this.cvalue = cvalue;
	}
	public long getTimestampBeging() {
		return timestampBeging;
	}
	public void setTimestampBeging(long timestampBeging) {
		this.timestampBeging = timestampBeging;
	}
	public long getTimestampEnd() {
		return timestampEnd;
	}
	public void setTimestampEnd(long timestampEnd) {
		this.timestampEnd = timestampEnd;
	}
	
	public String getVmid() {
		return vmid;
	}
	public void setVmid(String vmid) {
		this.vmid = vmid;
	}
	public String export(){
		return this.getVmid()+"," + this.getEventid()+","+this.getTimestampBeging()+","+this.getTimestampEnd()+","+this.getPvalue()+","+this.getEvalue();
	}	
	
	public String toString(){
		return " VMid "+this.getVmid()+" eventid " + this.getEventid()+" power "+this.getPvalue()+" energy "+this.getEvalue()+" tsbegin "+this.getTimestampBeging()+" tsend "+this.getTimestampEnd();
	}
}
