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
    	
    	//Add accumulated cost, energy and time
    	StatisticParameter accumulatedCost = new StatisticParameter("Cost", "0.0", "0.0");
    	StatisticParameter accumulatedEnergy = new StatisticParameter("Energy", "0.0", "0.0");
    	StatisticParameter elapsedTime = new StatisticParameter("Elapsed Time", "0.0", "0.0");
    	

    	statistics.add(accumulatedCost);
    	statistics.add(accumulatedEnergy);
    	statistics.add(elapsedTime);
    }
    
    public List<StatisticParameter> getStatistics () {
    	return new ListModelList<StatisticParameter>(this.statistics);
    }
  
    @Command
    @NotifyChange("statistics")
    public void update (String[] statisticsParameters) {
    	String[] units = new String[statisticsParameters.length];
    	
    	units[0] = " €";
    	units[1] = " Wh";
    	units[2] = " s";
    	
    	logger.debug("Updating Statistics ViewModel...");
    	//Erase all current resources
    	for (StatisticParameter param : statistics) {
    		param.reset();
    	}
    	
    	//Import new values
    	for (int i = 0; i < statistics.size(); ++i) {
    		statistics.get(i).setValue(statisticsParameters[i] + units[i]);
    	}
    	
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
