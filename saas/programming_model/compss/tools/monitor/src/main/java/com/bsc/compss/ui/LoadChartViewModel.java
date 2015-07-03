package com.bsc.compss.ui;

import org.apache.log4j.Logger;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.util.Clients;

import monitoringParsers.ResourcesLogParser;


public class LoadChartViewModel {
	private String divUUID; //ZUL div UUID's
	private String chartType;

	//Logger
	private static final Logger logger = Logger.getLogger("compssMonitor.LoadChartVM");

	@Init
	public void init() {
		this.divUUID = new String("");
		this.chartType = new String(Constants.TOTAL_LOAD_CHART);
	}
    
    @Command
    public void setDivUUID(@BindingParam("divuuid") String divuuid) {
    	this.divUUID = divuuid;
    }
    
    public String getChartType() {
    	return this.chartType;
    }
    
    public void setChartType(String chartType) {
    	this.chartType = chartType;
    	draw();
    }
	
    @Command
	public void update() {
    	logger.debug("Updating Load Chart View Model...");
		ResourcesLogParser.parse();
		draw();
    	logger.debug("Load Chart View Model updated");
	}
    
    @Command
	public void clear() {
		this.chartType = Constants.TOTAL_LOAD_CHART;
		ResourcesLogParser.clear();
    	draw();
    }
    
    private void draw() {
    	if (!this.divUUID.equals("")) {
	    	//Total Load
	    	if (this.chartType.equals(Constants.TOTAL_LOAD_CHART)) {
	    		Clients.evalJavaScript("drawTotalLoadChart('" + this.divUUID + "'," + ResourcesLogParser.getTotalLoad() + ");");
	    	} else if (this.chartType.equals(Constants.LOAD_PER_CORE_CHART)) {
	    		Clients.evalJavaScript("drawLoadPerCoreChart('" + this.divUUID + "'," + ResourcesLogParser.getLoadPerCore() + ");");
	    	} else if (this.chartType.equals(Constants.TOTAL_RUNNING_CHART)) {
	    		Clients.evalJavaScript("drawTotalCores('" + this.divUUID + "'," + ResourcesLogParser.getTotalRunningCores() + ");");
	    	} else if (this.chartType.equals(Constants.RUNNING_PER_CORE_CHART)) {
	    		Clients.evalJavaScript("drawCoresPerCoreChart('" + this.divUUID + "'," + ResourcesLogParser.getRunningCoresPerCore() + ");");
	    	} else if (this.chartType.equals(Constants.TOTAL_PENDING_CHART)) {
	    		Clients.evalJavaScript("drawTotalCores('" + this.divUUID + "'," + ResourcesLogParser.getTotalPendingCores() + ");");
	    	} else if (this.chartType.equals(Constants.PENDING_PER_CORE_CHART)) {
	    		Clients.evalJavaScript("drawCoresPerCoreChart('" + this.divUUID + "'," + ResourcesLogParser.getPendingCoresPerCore() + ");");
	    	} else if (this.chartType.equals(Constants.RESOURCES_STATUS_CHART)) {
	    		Clients.evalJavaScript("drawTotalResourcesStatusChart('" + this.divUUID + "'," + ResourcesLogParser.getResourcesStatus() + ");");
	    	} else {
	    		logger.debug("Invalid chart Type");
	    		Clients.evalJavaScript("drawEmpty('" + this.divUUID + "');");
	    	}
    	} else {
    		logger.debug("DivUUID not found. Cannot render chart.");
    	}
    }
	
}
