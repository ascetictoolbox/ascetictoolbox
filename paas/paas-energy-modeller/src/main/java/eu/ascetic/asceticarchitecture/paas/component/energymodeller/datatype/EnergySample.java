/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype;

public class EnergySample {

	private String vmid;
	private String eventid;
	private long timestampBeging;
	private long timestampEnd;
	private double e_value;
	private double p_value;


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
	public String getEventid() {
		return eventid;
	}
	public void setEventid(String eventid) {
		this.eventid = eventid;
	}	
}
