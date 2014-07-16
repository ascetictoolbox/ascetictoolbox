package eu.ascetic.asceticarchitecture.iaas.energymodeller.training;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.HostUsage;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;

import java.util.*;


public class DefaultEnergyModelTrainer implements EnergyModelTrainerInterface {
	
	public DefaultEnergyModelTrainer(){}
	
	HashMap<Host, ArrayList<HostUsage>> storeValues = new HashMap<Host, ArrayList<HostUsage>>();
	 
	
	@Override
	public int trainModel (Host host, double usageCPU, double usageRAM, double totalEnergyUsed){
		HostUsage usageHost=new HostUsage(usageCPU, usageRAM, totalEnergyUsed);
		int numberOfValues=0;
		if (storeValues.containsKey(host)){
			ArrayList<HostUsage> temp=new ArrayList<>();
			temp=storeValues.get(host);
			temp.add(usageHost);
			storeValues.put(host, temp);
			numberOfValues=temp.size();
			}
			else {
				ArrayList<HostUsage> temp=new ArrayList<>();
				temp.add(usageHost);
				storeValues.put(host, temp);
				numberOfValues=temp.size();
			}
		printValuesMap(storeValues);
		 return numberOfValues;

	}
	
	public void printValuesMap(HashMap<Host, ArrayList<HostUsage>> storeValues){
		 Set set = storeValues.entrySet();
	      Iterator i = set.iterator();
	      while(i.hasNext()) {
	         Map.Entry aHost = (Map.Entry)i.next();
	         System.out.print(aHost.getKey() + ": ");
	         System.out.println(aHost.getValue());
	      }
	      System.out.println();
	}
	
	public EnergyModel retrieveModel (Host host){
		EnergyModel temp=new EnergyModel();
		if (storeValues.containsKey(host)){
			ArrayList<HostUsage> valuesOfHost=new ArrayList<>();
			valuesOfHost=storeValues.get(host);
			//TotalEnergyUsed=intercept+coefficientCPU*usageCPU+coefficientRAM*usageRAM;
			ArrayList<Double> UR=new ArrayList<>();
			ArrayList<Double> CPUEnergy=new ArrayList<>();
			ArrayList<Double> CPURAM=new ArrayList<>();
			ArrayList<Double> RAMEnergy=new ArrayList<>();
			ArrayList<Double> UC=new ArrayList<>();
			HostUsage temp1=new HostUsage();
			double energy=0.0;
			double CPU = 0.0;
			double RAM = 0.0;
			for (Iterator<HostUsage> it = valuesOfHost.iterator(); it.hasNext();) {
				temp1=it.next();
				UR.add(temp1.getusageRAM()*temp1.getusageRAM());
				CPUEnergy.add(temp1.getusageCPU()*temp1.getTotalEnergyUsed());
				CPURAM.add(temp1.getusageCPU()*temp1.getusageRAM());
				RAMEnergy.add(temp1.getusageRAM()*temp1.getTotalEnergyUsed());
				UC.add(temp1.getusageCPU()*temp1.getusageCPU());
				energy = temp1.getTotalEnergyUsed();
				CPU=temp1.getusageCPU();
				RAM=temp1.getusageRAM();

			}
			double sumUR=calculateSums(UR);
			double sumCPUEnergy=calculateSums(CPUEnergy);
			double sumCPURAM=calculateSums(CPURAM);
			double sumRAMEnergy=calculateSums(RAMEnergy);
			double sumUC=calculateSums(UC);
			
			double coefficientCPU = (sumUR*sumCPUEnergy-sumCPURAM*sumRAMEnergy)/(sumUR*sumUC-(sumCPURAM*sumCPURAM));
			double coefficientRAM = (sumUC*sumRAMEnergy-sumCPURAM*sumCPUEnergy)/(sumUR*sumUC-(sumCPURAM*sumCPURAM));
			double intercept = energy-coefficientCPU*CPU-coefficientRAM*RAM;
			temp.setCoefCPU(coefficientCPU);
			temp.setCoefRAM(coefficientRAM);
			temp.setIntercept(intercept);
		
		}
			else {
				System.out.print("No Values for this Host for the training");
				}
		return temp;
		//this function should use the values stored to create the coeeficients of the model. It returns the coefficients to the caller.
	}
	
	private double calculateSums(ArrayList<Double> valuesList){
		double sum =0.0;
		for (Iterator<Double> it = valuesList.iterator(); it.hasNext();){
			sum= sum + it.next();
		}
		return sum;
	}
}