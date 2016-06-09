package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client;

import java.util.ArrayList;
import java.util.List;

public class GenericPricingMessage {
		
		private int IaaSprovider;
		private String applicationid;
		private int	deploymentid;
		private int schemeid;

		private double energyPrice;
		
		private double charges;
		
		public GenericPricingMessage (){

		}
		
		public GenericPricingMessage (int deploymentid, int schemeid, double charges){
			this.deploymentid = deploymentid;
			this.schemeid= schemeid;
			this.charges = charges;
		}
		
		public GenericPricingMessage (int deploymentid, double charges){
			this.deploymentid = deploymentid;
			this.charges = charges;
		}
		
		public int getProvider() {
			return IaaSprovider;
		}
		public void setProvider(int provider) {
			this.IaaSprovider = provider;
		}
		public String getApplicationid() {
			return applicationid;
		}
		public void setApplicationid(String applicationid) {
			this.applicationid = applicationid;
		}
		
		public int getDeploymentid() {
			return deploymentid;
		}
		public void setDeploymentid(int deploymentid) {
			this.deploymentid = deploymentid;
		}
	
		public int getSchemeid() {
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
		
		public String MsgToString (GenericPricingMessage msg){
			String toprint = "\""+ Integer.toString(deploymentid) + "\""+"," +"\""+ Integer.toString(schemeid)+"\""+","+"\""+Double.toString(charges)+"\"";
			return toprint;
		}

		
	}

