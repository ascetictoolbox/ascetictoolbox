/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
/**
 * 
 * @author sommacam
 * represent data stored inside the database about consumption
 */
public interface DataConsumptionMapper {
	
	// TODO when restored add deploymentid = #{deploymentid}
	// M. Fontanella - 20 Jan 2016 - begin
	//provider id | application id | deployment id | start time | end time | event load (cpu/ram usage) | total energy consumed | min | max | avg
	// M. Fontanella - 20 Jan 2016 - end
	  @Select("SELECT * FROM DATACONSUMPTION WHERE applicationid = #{applicationid} order by time asc")
	  List<DataConsumption> selectByApp(@Param("applicationid")String applicationid);

	  @Select("SELECT * FROM DATACONSUMPTION WHERE deploymentid = #{deploymentid} order by time asc")
	  List<DataConsumption> selectByDeploy(@Param("deploymentid") String deploymentid);
	  
	  @Select("SELECT * FROM DATACONSUMPTION WHERE vmid = #{vmid} and metrictype='power' order by time asc")
	  List<DataConsumption> selectByVm(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	 
	  // M. Fontanella - 20 Jan 2016 - begin	 
	  @Insert("INSERT INTO DATACONSUMPTION (providerid,applicationid,deploymentid,vmid,metrictype,eventid,time,vmcpu,vmenergy,vmpower,vmmemory) VALUES(#{providerid},#{applicationid},#{deploymentid},#{vmid},#{metrictype},#{eventid},#{time},#{vmcpu},#{vmenergy},#{vmpower},#{vmmemory})")
	  void createMeasurement(DataConsumption dc);
	  // M. Fontanella - 20 Jan 2016 - end

	  // M. Fontanella - 05 Feb 2016 - begin
	  @Select("select IFNULL(max(time),0) FROM DATACONSUMPTION WHERE vmid = #{vmid} and metrictype = 'power'")
	  long getLastConsumptionForVM(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 05 Feb 2016 - end
	  
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
	  
	  @Select("select * from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'cpu' ")
	  List<DataConsumption> getCPUs(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	 
	  @Select("select * from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'memory' ")
	  List<DataConsumption> getMemory(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  
	  @Select("select * from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'power' ")
	  List<DataConsumption> getPower(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);

	  // M. Fontanella - 20 Jan 2016 - begin
	  @Select("select * from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'power' ORDER BY providerid,applicationid,deploymentid,vmid DESC LIMIT 1")
	  DataConsumption getLastSample(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	 // M. Fontanella - 20 Jan 2016 - end
	 
	  @Select("select * from DATACONSUMPTION WHERE metrictype = 'power' and vmid = #{vmid} and time = #{time} LIMIT 1")
	  DataConsumption getSampleAtTime(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  
	  @Select("select IFNULL(max(time),0) from DATACONSUMPTION WHERE vmid = #{vmid}  and metrictype = 'power' and time <= #{time}")
	  long getSampleTimeBefore(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  
	  @Select("select IFNULL(min(time),0) from DATACONSUMPTION WHERE metrictype = 'power' and vmid = #{vmid} and time >= #{time}")
	  long getSampleTimeAfter(@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  

}

