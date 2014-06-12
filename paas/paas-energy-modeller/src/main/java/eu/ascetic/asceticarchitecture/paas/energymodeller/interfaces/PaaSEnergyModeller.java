package eu.ascetic.asceticarchitecture.paas.energymodeller.interfaces;

import javax.ws.rs.core.Response;

public interface PaaSEnergyModeller {

	public Response startModellingApplicationEnergy(String applicationid,String providerid);
	
	public Response energyApplicationConsumption( String applicationid, String providerid) ;
	
	public Response stopModellingApplicationEnergy(String applicationid, String providerid);
	
	public Response energyEstimation( String applicationid, String providerid) ;
	
	
}
