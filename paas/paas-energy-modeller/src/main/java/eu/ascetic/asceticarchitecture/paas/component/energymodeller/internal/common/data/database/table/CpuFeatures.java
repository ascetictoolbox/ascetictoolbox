/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table;

//model | tdp | minpower | maxpower

public class CpuFeatures {

	private String model;
	private int core;
	private double tdp;
	private double minpower;
	private double maxpower;	
	
	
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	
	public int getCore() {
		return core;
	}
	public void setCore(int core) {
		this.core = core;
	}
	
	public double getTdp() {
		return tdp;
	}
	public void setTdp(double tdp) {
		this.tdp = tdp;
	}
	
	public double getMinPower() {
		return minpower;
	}
	public void setMinPower(double minpower) {
		this.minpower = minpower;
	}
	
	public double getMaxPower() {
		return maxpower;
	}
	public void setMaxPower(double maxpower) {
		this.maxpower = maxpower;
	}
	
}


