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

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.CpuFeatures;
/**
 * 
 * @author M. Fontanella
 * represent data stored inside the database about CPU features (model, tdp, maxpower, minpower, ...)
 */
public interface CpuFeaturesMapper {
	
	//model | tdp | minpower | maxpower
	  @Select("SELECT * FROM CPUFEATURES WHERE model = #{model}")
	  CpuFeatures selectByModel(@Param("model")String model);
	  
	  @Select("SELECT COUNT(*) FROM CPUFEATURES WHERE model = #{model}")
	  int checkCpuModel(@Param("model") String model);
}

