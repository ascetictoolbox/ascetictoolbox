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


public class Core {
	private String color;
    private String name;
    private String parameters;
    private String avgExecTime;
    private String executedCount; 

    
    public Core() {		
    	this.setColor(File.separator + "images" + File.separator + "colors" + File.separator + Constants.CORE_COLOR_DEFAULT + ".png");	//Empty Image
    	this.setName("");															//Any
    	this.setParameters("");														//Any
    	this.setAvgExecTime("0.0");													//Float
    	this.setExecutedCount("0");													//Int
    }
    
    public Core(String color, String name, String params, String avgExecTime, String executedCount) {
    	this.setColor(color);
    	this.setName(name);
    	this.setParameters(params);
    	this.setAvgExecTime(avgExecTime);
    	this.setExecutedCount(executedCount);
    }

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getAvgExecTime() {
		return avgExecTime;
	}

	public void setAvgExecTime(String avgExecTime) {
		this.avgExecTime = avgExecTime;
	}

	public String getExecutedCount() {
		return executedCount;
	}

	public void setExecutedCount(String executedCount) {
		this.executedCount = executedCount;
	}

}
