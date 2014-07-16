package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel;

public class EnergyModel{
	double coefficientCPU=0.0;
	double coefficientRAM=0.0;
	double intercept=0.0;
	
	public EnergyModel(){
		
	}
	
	public EnergyModel (double coefficientCPU, double coefficientRAM, double intercept){
		this.coefficientCPU=coefficientCPU;
		this.coefficientRAM=coefficientRAM;
		this.intercept=intercept;
	}
	
	public double getCoefCPU(){
		return coefficientCPU;
	}
	
	public double getCoefRAM(){
		return coefficientRAM;
	}
	
	public double getIntercept(){
		return intercept;
	}
	
	public void setCoefCPU(double coefficientCPU){
		this.coefficientCPU=coefficientCPU;
	}
	
	public void setCoefRAM(double coefficientRAM){
		this.coefficientRAM=coefficientRAM;
	}
	
	public void setIntercept(double intercept){
		this.intercept=intercept;
	}
	
	
}