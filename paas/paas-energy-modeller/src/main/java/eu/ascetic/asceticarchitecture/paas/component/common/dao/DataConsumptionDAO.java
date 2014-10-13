/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.common.dao;

import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import eu.ascetic.asceticarchitecture.paas.component.common.model.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EnergySamples;

public interface DataConsumptionDAO {
    
	    
		public void initialize();
		
		public void setDataSource(DataSource dataSource);
	
	    public void save(DataConsumption data);
	    
	    public List<DataConsumption> getByApplicationId(String applicationid);
	    
	    public List<DataConsumption> getByDeploymentId(String deploymentyid);
	    
	    public List<DataConsumption> getByVMId(String vmid);
	    
	    public List<DataConsumption> getByEventId(String eventid);
	    
	    public Timestamp getLastConsumptionForVM(String applicationid, String vmid);
	    
	    public double[] getConsumptionDataVM(String applicationid, String vmid);
	    
	    public double[] getConsumptionByTimeVM(String applicationid, String vmid);
	    
	    public double[] getTimeDataVM(String applicationid, String vmid);
	    
	    public double[] getCpuDataVM(String applicationid, String vmid);
	    
	    public Timestamp getFirsttConsumptionForVM(String applicationid, String vmid);

		public double getTotalEnergyForDeployment(String applicationid,String deploymentid);

		public double getTotalEnergyForVM(String applicationid, String deploymentid,String vmid);

		double getTotalEnergyForVMTime(String applicationid,String vmid, Timestamp start, Timestamp end);

		//List<EnergySamples> getDataSamplesVM(String applicationid, String deployment,String vmid, Timestamp start, Timestamp end);

		void insertBatch(List<DataConsumption> samples);
	
	    
}
