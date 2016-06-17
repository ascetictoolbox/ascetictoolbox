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
import org.apache.ibatis.annotations.Update;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.VirtualMachine;

/**
 * 
 * @author sommacam
 * represent data stored inside the database about application
 */
public interface AppRegistryMapper {
	  // M. Fontanella - 10 Feb 2016 - begin
	  @Select("SELECT * FROM APPLICATION_REGISTRY WHERE applicationid = #{applicationid}")
	  List<VirtualMachine> selectByApp(@Param("applicationid") String applicationid);
		  
	  // M. Fontanella - 08 Feb 2016 - begin
	  @Select("SELECT providerid FROM APPLICATION_REGISTRY WHERE deploymentid = #{deploymentid} GROUP BY providerid")
	  String selectProvByDeploy(@Param("deploymentid") String deploymentid);

	  // M. Fontanella - 08 Feb 2016 - end
	  @Select("SELECT applicationid FROM APPLICATION_REGISTRY WHERE deploymentid = #{deploymentid} GROUP BY applicationid")
	  String selectAppByDeploy(@Param("deploymentid") String deploymentid);

	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("SELECT * FROM APPLICATION_REGISTRY WHERE providerid = #{providerid} and applicationid = #{applicationid} and deploymentid = #{deploymentid}")
	  List<VirtualMachine> selectByDeploy(@Param("providerid") String providerid,@Param("applicationid") String applicationid,@Param("deploymentid") String deploymentid);
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 20 Jan 2016 - begin
	  @Select("SELECT * FROM APPLICATION_REGISTRY WHERE providerid = #{providerid} and applicationid = #{applicationid} and deploymentid = #{deploymentid} and vmid = #{vmid} ")
	  VirtualMachine selectByVmp(@Param("providerid") String providerid,@Param("applicationid") String applicationid,@Param("deploymentid") String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 20 Jan 2016 - end
	  
	  @Select("SELECT * FROM APPLICATION_REGISTRY WHERE iaasid = #{iaasid} ")
	  VirtualMachine selectByIaaSId(@Param("iaasid") String iaasid);
	  
	  // M. Fontanella - 20 Jan 2016 - begin
	  @Insert("INSERT INTO APPLICATION_REGISTRY (providerid,applicationid,deploymentid,vmid,start,stop,profileid,modelid,iaasid) VALUES(#{providerid},#{applicationid},#{deploymentid},#{vmid},#{start},#{stop},#{profileid},#{modelid},#{iaasid})")
	  void createVM(VirtualMachine vm);
	  
	  @Update("UPDATE APPLICATION_REGISTRY set stop=#{stop} WHERE providerid=#{providerid} and applicationid=#{applicationid}  and deploymentid = #{deploymentid} and vmid = #{vmid} ")
	  void stopVM(VirtualMachine vm);
	  
	  //@Update("UPDATE APPLICATION_REGISTRY set modelid=#{modelid} WHERE #{applicationid} and deploymentid = #{deploymentid} and vmid = #{vmid} ")
	  @Update("UPDATE APPLICATION_REGISTRY set modelid=#{modelid} WHERE providerid=#{providerid} and applicationid=#{applicationid} and deploymentid = #{deploymentid} and vmid = #{vmid} ")
	  void setModel(VirtualMachine vm);
	  
	  //@Update("UPDATE APPLICATION_REGISTRY set profileid=#{profileid} WHERE #{applicationid} and deploymentid = #{deploymentid} and vmid = #{vmid} ")
	  @Update("UPDATE APPLICATION_REGISTRY set profileid=#{profileid} WHERE providerid=#{providerid} and applicationid=#{applicationid} and deploymentid = #{deploymentid} and vmid = #{vmid} ")
	  void setProfile(VirtualMachine vm);
	  
	  @Select("SELECT COUNT(*) FROM APPLICATION_REGISTRY WHERE providerid = #{providerid} and applicationid = #{applicationid} and deploymentid = #{deploymentid} and vmid = #{vmid} ")
	  int checkVM(@Param("providerid") String providerid,@Param("applicationid") String applicationid,@Param("deploymentid") String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 20 Jan 2016 - end
	  
	  @Select("SELECT COUNT(*) FROM APPLICATION_REGISTRY WHERE iaasid = #{iaasid} ")
	  int checkIaaSVM(@Param("iaasid") String iaasid);
	
	  // M. Fontanella - 16 Jun 2016 - begin
	  @Select("SELECT iaasid FROM APPLICATION_REGISTRY WHERE providerid = #{providerid} and deploymentid = #{deploymentid} and vmid = #{vmid} ")
	  String selectFromIaaSID(@Param("providerid") String providerid,@Param("deploymentid") String deploymentid,@Param("vmid") String vmid);
	  // M. Fontanella - 16 Jun 2016 - end
	  
	  // M. Fontanella - 20 Jan 2016 - begin
	  //@Update("UPDATE APPLICATION_REGISTRY set energy=#{energy} WHERE #{applicationid} and deploymentid = #{deploymentid} and vmid = #{vmid} ")
	  @Update("UPDATE APPLICATION_REGISTRY set energy=#{energy} WHERE providerid = #{providerid} and applicationid = #{applicationid} and deploymentid = #{deploymentid} and vmid = #{vmid} ")
	  void updateEnergy(@Param("providerid") String providerid, @Param("applicationid") String applicationid, @Param("deploymentid") String deploymentid,@Param("vmid") String vmid,@Param("energy") double energy);

	  //@Update("UPDATE APPLICATION_REGISTRY set power=#{power} WHERE #{applicationid} and deploymentid = #{deploymentid} and vmid = #{vmid}  ")
	  @Update("UPDATE APPLICATION_REGISTRY set power=#{power} WHERE providerid = #{providerid} and applicationid = #{applicationid} and deploymentid = #{deploymentid} and vmid = #{vmid}  ")
	  void updatePower(@Param("providerid") String providerid, @Param("applicationid") String applicationid, @Param("deploymentid") String deploymentid,@Param("vmid") String vmid,@Param("energy") double power);
	  // M. Fontanella - 20 Jan 2016 - end
	  
	  @Select("SELECT deploymentid FROM APPLICATION_REGISTRY where stop = 0 GROUP BY deploymentid")
	  List<String> selectDeployments();
	  
	  @Select("SELECT vmid FROM APPLICATION_REGISTRY where stop = 0 and deploymentid = #{deploymentid} GROUP BY deploymentid,vmid")
	  List<String> selectVMActiveperDeployment(@Param("deploymentid") String deploymentid);
	  
	  @Select("SELECT vmid FROM APPLICATION_REGISTRY where stop > 0 and deploymentid = #{deploymentid} GROUP BY deploymentid,vmid")
	  List<String> selectVMTerminatedperDeployment(@Param("deploymentid") String deploymentid);
	  // M. Fontanella - 10 Feb 2016 - end
	  
}