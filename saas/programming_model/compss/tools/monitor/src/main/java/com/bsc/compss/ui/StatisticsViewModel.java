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

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.ListModelList;


public class StatisticsViewModel {
	private List<StatisticParameter> statistics;
	private static final Logger logger = Logger.getLogger("compssMonitor.StatisticsVM");

    @Init
    public void init () {
    	statistics = new LinkedList<StatisticParameter>();
    	
    	//Add accumulated cost
    	//StatisticParameter accumulatedCost = new StatisticParameter("Accumulated Cost", "0.0", "0.0");
    	//statistics.add(accumulatedCost);
    }
    
    public List<StatisticParameter> getStatistics () {
    	return new ListModelList<StatisticParameter>(this.statistics);
    }
  
    @Command
    @NotifyChange("statistics")
    public void update (List<StatisticParameter> statisticsParameters) {
    	logger.debug("Updating Statistics ViewModel...");
    
    		statistics=statisticsParameters;
       	
    	logger.debug("Statistics ViewModel updated");
    }
    
    @Command
    @NotifyChange("statistics")
    public void clear() {
    	//Erase all current resources
    	for (StatisticParameter param : statistics) {
    		param.reset();
    	}
    }

}
