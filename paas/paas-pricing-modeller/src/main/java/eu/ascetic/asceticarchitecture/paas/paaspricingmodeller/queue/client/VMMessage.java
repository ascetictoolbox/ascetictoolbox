package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client;

import java.util.List;


public class VMMessage extends GenericPricingMessage{
	
	
		private int schemeid;
	
		private int VMid;
		
		public VMMessage (){

		}
		
		public void setVMMessage(int deplID, int VMid, int schemeID, Unit unit, double value){
			this.deploymentid = deplID;
			this.schemeid = schemeID;
			this.VMid = VMid;
			this.unit = unit;
			this.value = value;
		}
		

		public int getDeploymentid() {
			return deploymentid;
		}
		public void setDeploymentid(int VMid) {
			this.deploymentid = VMid;
		}
		public int getVMid() {
			return VMid;
		}
		public void setVMid(int deploymentid) {
			this.VMid = deploymentid;
		}
		
		public int getSchemeid() {
			return schemeid;
		}
		public void setSchemeid(int schemeid) {
			this.schemeid = schemeid;
		}
		
		
	
	}

