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
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.ListModelList;


public class ConfigurationViewModel {
	private static final Logger logger = Logger.getLogger("compssMonitor.ConfigurationVM");
	private List<ConfigParam> configurations;
 
	//Define Refresh time class
	private class refreshTime extends ConfigParam {		
		public refreshTime(String name, String value, boolean editing) {
			super(name, value, editing);
		}
		
		public void update() {
			//Actions after UI update
			logger.debug("Refresh time update.");
			
			int ms = Integer.valueOf(this.getValue())*1000;
			if ((ms > 0) && (ms < 60000)) {
				logger.debug("   New refresh time = " + ms + " ms");
				Properties.REFRESH_TIME = ms;
			} else {
				logger.debug("   Refresh time out of bounds: " + ms + " ms");
				this.setValue(String.valueOf((Properties.REFRESH_TIME)/1000));
			}
		}
	}
	
	//Define Sort Applications class
	private class sortApplications extends ConfigParam {	
		public sortApplications(String name, String value, boolean editing) {
			super(name, value, editing);
		}
		
		public void update() {
			//Actions after UI update
			logger.debug("Sort Applications update.");
			boolean newValue = Boolean.valueOf(this.getValue());
			logger.debug("   New sort application value = " + newValue);
			Properties.SORT_APPLICATIONS = newValue;
		}
	}
	
	//Define load Graph x-scale class
	private class loadGraphXScale extends ConfigParam {
		public loadGraphXScale(String name, String value, boolean editing) {
			super(name, value, editing);
		}
		
		public void update() {
			//Actions after UI update
			logger.debug("Load Graph x-Scale update");
			int newValue = Integer.valueOf(this.getValue());
			if (newValue >= 1) {
				logger.debug("   New load Graph x-Scale update = " + newValue);
				Properties.LOAD_GRAPH_X_SCALE = newValue;
			} else {
				logger.debug("   The load graph value isn't correct. Reverting value.");
				this.setValue(String.valueOf(Properties.LOAD_GRAPH_X_SCALE));
			}
		}
	}
	
    @Init
    public void init() {
    	logger.debug("Loading configurable parameters...");
       	configurations = new LinkedList<ConfigParam> ();

    	//Add Refresh Time
    	refreshTime rt = new refreshTime("Refresh Time (s)", String.valueOf((Properties.REFRESH_TIME)/1000), false);
    	configurations.add(rt);
    	//Add Sort Applications
    	sortApplications sa = new sortApplications("Sort applications (true/false)", String.valueOf(Properties.SORT_APPLICATIONS), false);
    	configurations.add(sa);
    	//Add LoadGraph X-Scale
    	loadGraphXScale lgxs = new loadGraphXScale("Load Graph's X-Scale factor (int >= 1)", String.valueOf(Properties.LOAD_GRAPH_X_SCALE), false);
    	configurations.add(lgxs);
    	
    	logger.debug("Configurable parameters loaded");
    }
    
    public List<ConfigParam> getConfigurations() {
    	return new ListModelList<ConfigParam>(this.configurations);
    }
    
    @Command
    public void changeEditableStatus(@BindingParam("ConfigParam") ConfigParam cp) {
    	cp.setEditingStatus(!cp.getEditingStatus());
    	refreshRowTemplate(cp);
    }
    
    @Command
    public void confirm(@BindingParam("ConfigParam") ConfigParam cp) {
    	cp.update();
    	changeEditableStatus(cp);
    	refreshRowTemplate(cp);
    }
    
    public void refreshRowTemplate(ConfigParam cp) {
    	BindUtils.postNotifyChange(null, null, cp, "editingStatus");
    } 
    
}
