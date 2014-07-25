package eu.ascetic.asceticarchitecture.paas.component.common.dao;

import java.util.List;

import javax.sql.DataSource;

import eu.ascetic.asceticarchitecture.paas.component.common.model.IaaSVMConsumption;

public interface IaaSDataDAO {

	public void initialize();
	
	public void setDataSource(DataSource dataSource);
	
	public String getHostIdForVM(String VMid);
	
	public String getHostTotalCpu(String hostid);
	
	public List<IaaSVMConsumption> getEnergyForVM(String hostid,String vmid);
	
}
