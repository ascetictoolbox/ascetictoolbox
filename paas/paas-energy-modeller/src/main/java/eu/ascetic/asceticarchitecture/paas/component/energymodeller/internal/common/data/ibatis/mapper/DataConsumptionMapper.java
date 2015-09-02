package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;

public interface DataConsumptionMapper {
	
	//application id | deployment id | start time | end time | event load (cpu/ram usage) | total energy consumed | min | max | avg
	  @Select("SELECT * FROM DATACONSUMPTION WHERE applicationid = #{applicationid}")
	  List<DataConsumption> selectByApp(@Param("applicationid")String applicationid);

	  @Select("SELECT * FROM DATACONSUMPTION WHERE deploymentid = #{deploymentid}")
	  List<DataConsumption> selectByDeploy(@Param("deploymentid") String deploymentid);
	  
	  @Select("SELECT * FROM DATACONSUMPTION WHERE deploymentid = #{deploymentid} and vmid = #{vmid}")
	  List<DataConsumption> selectByVm(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  	  
	  @Insert("INSERT INTO DATACONSUMPTION (applicationid,deploymentid,vmid,eventid,time,vmcpu,vmenergy,vmpower) VALUES(#{applicationid},#{deploymentid},#{vmid},#{eventid},#{time},#{cpu},#{vmenergy},#{vmpower})")
	  void createMeasurement(DataConsumption dc);
	  
	  //ok
	  @Select("select max(time) FROM DATACONSUMPTION WHERE deploymentid = #{deploymentid} and vmid = #{vmid}")
	  long getLastConsumptionForVM(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);

	  //ok
	  @Select("select IFNULL(sum(vmenergy),0) from DATACONSUMPTION WHERE deploymentid = #{deploymentid} and vmid = #{vmid}")
	  double getTotalEnergyForVM(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);

	  //ok
	  @Select("select IFNULL(avg(vmpower),0) from DATACONSUMPTION WHERE deploymentid = #{deploymentid} and vmid = #{vmid} and time > #{starttime} and time <= #{endtime}")
	  double getPowerInIntervalForVM(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
	  
	  // ok
	  @Select("select IFNULL(sum(vmenergy),0) from DATACONSUMPTION WHERE deploymentid = #{deploymentid} and vmid = #{vmid} and time > #{starttime} and time <= #{endtime}")
	  double getTotalEnergyForVMTime(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
 
	  // ok
	  @Select("select * from DATACONSUMPTION WHERE deploymentid = #{deploymentid} and vmid = #{vmid} and time > #{starttime} and time <= #{endtime}")
	  List<DataConsumption> getDataSamplesVM(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
 
	  // ok	  	  	
	  @Select("select count(*) from DATACONSUMPTION WHERE deploymentid = #{deploymentid} and vmid = #{vmid} and time > #{starttime} and time <= #{endtime}")
	  int getSamplesBetweenTime(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
	  
	  // ok
	  @Select("select * from DATACONSUMPTION WHERE deploymentid = #{deploymentid} and vmid = #{vmid} and time = #{time}")
	  DataConsumption getSampleAtTime(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  
	  // ok
	  @Select("select max(time) from DATACONSUMPTION WHERE deploymentid = #{deploymentid} and vmid = #{vmid} and time <= #{time}")
	  long getSampleTimeBefore(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  
	  // ok
	  @Select("select max(time) from DATACONSUMPTION WHERE deploymentid = #{deploymentid} and vmid = #{vmid} and time >= #{time}")
	  long getSampleTimeAfter(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  

}

