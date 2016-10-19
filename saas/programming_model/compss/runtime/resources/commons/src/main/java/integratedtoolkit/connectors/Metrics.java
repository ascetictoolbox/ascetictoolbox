package integratedtoolkit.connectors;

import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;


public interface Metrics {

    public Float getExpectedTotalEnergy();
        
    public Float getExpectedTotalCost();
	
	public Float getTotalEnergy();
    
    public long getTotalTime();
    
    public Float getTotalCost();

    public Float currentCostPerHour();

    public Float getMachineCostPerHour(CloudMethodResourceDescription rc);
    
}
