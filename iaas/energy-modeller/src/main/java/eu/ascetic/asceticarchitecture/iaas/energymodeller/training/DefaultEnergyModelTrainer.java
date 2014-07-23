/**
 *  Copyright 2014 Athens University of Economics and Business
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eu.ascetic.asceticarchitecture.iaas.energymodeller.training;

/**
 * This is implements the basic trainer for the energy model for the ASCETiC project. 
 * 
 * @author E. Agiatzidou
 */


import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.HostEnergyCalibrationData;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;

import java.util.*;


public class DefaultEnergyModelTrainer implements EnergyModelTrainerInterface {
	
	public DefaultEnergyModelTrainer(){}
	
		
	public HashMap<Host, ArrayList<HostEnergyCalibrationData>> storeValues = new HashMap<Host, ArrayList<HostEnergyCalibrationData>>();
	
	/**
     * This function stores the appropriate values that are needed for training the model. 
     * Several times should be called for a specific number of values to be gathered. 
     *
     * @param usageCPU The CPU usage of the host
     * @param usageRAM The RAM usage of the host
     * @param wattsUsed The watts consumed under these levels of usage
     * @param numberOfValues The number of values that the trainer expects from the user before it is ready for extracting
     * the coefficients of the model.
     * @return True if the appropriate amount of values has been gathered, False if not. 
     */
	
	@Override
	public boolean trainModel (Host host, double usageCPU, double usageRAM, double wattsUsed, int numberOfValues){
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
		
		if (num>=numberOfValues){
			printValuesMap(storeValues, host);
			return true;
		}
		else return false; 

	}
	
	/**
     * This function prints the HashMap that stores the values needed for the model training for a specific host.
     *
     * @param storeValues The HashMap storing the values
     * @param host The host (key) for which the values are going to be printed
     * 
     * @return void 
     */
	
	public void printValuesMap(HashMap<Host, ArrayList<HostEnergyCalibrationData>> storeValues, Host host){

			ArrayList<HostEnergyCalibrationData> data = storeValues.get(host);
	         System.out.print(host.getHostName() + ": ");
	         HostEnergyCalibrationData next = new HostEnergyCalibrationData();
	         for (Iterator< HostEnergyCalibrationData> it = data.iterator(); it.hasNext();) {
	        	next=it.next();
		       System.out.println("CPU"+ next.getCpuUsage());
		       System.out.println(" RAM" +next.getMemoryUsage());
		       System.out.println(" watts"+next.getWattsUsed());
	         }
	    
	      System.out.println();
	}
	
	
	/**
     * This function should use the values stored to create the coefficients of the model. 
     * 
     * @param host The host (key) for which the values are going to be printed.
     * 
     * @return The coefficients and the intercept of the model.
     */
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
			
			HostEnergyCalibrationData temp1=new HostEnergyCalibrationData(0, 0, 0);
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
		
	}
	
	
	/**
     * This function calculates the sums of the values in an Array List. 
     * 
     * @param valuesList The ArrayList to calculate the values of. 
     * 
     * @return The sum of the values. 
     */
	private double calculateSums(ArrayList<Double> valuesList){
		double sum =0.0;
		for (Iterator<Double> it = valuesList.iterator(); it.hasNext();){
			sum= sum + it.next();
		}
		return sum;
	}


}