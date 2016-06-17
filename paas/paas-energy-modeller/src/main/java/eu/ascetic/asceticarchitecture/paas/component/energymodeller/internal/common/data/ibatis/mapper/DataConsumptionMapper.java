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
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("SELECT * FROM DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid} and metrictype='power' order by time asc")
	  List<DataConsumption> selectByVm(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  // M. Fontanella - 26 Apr 2016 - begin
	  @Select("SELECT * FROM DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid} and metrictype='virtualpower' order by time asc")
	  List<DataConsumption> selectByVmVirtualPower(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 26 Apr 2016 - end
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 20 Jan 2016 - begin	 
	  @Insert("INSERT INTO DATACONSUMPTION (providerid,applicationid,deploymentid,vmid,metrictype,eventid,time,vmcpu,vmenergy,vmpower,vmmemory) VALUES(#{providerid},#{applicationid},#{deploymentid},#{vmid},#{metrictype},#{eventid},#{time},#{vmcpu},#{vmenergy},#{vmpower},#{vmmemory})")
	  void createMeasurement(DataConsumption dc);
	  // M. Fontanella - 20 Jan 2016 - end

	  // M. Fontanella - 16 Jun 2016 - begin
	  // M. Fontanella - 05 Feb 2016 - begin
	  @Select("select IFNULL(max(time),0) FROM DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid} and metrictype = 'power'")
	  long getLastConsumptionForVM(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 05 Feb 2016 - end
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  // M. Fontanella - 26 Apr 2016 - begin
	  @Select("select IFNULL(max(time),0) FROM DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid} and metrictype = 'virtualpower'")
	  long getLastConsumptionForVMVirtualPower(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 26 Apr 2016 - end
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select IFNULL(sum(vmenergy),0) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'power'")
	  double getTotalEnergyForVM(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  // M. Fontanella - 26 Apr 2016 - begin
	  @Select("select IFNULL(sum(vmenergy),0) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'virtualpower'")
	  double getTotalEnergyForVMVirtualPower(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 26 Apr 2016 - end
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select IFNULL(avg(vmpower),0) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'power' and  time > #{starttime} and time <= #{endtime}")
	  double getPowerInIntervalForVM(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  // M. Fontanella - 26 Apr 2016 - begin
	  @Select("select IFNULL(avg(vmpower),0) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'virtualpower' and  time > #{starttime} and time <= #{endtime}")
	  double getVirtualPowerInIntervalForVM(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
	  // M. Fontanella - 26 Apr 2016 - end
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select IFNULL(avg(vmpower),0) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'power' ")
	  double getAvgPowerForVM(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 26 Apr 2016 - begin
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select IFNULL(avg(vmpower),0) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'virtualpower' ")
	  double getAvgVirtualPowerForVM(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 16 Jun 2016 - begin
	  // M. Fontanella - 26 Apr 2016 - end

	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select IFNULL(sum(vmenergy),0) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'power' and time > #{starttime} and time <= #{endtime}")
	  double getTotalEnergyForVMTime(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
	  // M. Fontanella - 16 Jun 2016 - end
 
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select * from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid} and time > #{starttime}  and metrictype = 'power' and time <= #{endtime}")
	  List<DataConsumption> getDataSamplesVM(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
	  // M. Fontanella - 16 Jun 2016 - end
 
	  // M. Fontanella - 26 Apr 2016 - begin
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select * from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid} and time > #{starttime}  and metrictype = 'virtualpower' and time <= #{endtime}")
	  List<DataConsumption> getDataSamplesVMVirtualPower(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
	  // M. Fontanella - 16 Jun 2016 - end
	  // M. Fontanella - 26 Apr 2016 - end

	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select * from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'power' ")
	  List<DataConsumption> getAllDataSamplesVM(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 16 Jun 2016 - end

	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select count(*) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'power' ")
	  int getAllSamples(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select count(*) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'power' and time > #{starttime} and time <= #{endtime}")
	  int getSamplesBetweenTime(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 26 Apr 2016 - begin
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select count(*) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'virtualpower' and time > #{starttime} and time <= #{endtime}")
	  int getSamplesBetweenTimeVirtualPower(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("starttime")long starttime,@Param("endtime") long endtime);
	  // M. Fontanella - 16 Jun 2016 - end
	  // M. Fontanella - 26 Apr 2016 - end
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select * from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'cpu' ")
	  List<DataConsumption> getCPUs(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 16 Jun 2016 - end
	 
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select * from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'memory' ")
	  List<DataConsumption> getMemory(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select * from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'power' ")
	  List<DataConsumption> getPower(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 16 Jun 2016 - end

	  // M. Fontanella - 26 Apr 2016 - begin
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select * from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'virtualpower' ")
	  List<DataConsumption> getVirtualPower(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 16 Jun 2016 - begin
	  // M. Fontanella - 26 Apr 2016 - end
	  
	  // M. Fontanella - 20 Jan 2016 - begin
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select * from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'power' ORDER BY providerid,applicationid,deploymentid,vmid DESC LIMIT 1")
	  DataConsumption getLastSample(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 16 Jun 2016 - begin
	  // M. Fontanella - 20 Jan 2016 - end
	 
	  // M. Fontanella - 26 Apr 2016 - begin
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select * from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'virtualpower' ORDER BY providerid,applicationid,deploymentid,vmid DESC LIMIT 1")
	  DataConsumption getLastSampleVirtualPower(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 16 Jun 2016 - end
	  // M. Fontanella - 26 Apr 2016 - end
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select * from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and metrictype = 'power' and vmid = #{vmid} and time = #{time} LIMIT 1")
	  DataConsumption getSampleAtTime(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 26 Apr 2016 - begin
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select * from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and metrictype = 'virtualpower' and vmid = #{vmid} and time = #{time} LIMIT 1")
	  DataConsumption getSampleAtTimeVirtualPower(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  // M. Fontanella - 16 Jun 2016 - end
	  // M. Fontanella - 26 Apr 2016 - end
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select IFNULL(max(time),0) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'power' and time <= #{time}")
	  long getSampleTimeBefore(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 26 Apr 2016 - begin
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select IFNULL(max(time),0) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid}  and metrictype = 'virtualpower' and time <= #{time}")
	  long getSampleTimeBeforeVirtualPower(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  // M. Fontanella - 16 Jun 2016 - end
	  // M. Fontanella - 26 Apr 2016 - end
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select IFNULL(min(time),0) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and metrictype = 'power' and vmid = #{vmid} and time >= #{time}")
	  long getSampleTimeAfter(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 26 Apr 2016 - begin
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select IFNULL(min(time),0) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and metrictype = 'virtualpower' and vmid = #{vmid} and time >= #{time}")
	  long getSampleTimeAfterVirtualPower(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid, @Param("time") long time);
	  // M. Fontanella - 16 Jun 2016 - end
	  // M. Fontanella - 26 Apr 2016 - end
	  
	  // M. Fontanella - 17 May 2016 - begin
	  // M. Fontanella - 26 Apr 2016 - begin
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select IFNULL(avg(vmcpu),-1) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid} and metrictype = 'cpu' and time = #{time} LIMIT 1")
	  double getCPUSampleAtTime(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("time") long time);
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select IFNULL(avg(vmmemory),-1) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid} and metrictype = 'memory' and time = #{time} LIMIT 1")
	  double getMemorySampleAtTime(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("time") long time);
	  // M. Fontanella - 16 Jun 2016 - end
	  // M. Fontanella - 26 Apr 2016 - end
	  // M. Fontanella - 17 May 2016 - end
	  
	  // M. Fontanella - 14 Jun 2016 - begin 
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("select count(*) from DATACONSUMPTION WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid} and metrictype = #{metrictype} and time = #{time}")
	  int getSamplesAtTime(@Param("providerid")String providerid,@Param("deploymentid")String deploymentid,@Param("vmid") String vmid,@Param("metrictype")String metrictype,@Param("time") long time);
	  // M. Fontanella - 16 Jun 2016 - end
	  // M. Fontanella - 14 Jun 2016 - end
}

