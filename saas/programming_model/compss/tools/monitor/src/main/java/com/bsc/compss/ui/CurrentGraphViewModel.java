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
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Filedownload;


public class CurrentGraphViewModel {
	private String graph;
	private String graphLastUpdateTime;
	private static final Logger logger = Logger.getLogger("compssMonitor.GraphVM");

    @Init
    public void init() {
    	graph = new String();
    	graphLastUpdateTime = new String();
    }
    
    public String getGraph() {
    	return this.graph;
    }
    
    @Command
    public void download() {
    	try {
    		if ((graph.equals(Constants.GRAPH_NOT_FOUND_PATH)) 
    			|| (graph.equals(Constants.GRAPH_EXECUTION_DONE_PATH))
    			|| (graph.equals(Constants.EMPTY_GRAPH_PATH))) {
    			Filedownload.save(graph, null);
    		} else {
    			Filedownload.save(graph.substring(0, graph.lastIndexOf("?")), null);
    		}
    	} catch (Exception e) {
    		//Cannot download file. Nothing to do
    		logger.error("Cannot download current graph");
    	}
    }
    
    @Command
    @NotifyChange("graph")
    void update (Application monitoredApp) {
    	logger.debug("Updating Graph...");
    	String monitorLocation = monitoredApp.getPath() + Constants.MONITOR_CURRENT_DOT_FILE;
    	
    	File monitorFile = new File(monitorLocation);
		if (monitorFile.exists()) {
			if (monitorFile.length() > 45) {
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				String modifiedTime = sdf.format(monitorFile.lastModified());
				if (!modifiedTime.equals(graphLastUpdateTime)) {
			    	try {
			    		String graphSVG = File.separator + "svg" + File.separator + monitoredApp.getName() + "_" + Constants.GRAPH_FILE_NAME;
			    		graph = loadGraph(monitorLocation, graphSVG);
			    		graphLastUpdateTime = modifiedTime;
			    	} catch (Exception e) {
			    		graph = Constants.GRAPH_NOT_FOUND_PATH;
			    		graphLastUpdateTime = "";
			    		logger.error("Graph generation error");
			    	}
				} else {
					logger.debug("Graph is already loaded");
				}
			} else {
				//The empty graph has length = 45
				graph = Constants.GRAPH_EXECUTION_DONE_PATH;
				graphLastUpdateTime = "";
			}
		} else {
			graph = Constants.GRAPH_NOT_FOUND_PATH;
			graphLastUpdateTime = "";
			logger.debug("Graph file not found");
    	}
    }
    
    @Command
    @NotifyChange("graph")
    public void clear() {
    	graph = Constants.EMPTY_GRAPH_PATH;
    	graphLastUpdateTime = "";
    }
    
    private String loadGraph(String location, String target) throws Exception {
    	if (logger.isDebugEnabled()) {
	    	logger.debug("Loading Graph...");
	    	logger.debug("   - Monitoring source: " + location);
	    	logger.debug("   - Monitoring target: " + target);
    	}
    	//Create SVG
    	String[] createSVG = {
    			"/bin/sh",
    			"-c",
    			"dot -T svg "+ location +" > " + System.getProperty("catalina.base") + File.separator + "webapps" + File.separator + "compss-monitor" + File.separator + target};
		Process p1 = Runtime.getRuntime().exec(createSVG);
		p1.waitFor();
		
		//Add JSPan.js configuration
		String[] addJSScript = {
				"/bin/sh",
				"-c",
				"sed -i \"s/\\<g id\\=\\\"graph0/script xlink:href\\=\\\"SVGPan.js\\\"\\/\\>\\n\\<g id\\=\\\"viewport/\" " + System.getProperty("catalina.base") + File.separator + "webapps" + File.separator + "compss-monitor" + File.separator + target};
		Process p2 = Runtime.getRuntime().exec(addJSScript);
		p2.waitFor();

		String[] createViewBox = {
				"/bin/sh",
				"-c",
				"sed -i \"s/<svg .*/<svg xmlns\\=\\\"http:\\/\\/www.w3.org\\/2000\\/svg\\\" xmlns:xlink\\=\\\"http:\\/\\/www.w3.org\\/1999\\/xlink\\\"\\>/g\" " + System.getProperty("catalina.base") + File.separator + "webapps" + File.separator + "compss-monitor" + File.separator + target};
		Process p3 = Runtime.getRuntime().exec(createViewBox);
		p3.waitFor();

    	//Load graph image
		logger.debug("Graph loaded");
		return target + "?t=" + System.currentTimeMillis();
    }

}
