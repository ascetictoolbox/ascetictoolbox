package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client;

import java.util.List;


public class GenericPricingMessage {
	
		public enum Unit { PRICEHOUR, CHARGES, TOTALCHARGES };
	
	//	private int IaaSprovider;
	//	private String applicationid;
		protected int	deploymentid;
	//	private int	eventid;
	//	private int schemeid;
		protected Unit unit;
		protected double value;
	//	private List<Integer> VMids;
	//	private double energyPrice;
	//	private int VMid;
		
		public GenericPricingMessage (){

		}
		
	/*	public void setVMMessage(int deplID, int VMid, int schemeID, Unit unit, double value){
			deploymentid = deplID;
			this.schemeid = schemeID;
			this.unit = unit;
			this.value = value;
			this.VMid = VMid;
		}*/
		
	/*	public void setAppMessage(int deplID, Unit unit, double value, List<Integer> VMs){
			deploymentid = deplID;
			this.unit = unit;
			this.value = value;
			this.VMids = VMs;
		}
		
		public int getProvider() {
			return IaaSprovider;
		}
		public void setProvider(int provider) {
			this.IaaSprovider = provider;
		}
		public List<Integer> getVms() {
			return VMids;
		}
		public void setVms(List<Integer> vms) {
			this.VMids = vms;
		}
		public String getApplicationid() {
			return applicationid;
		}
		public void setApplicationid(String applicationid) {
			this.applicationid = applicationid;
		}
		public int getEventid() {
			return eventid;
		}
		public void setEventid(int eventid) {
			this.eventid = eventid;
		}*/
		public Unit getUnit() {
			return unit;
		}
		public void setUnit(Unit unit) {
			this.unit = unit;
		}
	
		public double getValue() {
			return value;
		}
		public void setValue(double value) {
			this.value = value;
			}
	
		public int getDeploymentid() {
			return deploymentid;
		}
		public void setDeploymentid(int VMid) {
			this.deploymentid = VMid;
		}
	/*	public int getVMid() {
			return VMid;
		}
		public void setVMid(int deploymentid) {
			this.VMid = deploymentid;
		}
		
	/*	public int getSchemeid() {
			return schemeid;
		}
		public void setSchemeid(int schemeid) {
			this.schemeid = schemeid;
		}
		
		public double getEnergyPrice() {
			return energyPrice;
		}
		public void setEnergyPrice(double value) {
			this.energyPrice = value;
		}
		*/
	
	}

