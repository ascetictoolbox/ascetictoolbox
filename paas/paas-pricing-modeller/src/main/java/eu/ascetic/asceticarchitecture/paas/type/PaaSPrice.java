package eu.ascetic.asceticarchitecture.paas.type;

public class PaaSPrice{
	double totalEnergyUsed;
	int deploymentId;
	int appId;
	int iaasId;
	double iaasPrice;
	
	
	public PaaSPrice(double totalEnergyUsed, int deploymentId, int appId, int iaasId, double iaasPrice){
		this.totalEnergyUsed=totalEnergyUsed;
		this.deploymentId=deploymentId;
		this.appId=appId;
		this.iaasId=iaasId;
		this.iaasPrice=iaasPrice;
	}
	
	public PaaSPrice(int deploymentId, int appId, int iaasId, double iaasPrice){
		this.deploymentId=deploymentId;
		this.appId=appId;
		this.iaasId=iaasId;
		this.iaasPrice=iaasPrice;
	}
	
	public PaaSPrice(double totalEnergyUsed, int deploymentId, int appId, int iaasId){
		this.totalEnergyUsed=totalEnergyUsed;
		this.deploymentId=deploymentId;
		this.appId=appId;
		this.iaasId=iaasId;
	}
}