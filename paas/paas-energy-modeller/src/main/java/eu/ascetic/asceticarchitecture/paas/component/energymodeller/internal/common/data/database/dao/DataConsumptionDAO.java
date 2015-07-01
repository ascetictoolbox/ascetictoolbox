/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao;

import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.ApplicationSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;

public interface DataConsumptionDAO {
	    
		public void initialize();
		
		public void setDataSource(DataSource dataSource);
	
	    public void save(DataConsumption data);
	    
	    public Timestamp getLastConsumptionForVM(String applicationid, String vmid);

	    public double getPowerInIntervalForVM(String applicationid, String vmid,Timestamp start,Timestamp end);
	    
	    public double getTotalEnergyForVM(String applicationid, String deploymentid,String vmid);

		double getTotalEnergyForVMTime(String applicationid,String vmid, Timestamp start, Timestamp end);
		
		int getSamplesBetweenTime(String applicationid,String vmid, long start, long end);
		
		ApplicationSample getSampleAtTime(String applicationid,String vmid, long time);
		
		long getSampleTimeBefore(String applicationid,String vmid, long time);
	
		long getSampleTimeAfter(String applicationid,String vmid, long time);

		public List<ApplicationSample> getDataSamplesVM(String applicationid, String deployment,String vmid,long start,long end);
		
		void insertBatch(List<DataConsumption> samples);
	    
}
