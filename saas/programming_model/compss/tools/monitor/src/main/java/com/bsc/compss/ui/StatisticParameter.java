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


public class StatisticParameter {
    private String name;
    private String value;
    private String defaultValue;
    
    
    public StatisticParameter() {   	
    	this.setName("");				//Any
    	this.setValue("");				//Any
    	this.setDefaultValue("");		//Any
    }
    
    public StatisticParameter(String name, String value) {
		this.setName(name);
		this.setValue(value);
		this.setDefaultValue(value);
    }
    
    public StatisticParameter(String name, String value, String defaultValue) {
		this.setName(name);
		this.setValue(value);
		this.setDefaultValue(defaultValue);
    }

	public String getName() {
		return this.name;
	}
	
	public void setName (String name) {
		this.name = name;
	}
	
	public String getValue () {
		return this.value;
	}
	
	public void setValue (String value) {
		this.value = value;
	}
	
	public String getDefaultValue() {
		return this.defaultValue;
	}

	public void setDefaultValue (String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public void reset() {
		this.value = this.defaultValue;
	}
}
