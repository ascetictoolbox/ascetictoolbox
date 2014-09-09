/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
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

package integratedtoolkit;

public interface ITConstants {

	// Component names
	public static final String IT				= "integratedtoolkit.IntegratedToolkit";
	public static final String TA 				= "Task Analyser";
	public static final String TS 				= "Task Scheduler";
	public static final String JM 				= "Job Manager";
	public static final String DM 				= "Data Manager";
	public static final String DIP 				= "Data Information Provider";
	public static final String FTM 				= "File Transfer Manager";

	// Dynamic system properties
	public static final String IT_APP_NAME 			= "it.appName";
	public static final String IT_PROJ_FILE 		= "it.project.file";
	public static final String IT_PROJ_SCHEMA 		= "it.project.schema";        
	public static final String IT_RES_FILE 			= "it.resources.file";
	public static final String IT_RES_SCHEMA 		= "it.resources.schema";
	public static final String IT_CONSTR_FILE    		= "it.constraints.file";
	public static final String IT_SCHEDULER           	= "it.scheduler";
	public static final String IT_TRACING           	= "it.tracing";
	public static final String IT_PRESCHED			= "it.presched";
	public static final String IT_GRAPH 			= "it.graph";
	public static final String IT_MONITOR			= "it.monitor";
	
	public static final String IT_LANG              	= "it.lang";
	public static final String IT_WORKER_CP         	= "it.worker.cp";
	public static final String IT_CORE_COUNT 		= "it.core.count";
	public static final String IT_SCRIPT_DIR                 = "it.script.dir";
	
	// System properties for Instrumentation flags
	public static final String IT_TO_FILE 			= "it.to.file";
	public static final String IT_IS_WS                     = "it.is.ws";
	public static final String IT_IS_MAINCLASS              = "it.is.mainclass";

	//jorgee: properties for locating the it.properties file
	public static final String IT_CONFIG             	= "it.properties";         
	public static final String IT_CONFIG_LOCATION           = "it.properties.location";      
	//I think this is used just for optimis case
	public static final String IT_CONTEXT                   = "it.context";   

	public static final String LOG4J		 	= "log4j.configuration";

	// Deployment
	public static final String IT_JVM 			= "ITJvm";

	// Initialization
	public static final String INIT_OK 			= "OK";

	// GAT
	public static final String GAT_ADAPTOR			= "gat.adaptor.path";
	public static final String GAT_DEBUG			= "gat.debug";
	public static final String GAT_BROKER_ADAPTOR   	= "it.gat.broker.adaptor";
	public static final String GAT_FILE_ADAPTOR     	= "it.gat.file.adaptor";

	// Project properties
	public static final String INSTALL_DIR 			= "InstallDir";
	public static final String WORKING_DIR 			= "WorkingDir";
	public static final String APP_DIR 			= "AppDir";
	public static final String LIB_PATH 			= "LibraryPath";
	public static final String USER                         = "User";
	public static final String LIMIT_OF_TASKS 		= "LimitOfTasks";
	public static final String LIMIT_OF_JOBS 		= "LimitOfJobs";
	public static final String MAX_CLUSTER_SIZE     	= "MaxClusterSize";
	
	// Language
	public static enum Lang {
		JAVA,
		C,
		PYTHON
	}
	
}
