/*
 *  Copyright 2002-2012 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package integratedtoolkit.types.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Constraints {

	String 	processorArchitecture() 	default "[unassigned]";
	int 	processorCPUCount()		default 0;
	int 	processorCoreCount()		default 1;
	float 	processorSpeed()		default 0;// in GHz
	
	float 	memoryPhysicalSize()		default 0;// in GB
	float 	memoryVirtualSize()		default 0;// in GB
	float	memoryAccessTime()		default 0;// in ns
	float 	memorySTR()			default 0;// in GB/s
	
	float 	storageElemSize() 		default 0;// in GB
	float	storageElemAccessTime()		default 0;// in ms
	float 	storageElemSTR()		default 0;// in MB/s
	
	String 	operatingSystemType()		default "[unassigned]";	
	String 	hostQueue() 			default "[unassigned]";
	String 	appSoftware() 			default "[unassigned]";

}
