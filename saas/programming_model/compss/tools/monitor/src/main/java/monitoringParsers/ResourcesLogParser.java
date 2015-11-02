/**
 *
 *   Copyright 2013-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package monitoringParsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.bsc.compss.ui.Constants;
import com.bsc.compss.ui.Properties;
import com.bsc.compss.ui.LoadData;
import com.bsc.compss.ui.DescriptionData;


public class ResourcesLogParser {
	private static Vector<LoadData> dataCurrentLoad = new Vector<LoadData> ();
	private static Vector<DescriptionData> dataDescription = new Vector<DescriptionData> ();
	private static String resourcesLogPath = "";
	
	private static int lastParsedLine = -1;
	private static long referenceTimestamp = 0L;
	private static long lastSeenTimestamp = 0L;
	private static int scaleTimeStamp = 0;						//To scale X-axe for long applications
	private static boolean processInformation = false;
	
	private static final Logger logger = Logger.getLogger("compssMonitor.monitoringParser");

	
	// Format: Each entry separated by " ". Entry = "time:totalLoad:numResources" (int:float:int)
	public static String getTotalLoad() {
		StringBuilder result = new StringBuilder("'");
	    for (int i = 0; i < dataCurrentLoad.size(); i++) {
	    	if (i != 0) {
	    		result.append(" ");
	    	}
	    	result.append(dataCurrentLoad.get(i).getTimestamp()).append(":").append(dataCurrentLoad.get(i).getTotalLoad()).append(":").append(dataCurrentLoad.get(i).getTotalResources());
	    }
	    result.append("'");
	    
	    logger.debug("TotalLoadPoints: " + result.toString());
	    return result.toString();    	    
	}
	
	// Format: Each entry separated by " ". Entry = "time:loadC0:...:loadCN:numResources" (int:float:...:int)
	public static String getLoadPerCore() {
		StringBuilder coreData = new StringBuilder("'");
		int maxCores = 0;
	    for (int i = 0; i < dataCurrentLoad.size(); i++) {
	    	if (i != 0) {
	    		coreData.append(" ");
	    	}
	    	coreData.append(dataCurrentLoad.get(i).getTimestamp());
	    	maxCores = Math.max(maxCores, dataCurrentLoad.get(i).getLoadInfo().size());
	    	for (Float coreLoad : dataCurrentLoad.get(i).getLoadInfo()) {
	    		if (coreLoad != null) {
	    			coreData.append(":").append(coreLoad);
	    		} else {
	    			coreData.append(":").append(0);
	    		}
	    	}
	    	coreData.append(":").append(dataCurrentLoad.get(i).getTotalResources());
	    }
	    coreData.append("'");
	    
	    logger.debug("LoadPerCorePoints: " + coreData.toString());
	    logger.debug("LoadPerCorePointsMAXCORES: " + maxCores);
	    return "'" + String.valueOf(maxCores) + "'," + coreData.toString();    	    
	}
	
	// Format: Each entry separated by " ". Entry = "time:totalRunningCores:numResources" (int:int:int)
	public static String getTotalRunningCores() {
		StringBuilder result = new StringBuilder("'");
	    for (int i = 0; i < dataCurrentLoad.size(); i++) {
	    	if (i != 0) {
	    		result.append(" ");
	    	}
	    	result.append(dataCurrentLoad.get(i).getTimestamp()).append(":").append(dataCurrentLoad.get(i).getTotalCoresRunning()).append(":").append(dataCurrentLoad.get(i).getTotalResources());
	    }
	    result.append("'");
	    
	    logger.debug("TotalRunningCoresPoints: " + result.toString());
	    return result.toString();  
	}
	
	// Format: Each entry separated by " ". Entry = "time:#runningCore0:...:numResources" (int:int:...:int)
	public static String getRunningCoresPerCore() {
		StringBuilder coreData = new StringBuilder("'");
		int maxCores = 0;
	    for (int i = 0; i < dataCurrentLoad.size(); i++) {
	    	if (i != 0) {
	    		coreData.append(" ");
	    	}
	    	coreData.append(dataCurrentLoad.get(i).getTimestamp());
	    	maxCores = Math.max(maxCores, dataCurrentLoad.get(i).getRunningCoresInfo().size());
	    	for (Integer numRunning : dataCurrentLoad.get(i).getRunningCoresInfo()) {
	    		if (numRunning != null) {
	    			coreData.append(":").append(numRunning);
	    		} else {
	    			coreData.append(":").append(0);
	    		}
	    	}
	    	coreData.append(":").append(dataCurrentLoad.get(i).getTotalResources());
	    }
	    coreData.append("'");
	    
	    logger.debug("RunningCoresPerCorePoints: " + coreData.toString());
	    logger.debug("RunningCoresPerCorePointsMAXCORES: " + maxCores);
	    return "'" + String.valueOf(maxCores) + "'," + coreData.toString();  
	}
	
	// Format: Each entry separated by " ". Entry = "time:totalPendingCores:numResources" (int:int:int)
	public static String getTotalPendingCores() {
		StringBuilder result = new StringBuilder("'");
	    for (int i = 0; i < dataCurrentLoad.size(); i++) {
	    	if (i != 0) {
	    		result.append(" ");
	    	}
	    	result.append(dataCurrentLoad.get(i).getTimestamp()).append(":").append(dataCurrentLoad.get(i).getTotalCoresPending()).append(":").append(dataCurrentLoad.get(i).getTotalResources());
	    }
	    result.append("'");
	    
	    logger.debug("TotalPendingCoresPoints: " + result.toString());
	    return result.toString();  
	}
	
	// Format: Each entry separated by " ". Entry = "time:#pendingCore0:...:numResources" (int:int:...:int)
	public static String getPendingCoresPerCore() {
		StringBuilder coreData = new StringBuilder("'");
		int maxCores = 0;
	    for (int i = 0; i < dataCurrentLoad.size(); i++) {
	    	if (i != 0) {
	    		coreData.append(" ");
	    	}
	    	coreData.append(dataCurrentLoad.get(i).getTimestamp());
	    	maxCores = Math.max(maxCores, dataCurrentLoad.get(i).getPendingCoresInfo().size());
	    	for (Integer numPending : dataCurrentLoad.get(i).getPendingCoresInfo()) {
	    		if (numPending != null) {
	    			coreData.append(":").append(numPending);
	    		} else {
	    			coreData.append(":").append(0);
	    		}
	    	}
	    	coreData.append(":").append(dataCurrentLoad.get(i).getTotalResources());
	    }
	    coreData.append("'");
	    
	    logger.debug("PendingCoresPerCorePoints: " + coreData.toString());
	    logger.debug("PendingCoresPerCorePointsMAXCORES: " + maxCores);
	    return "'" + String.valueOf(maxCores) + "'," + coreData.toString();  
	}
	
	// Format: Last entry only. Entry = "time:CPU:MEM" (int:int:int)
	public static String getResourcesStatus() {
		StringBuilder result = new StringBuilder("'");
	    result.append(dataDescription.lastElement().getTimestamp()).append(":");
	    result.append(dataDescription.lastElement().getTotalCPUConsumption()).append(":");
	    result.append(dataDescription.lastElement().getTotalMemoryConsumption());
	    result.append("'");
	    
	    if (logger.isDebugEnabled()) {
	    	logger.debug("ResourcesStatusPoints: " + result.toString());
	    }
	    return result.toString(); 
	}
	
	
	public static void parse () {
		logger.debug("Parsing resources.log file...");
    	if (!Properties.BASE_PATH.equals("")) {
    		//Check if applicaction has changed
    		String newPath = Properties.BASE_PATH + File.separator + Constants.RESOURCES_LOG;
    		if (!resourcesLogPath.equals(newPath)) {
    			//Load new application
    			clear();
    			resourcesLogPath = newPath;
    		}
    		//Parse
    		try {
    			FileReader fr = new FileReader(resourcesLogPath);
    			BufferedReader br = new BufferedReader(fr);
    			String line = br.readLine();				//Parsed line
    			int i = 0;									//Line counter
    			while (line != null) {
    				if (i > lastParsedLine) {
    					//Check line information and add to structures
    					if (line.contains("TIMESTAMP = ")) {
    						logger.debug("* Timestamp flag");
    						scaleTimeStamp++;
    						if (scaleTimeStamp >= Properties.LOAD_GRAPH_X_SCALE) {
    							processInformation = true;
    							scaleTimeStamp = 0;
        						lastSeenTimestamp = Long.valueOf(line.substring(line.lastIndexOf("=") + 2));
        						if (dataCurrentLoad.isEmpty()) {
        							referenceTimestamp = lastSeenTimestamp;
        						}
    						} else {
    							processInformation = false;
    						}
    					}
    					if (processInformation) {
	    					if (line.contains("LOAD_INFO = [")) {
	    						logger.debug("* Load Information flag");
	    						dataCurrentLoad.add(new LoadData(((int)(lastSeenTimestamp - referenceTimestamp))/1000)); //seconds
	    						line = br.readLine();
	    						i = i + 1;
	    						while ((line != null) && (line.contains("CORE_INFO = ["))) {
									line = br.readLine();
									i = i + 1;
									int id = Integer.valueOf(line.substring(line.lastIndexOf("=") + 2));
									line = br.readLine();
									i = i + 1;
									int no_resource = Integer.valueOf(line.substring(line.lastIndexOf("=") + 2));
									line = br.readLine();
									i = i + 1;
									int to_reschedule = Integer.valueOf(line.substring(line.lastIndexOf("=") + 2));
									line = br.readLine();
									i = i + 1;
									int ordinary = Integer.valueOf(line.substring(line.lastIndexOf("=") + 2));
									//Skip min
									line = br.readLine();
									i = i + 1;
									//Get mean
									line = br.readLine();
									i = i + 1;
									int mean = Integer.valueOf(line.substring(line.lastIndexOf("=") + 2));
									//Skip max
									line = br.readLine();
									i = i + 1;
									//Skip ] of CORE_INFO
									line = br.readLine();
									i = i + 1;
									//Add information
									float load = Float.valueOf((no_resource + to_reschedule + ordinary)*(mean))/Float.valueOf(1000); //seconds
									dataCurrentLoad.lastElement().addCoreLoad(id, load);
									dataCurrentLoad.lastElement().addCorePending(id, no_resource + to_reschedule + ordinary);
									//Loop
									line = br.readLine();
				    				i = i + 1;
	    						}
	    					} else if (line.contains("RESOURCES_INFO = [")) {
	    						logger.debug("* Resources Information flag");
	    						line = br.readLine();
	    						i = i + 1;
	    						while ((line != null) && (line.contains("RESOURCE = ["))) {
									line = br.readLine();
									i = i + 1;
									String resourceName = line.substring(line.lastIndexOf("=") + 2);
									//Add resource information
									dataCurrentLoad.lastElement().addResource(resourceName);
									//Process HOST_INFO
									line = br.readLine(); //HOST_INFO = [
									i = i +1;
									line = br.readLine(); //CORE = [ or ]
									i = i +1;
									while ((line != null) && (line.contains("CORE = ["))) {
										line = br.readLine(); //COREID
										i = i +1;
										int coreId = Integer.valueOf(line.substring(line.lastIndexOf("=") + 2));
										line = br.readLine(); //RUNNING
										i = i +1;
										int running = Integer.valueOf(line.substring(line.lastIndexOf("=") + 2));
										line = br.readLine(); //ELAPSED_TIME
										i = i +1;
										//Skip ] of CORE
										line = br.readLine();
										i = i +1;
										//Add core information to resource
										dataCurrentLoad.lastElement().addCoreRunning(coreId, running);
										//Loop
										line = br.readLine();
										i = i +1;
									}
									//Skip ] of RESOURCE
									line = br.readLine();
									i = i + 1;
									//Loop
									line = br.readLine();
				    				i = i + 1;
	    						}
	    					} else if (line.contains("RESOURCES = [")) {
	    						logger.debug("* Resources Description flag");
	    						dataDescription.add(new DescriptionData(((int)(lastSeenTimestamp - referenceTimestamp))/1000)); //seconds
	    						line = br.readLine();
	    						i = i + 1;
	    						while ((line != null) && (line.contains("RESOURCE = ["))) {
	    							line = br.readLine();
									i = i + 1;
	    							String resourceName = line.substring(line.lastIndexOf("=") + 2);
	    							//Skip executable cores
	    							line = br.readLine();
									i = i + 1;
									//Skip core simultaneous tasks
									line = br.readLine();
									i = i + 1;
									line = br.readLine();
									i = i + 1;
									while ((line != null) && (line.contains("CORE = ["))) {
										//Coreid + simtasks
										line = br.readLine();
										i = i + 1;
										line = br.readLine();
										i = i + 1;
										//] of CORE
										line = br.readLine();
										i = i + 1;
										//Loop
										line = br.readLine();
					    				i = i + 1;
									}
									//Skip implementation simultaneous tasks
									line = br.readLine();
									i = i + 1;
									line = br.readLine();
									i = i + 1;
									while ((line != null) && (line.contains("IMPLEMENTATION = ["))) {
										//CoreId + implId + simtasks
										line = br.readLine();
										i = i + 1;
										line = br.readLine();
										i = i + 1;
										line = br.readLine();
										i = i + 1;
										//] of IMPLEMENTATION
										line = br.readLine();
										i = i + 1;
										//Loop
										line = br.readLine();
					    				i = i + 1;
									}
									//Get type
									line = br.readLine();
									i = i + 1;
									String type = line.substring(line.lastIndexOf("=") + 2);
									//Switch between worker or service
									if (type.equals("WORKER")) {
										line = br.readLine();
										i = i + 1;
										int cpu = Integer.valueOf(line.substring(line.lastIndexOf("=") + 2));
										line = br.readLine();
										i = i + 1;
										float memory = Float.valueOf(line.substring(line.lastIndexOf("=") + 2));
										dataDescription.lastElement().addResource(resourceName, type, cpu, memory);
									} else if (type.equals("SERVICE")) {
										dataDescription.lastElement().addResource(resourceName, type);
									}
									//Skip set
									line = br.readLine();
									i = i + 1;
	    							//Skip ] of RESOURCE
									line = br.readLine();
									i = i + 1;
									//Loop
									line = br.readLine();
				    				i = i + 1;
	    						}
	    					}
	    				}
    				}
    				line = br.readLine();
    				i = i + 1;
    			}
    			lastParsedLine = i - 1;
    			br.close();
    			fr.close();
    		} catch (Exception e) {
    			clear();
    			logger.error("Cannot parse resrouces.log file: " + resourcesLogPath);
    		}
    	} else {
    		//Load default value
    		clear();
    	}
    	logger.debug("resources.log file parsed");
    }
	
	public static void clear() {  	
    	resourcesLogPath = "";
    	
    	lastParsedLine = -1;
    	referenceTimestamp = 0L;
		lastSeenTimestamp = 0L;
		scaleTimeStamp = 0;
		processInformation = false;
    	
    	dataCurrentLoad.clear();
    	dataDescription.clear();
	}
	
}

