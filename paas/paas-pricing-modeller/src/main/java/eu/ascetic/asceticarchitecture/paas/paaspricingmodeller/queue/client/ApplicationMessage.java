package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client;

import java.util.List;


public class ApplicationMessage extends GenericPricingMessage{
	
		
	
		private int IaaSprovider;
	
		private List<Integer> VMids;
		
		public ApplicationMessage (){

		}

		public void setAppMessage(int deplID, Unit unit, double value, List<Integer> VMs){
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
	
	}

