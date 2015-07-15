package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface AppRegistryMapper {

	  @Select("SELECT * FROM APPLICATION_REGISTRY WHERE app_id = #{app_id}")
	  List<VirtualMachine> selectByApp(@Param("app_id")int app_id);

	  @Select("SELECT * FROM APPLICATION_REGISTRY WHERE app_id = #{app_id} and deploy_id = #{deploy_id}")
	  List<VirtualMachine> selectByDeploy(@Param("app_id")int app_id,@Param("deploy_id") int deploy);
	  
	  @Select("SELECT * FROM APPLICATION_REGISTRY WHERE app_id = #{app_id} and deploy_id = #{deploy_id} and vm_id = #{vm_id} ")
	  VirtualMachine selectByVmp(@Param("app_id") int app_id,@Param("deploy_id") int deploy_id,@Param("vm_id") int vm_id);
	  
	  @Select("SELECT * FROM APPLICATION_REGISTRY WHERE iaas_id = #{iaas_id} ")
	  VirtualMachine selectByIaaSId(@Param("iaas_id") String iaas_id);
	  
	  @Insert("INSERT INTO APPLICATION_REGISTRY (app_id,deploy_id,vm_id,start,stop,profile_id,model_id,iaas_id) VALUES(#{app_id},#{deploy_id},#{vm_id},#{start},#{stop},#{profile_id},#{model_id},#{iaas_id})")
	  void createVM(VirtualMachine vm);
	  
	  @Update("UPDATE APPLICATION_REGISTRY set stop=#{stop} WHERE #{app_id} and deploy_id = #{deploy_id} and vm_id = #{vm_id} ")
	  void stopVM(VirtualMachine vm);
	  
	  @Update("UPDATE APPLICATION_REGISTRY set model_id=#{model_id} WHERE #{app_id} and deploy_id = #{deploy_id} and vm_id = #{vm_id} ")
	  void setModel(VirtualMachine vm);
	  
	  @Update("UPDATE APPLICATION_REGISTRY set profile_id=#{profile_id} WHERE #{app_id} and deploy_id = #{deploy_id} and vm_id = #{vm_id} ")
	  void setProfile(VirtualMachine vm);
	
}
