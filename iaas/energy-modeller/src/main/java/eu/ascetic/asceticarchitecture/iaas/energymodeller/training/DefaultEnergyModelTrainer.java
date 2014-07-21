package eu.ascetic.asceticarchitecture.iaas.energymodeller.training;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.HostEnergyCalibrationData;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;

import java.util.*;


public class DefaultEnergyModelTrainer implements EnergyModelTrainerInterface {
	
	public DefaultEnergyModelTrainer(){}
	
	//It takes values only for 1 min.
	
	public HashMap<Host, ArrayList<HostEnergyCalibrationData>> storeValues = new HashMap<Host, ArrayList<HostEnergyCalibrationData>>();
	
	@Override
	public boolean trainModel (Host host, double usageCPU, double usageRAM, double wattsUsed, int numberOfValues, TimePeriod duration ){
		if (duration.getDuration()==60) {
		HostEnergyCalibrationData usageHost=new HostEnergyCalibrationData(usageCPU, usageRAM, wattsUsed);
		EnergyModel model = new EnergyModel();
		ArrayList<HostEnergyCalibrationData> temp=new ArrayList<>();
		int num=0;
		if (storeValues.containsKey(host)){
			temp=storeValues.get(host);
			temp.add(usageHost);
			storeValues.put(host, temp);
			num=temp.size();
			}
			else {
				
				temp.add(usageHost);
				storeValues.put(host, temp);
			}
		
		if (num==numberOfValues){
			return true;
		}
		else return false; }
		else {
			return false;
		}

	}
	
	
	public void printValuesMap(HashMap<Host, ArrayList<HostEnergyCalibrationData>> storeValues, Host host){

			ArrayList<HostEnergyCalibrationData> data = storeValues.get(host);
	         System.out.print(host.getHostName() + ": ");
	         for (Iterator< HostEnergyCalibrationData> it = data.iterator(); it.hasNext();) {
	        System.out.println(it.next().getCpuUsage());
	         }
	     // }
	      System.out.println();
	}
	
	public EnergyModel retrieveModel (Host host){
			EnergyModel temp=new EnergyModel();
			ArrayList<HostEnergyCalibrationData> valuesOfHost=new ArrayList<>();
			valuesOfHost=storeValues.get(host);
			

			
			//WattsUsed=intercept+coefficientCPU*usageCPU+coefficientRAM*usageRAM;
			ArrayList<Double> UR=new ArrayList<>();
			ArrayList<Double> CPUEnergy=new ArrayList<>();
			ArrayList<Double> CPURAM=new ArrayList<>();
			ArrayList<Double> RAMEnergy=new ArrayList<>();
			ArrayList<Double> UC=new ArrayList<>();
			
			HostEnergyCalibrationData temp1=new HostEnergyCalibrationData();
			double energy=0.0;
			double CPU = 0.0;
			double RAM = 0.0;
			for (Iterator< HostEnergyCalibrationData> it = valuesOfHost.iterator(); it.hasNext();) {
				temp1=it.next();
				//System.out.print(temp1.getCpuUsage());
				UR.add(temp1.getMemoryUsage()*temp1.getMemoryUsage());
				CPUEnergy.add(temp1.getCpuUsage()*temp1.getWattsUsed());
				CPURAM.add(temp1.getCpuUsage()*temp1.getMemoryUsage());
				RAMEnergy.add(temp1.getMemoryUsage()*temp1.getWattsUsed());
				UC.add(temp1.getCpuUsage()*temp1.getCpuUsage());
				energy = temp1.getWattsUsed();
				CPU=temp1.getCpuUsage();
				RAM=temp1.getMemoryUsage();

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
		
			
		return temp;
		//this function should use the values stored to create the coefficients of the model. It returns the coefficients to the caller.
	}
	
	private double calculateSums(ArrayList<Double> valuesList){
		double sum =0.0;
		for (Iterator<Double> it = valuesList.iterator(); it.hasNext();){
			sum= sum + it.next();
		}
		return sum;
	}


}