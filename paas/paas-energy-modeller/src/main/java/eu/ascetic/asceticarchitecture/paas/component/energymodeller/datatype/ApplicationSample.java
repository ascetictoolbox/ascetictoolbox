/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype;

public class ApplicationSample {

	private int orderID;
	private String vmid;
	private String eventid;
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
	public double getC_value() {
		return c_value;
	}
	public void setC_value(double c_value) {
		this.c_value = c_value;
	}	
	
}
