package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.queue;

import java.util.ArrayList;
import java.util.List;

public class GenericPricingMessage {
		
		private int IaaSProviderID;
		private int schemeid;
		private double newEnergyPrice;

		private double valueToReturn;
		
		public GenericPricingMessage (){

		}
		
		public GenericPricingMessage (int providerID, double newEnergyPrice){
			this.IaaSProviderID = providerID;
			this.newEnergyPrice = newEnergyPrice;
		}
		
		public double getEnergyPrice(){
			return newEnergyPrice;
		}
		
		public int getProviderID(){
			return IaaSProviderID;
		}
		public int getSchemeid() {
			return schemeid;
		}
		public void setSchemeid(int schemeid) {
			this.schemeid = schemeid;
		}
		
		public double getValue() {
			return valueToReturn;
		}
		public void setValue(double value) {
			this.valueToReturn = value;
		}
		
		public String MsgToString (GenericPricingMessage msg){
			String toprint = "\""+Integer.toString(IaaSProviderID)+"\""+"\""+Double.toString(newEnergyPrice)+"\"";
			return toprint;
		}

		
	}

