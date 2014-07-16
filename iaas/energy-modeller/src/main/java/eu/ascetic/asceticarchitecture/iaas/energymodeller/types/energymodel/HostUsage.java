package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel;

public class HostUsage{
	double usageCPU=0.0;
	double usageRAM=0.0;
	double totalEnergyUsed=0.0;
	
	public HostUsage(){
		
	}
	
	public HostUsage (double usageCPU, double usageRAM, double totalEnergyUsed){
		this.usageCPU=usageCPU;
		this.usageRAM=usageRAM;
		this.totalEnergyUsed= totalEnergyUsed;
	}
	

	public double getusageCPU(){
		return usageCPU;
	}
	
	public double getusageRAM(){
		return usageRAM;
	}
	
	public double getTotalEnergyUsed(){
		return totalEnergyUsed;
	}
	
	public void setusageCPU(double usageCPU){
		this.usageCPU=usageCPU;
	}
	
	public void setusageRAM(double usageRAM){
		this.usageRAM=usageRAM;
	}
	
	public void setTotalEnergyUsed(double totalEnergyUsed){
		this.totalEnergyUsed=totalEnergyUsed;
	}
}