package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;

public interface DataConsumptionMapper {
	
	// TODO when restored add deploymentid = #{deploymentid}
	//application id | deployment id | start time | end time | event load (cpu/ram usage) | total energy consumed | min | max | avg
	  @Select("SELECT * FROM DATACONSUMPTION WHERE applicationid = #{applicationid} order by time asc")
	  List<DataConsumption> selectByApp(@Param("applicationid")String applicationid);

	  @Select("SELECT * FROM DATACONSUMPTION WHERE deploymentid = #{deploymentid} order by time asc")
	  List<DataConsumption> selectByDeploy(@Param("deploymentid") String deploymentid);
	  
	  @Select("SELECT * FROM DATACONSUMPTION WHERE vmid = #{vmid} and metrictype='power' order by time asc")
	  List<DataConsumption> selectByVm(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  	  
	  @Insert("INSERT INTO DATACONSUMPTION (applicationid,deploymentid,vmid,metrictype,eventid,time,vmcpu,vmenergy,vmpower,vmmemory) VALUES(#{applicationid},#{deploymentid},#{vmid},#{metrictype},#{eventid},#{time},#{vmcpu},#{vmenergy},#{vmpower},#{vmmemory})")
	  void createMeasurement(DataConsumption dc);

	  @Select("select max(time) FROM DATACONSUMPTION WHERE vmid = #{vmid} and metrictype = 'power'")
	  long getLastConsumptionForVM(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);

	  @Select("select IFNULL(sum(vmenergy),0) from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'power'")
	  double getTotalEnergyForVM(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);

	  @Select("select IFNULL(avg(vmpower),0) from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'power' and  time > #{starttime} and time <= #{endtime}")
	  double getPowerInIntervalForVM(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
	  
	  @Select("select IFNULL(avg(vmpower),0) from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'power' ")
	  double getAvgPowerForVM(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  
	  @Select("select IFNULL(sum(vmenergy),0) from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'power' and time > #{starttime} and time <= #{endtime}")
	  double getTotalEnergyForVMTime(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
 
	  @Select("select * from DATACONSUMPTION WHERE vmid = #{vmid} and time > #{starttime}  and metrictype = 'power' and time <= #{endtime}")
	  List<DataConsumption> getDataSamplesVM(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
 
	  @Select("select * from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'power' ")
	  List<DataConsumption> getAllDataSamplesVM(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);

	  @Select("select count(*) from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'power' ")
	  int getAllSamples(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  
	  @Select("select count(*) from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'power' and time > #{starttime} and time <= #{endtime}")
	  int getSamplesBetweenTime(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
	  
	  @Select("select count(*) from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'cpu' ")
	  List<DataConsumption> getCPUs(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	 
	  @Select("select count(*) from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'memory' ")
	  List<DataConsumption> getMemory(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	 
	  @Select("select * from DATACONSUMPTION WHERE metrictype = 'power' and vmid = #{vmid} and time = #{time}")
	  DataConsumption getSampleAtTime(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  
	  @Select("select IFNULL(max(time),0) from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'power' and time <= #{time}")
	  long getSampleTimeBefore(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  
	  @Select("select IFNULL(min(time),0) from DATACONSUMPTION WHERE metrictype = 'power' and vmid = #{vmid} and time >= #{time}")
	  long getSampleTimeAfter(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  

}

