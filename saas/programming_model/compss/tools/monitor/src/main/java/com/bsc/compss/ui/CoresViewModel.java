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

package com.bsc.compss.ui;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.ListModelList;

public class CoresViewModel {
	private List<Core> cores;
	private static final Logger logger = Logger.getLogger("compssMonitor.TasksVM");

    @Init
    public void init() {
    	cores = new LinkedList<Core>();   	
    }
    
    public List<Core> getCores() {
    	return new ListModelList<Core>(this.cores);
    }
  
    @Command
    @NotifyChange("cores")
    public void update(List<String[]> newCoreData) {
    	logger.debug("Updating Tasks ViewModel...");
    	//Erase all current resources
    	cores.clear();
    	
    	//Import new resources
    	for (String[] dc : newCoreData) {
    		//Check color
    		int i = Integer.parseInt(dc[0]) % Constants.CORE_COLOR_MAX;
    		String color = File.separator + "images" + File.separator + "colors" + File.separator + i +".png";
			
			//                color, name,  params, avgExecTime, executedCount)
    		Core c = new Core (color, dc[1], dc[2], dc[3], dc[4]);
    		cores.add(c);
    	}
    	logger.debug("Tasks ViewModel updated");
    }
    
    @Command
    @NotifyChange("cores")
    public void clear() {
    	cores.clear();
    }

}
