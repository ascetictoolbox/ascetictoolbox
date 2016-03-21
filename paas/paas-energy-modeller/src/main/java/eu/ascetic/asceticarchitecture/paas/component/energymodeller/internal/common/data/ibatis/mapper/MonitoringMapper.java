package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.EnergyModellerMonitoring;



public interface MonitoringMapper {

// M. Fontanella - 20 Jan 2016 - begin
	@Insert("insert into EMONITORING (providerid,applicationid,deploymentid,type, started, status , events ) values (#{providerid},#{applicationid},#{deploymentid},'MONITORING',#{started},#{status},#{events})")	
    public void createMonitoring(EnergyModellerMonitoring monitoring);
	
	@Delete("DELETE FROM EMONITORING WHERE providerid = #{providerid} and applicationid = #{applicationid} and deploymentid = #{deploymentid} and type = 'MONITORING'")
    public void terminateMonitoring(String providerid,String applicationid,String deploymentid);
    
	@Select("select * from EMONITORING where providerid = #{providerid} and applicationid = #{applicationid} and deploymentid = #{deploymentid} and type = 'MONITORING'")
    public List<EnergyModellerMonitoring> getByDeploymentId(String providerid,String applicationid,String deploymentid);
// M. Fontanella - 20 Jan 2016 - end
    	       
    @Select("select * from EMONITORING where status=true and type='MONITORING'")
    public List<EnergyModellerMonitoring> getMonitoringActive();
	
 }


