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

public interface AppRegistryMapper {

	  @Select("SELECT * FROM APPLICATION_REGISTRY WHERE app_id = #{app_id}")
	  List<VirtualMachine> selectByApp(@Param("app_id") String app_id);

	  @Select("SELECT * FROM APPLICATION_REGISTRY WHERE app_id = #{app_id} and deploy_id = #{deploy_id}")
	  List<VirtualMachine> selectByDeploy(@Param("app_id") String app_id,@Param("deploy_id") int deploy);
	  
	  @Select("SELECT * FROM APPLICATION_REGISTRY WHERE app_id = #{app_id} and deploy_id = #{deploy_id} and vm_id = #{vm_id} ")
	  VirtualMachine selectByVmp(@Param("app_id") String app_id,@Param("deploy_id") int deploy_id,@Param("vm_id") int vm_id);
	  
	  @Select("SELECT * FROM APPLICATION_REGISTRY WHERE iaas_id = #{iaas_id} ")
	  VirtualMachine selectByIaaSId(@Param("iaas_id") String iaas_id);
	  
	  @Insert("INSERT INTO APPLICATION_REGISTRY (app_id,deploy_id,vm_id,start,stop,profile_id,model_id,iaas_id) VALUES(#{app_id},#{deploy_id},#{vm_id},#{start},#{stop},#{profile_id},#{model_id},#{iaas_id})")
	  void createVM(VirtualMachine vm);
	  
	  @Update("UPDATE APPLICATION_REGISTRY set stop=#{stop} WHERE app_id=#{app_id}  and deploy_id = #{deploy_id} and vm_id = #{vm_id} ")
	  void stopVM(VirtualMachine vm);
	  
	  @Update("UPDATE APPLICATION_REGISTRY set model_id=#{model_id} WHERE #{app_id} and deploy_id = #{deploy_id} and vm_id = #{vm_id} ")
	  void setModel(VirtualMachine vm);
	  
	  @Update("UPDATE APPLICATION_REGISTRY set profile_id=#{profile_id} WHERE #{app_id} and deploy_id = #{deploy_id} and vm_id = #{vm_id} ")
	  void setProfile(VirtualMachine vm);
	  
	  @Select("SELECT COUNT(*) FROM APPLICATION_REGISTRY WHERE app_id = #{app_id} and deploy_id = #{deploy_id} and vm_id = #{vm_id} ")
	  int checkVM(@Param("app_id") String app_id,@Param("deploy_id") int deploy_id,@Param("vm_id") int vm_id);
	  
	  @Select("SELECT COUNT(*) FROM APPLICATION_REGISTRY WHERE iaas_id = #{iaas_id} ")
	  int checkIaaSVM(@Param("iaas_id") String iaas_id);
	
	  @Select("SELECT iaas_id FROM APPLICATION_REGISTRY WHERE deploy_id = #{deploy_id} and vm_id = #{vm_id} ")
	  String selectFromIaaSID(@Param("deploy_id") String deploy_id,@Param("vm_id") String vm_id);
	  
	  @Update("UPDATE APPLICATION_REGISTRY set energy=#{energy} WHERE #{app_id} and deploy_id = #{deploy_id} and vm_id = #{vm_id} ")
	  void updateEnergy(@Param("app_id") String app_id, @Param("deploy_id") String deploy_id,@Param("vm_id") String vm_id,@Param("energy") double energy);

	  @Update("UPDATE APPLICATION_REGISTRY set power=#{power} WHERE #{app_id} and deploy_id = #{deploy_id} and vm_id = #{vm_id}  ")
	  void updatePower(@Param("app_id") String app_id, @Param("deploy_id") String deploy_id,@Param("vm_id") String vm_id,@Param("energy") double power);

	  
}
