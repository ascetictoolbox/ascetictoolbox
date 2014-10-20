/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype;

public class Sample {

	private String vmid;
	private long timestampBeging;
	private long timestampEnd;
	private double evalue;
	private double pvalue;
	private double cvalue;

	
	
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
	
}
