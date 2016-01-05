package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.EnergyModellerMonitoring;



public interface MonitoringMapper {

	@Insert("insert into EMONITORING (applicationid,deploymentid,type, started, status , events ) values (#{applicationid},#{deploymentid},'MONITORING',#{started},#{status},#{events})")	
    public void createMonitoring(EnergyModellerMonitoring monitoring);
	
	@Delete("DELETE FROM EMONITORING WHERE applicationid = #{applicationid} and deploymentid = #{deploymentid} and type = 'MONITORING'")
    public void terminateMonitoring(String applicationid,String deploymentid);
    
	@Select("select * from EMONITORING where applicationid = #{applicationid} and deploymentid = #{deploymentid} and type = 'MONITORING'")
    public List<EnergyModellerMonitoring> getByDeploymentId(String applicationid,String deploymentid);
    	       
    @Select("select * from EMONITORING where status=true and type='MONITORING'")
    public List<EnergyModellerMonitoring> getMonitoringActive();
	
 }


