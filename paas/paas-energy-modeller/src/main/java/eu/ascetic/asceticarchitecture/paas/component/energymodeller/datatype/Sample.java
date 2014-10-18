/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype;

public class Sample {

	private String vmid;
	private long timestampBeging;
	private long timestampEnd;
	private double value;

	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
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
	
}
