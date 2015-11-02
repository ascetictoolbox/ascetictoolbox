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

import java.util.Random;
import java.util.Vector;


public class DescriptionData {
	private int timestamp;
	//Global values
	private int totalCPU;
	private float totalMemory;
	
	//Values per core/resource
	private Vector<ResourceInfo> resourcesInformation;
	
	public DescriptionData() {
		this.timestamp = 0;
		this.totalMemory = Float.valueOf(0);
		this.totalCPU = 0;
		
		this.resourcesInformation = new Vector<ResourceInfo>();
	}
	
	public DescriptionData(int timestamp) {
		this.timestamp = timestamp;
		this.totalMemory = Float.valueOf(0);
		this.totalCPU = 0;
		
		this.resourcesInformation = new Vector<ResourceInfo>();
	}
	
	public int getTimestamp() {
		return this.timestamp;
	}
	
	public int getTotalCPUConsumption() {
		float op1 = new Random().nextFloat()*Float.valueOf(this.totalCPU); //TODO	
		float op2 = Float.valueOf(this.totalCPU);
		
		//Protection
		if (op2 == Float.valueOf(0)) {
			return 0;
		}
		
		//Normal result
		float result = (op1/op2)*Float.valueOf(100);
		return (int)result;	
	}
	
	public float getTotalMemoryConsumption() {
		float op1 = new Random().nextFloat()*this.totalMemory; //TODO
		
		//Protection
		if (this.totalMemory == Float.valueOf(0)) {
			return Float.valueOf(0);
		}
		
		//Normal result
		float result = (op1/this.totalMemory)*Float.valueOf(100);
		return result;
	}
	
	public Vector<String> getCPUConsumption() {
		Vector<String> result = new Vector<String>();
		
		for (ResourceInfo resource : this.resourcesInformation) {
			result.add(resource.getName() + ":" + String.valueOf(resource.getCPUConsumption()));
		}
		
		return result;
	}
	
	public Vector<String> getMemoryConsumption() {
		Vector<String> result = new Vector<String>();
		
		for (ResourceInfo resource : this.resourcesInformation) {
			result.add(resource.getName() + ":" + String.valueOf(resource.getMemoryConsumption()));
		}
		
		return result;
	}

	
	public void addResource(String name, String type, int cpu, float memory) {
		this.totalCPU = this.totalCPU + cpu;
		this.totalMemory = this.totalMemory + memory;
		this.resourcesInformation.add(new ResourceInfo(name, type, cpu, memory));
	}
	
	public void addResource(String name, String type) {
		this.resourcesInformation.add(new ResourceInfo(name, type));
	}
	
	//Private classes
	private class ResourceInfo {
		String name;
		String type;
		int cpu;
		float memory;
		
		ResourceInfo(String name, String type) {
			this.name = name;
			this.type = type;
			this.cpu = 0;
			this.memory = Float.valueOf(0);
		}
		
		ResourceInfo(String name, String type, int cpu, float memory) {
			this.name = name;
			this.type = type;
			this.cpu = cpu;
			this.memory = memory;
		}
		
		public String getName() {
			return this.name;
		}
		
		public int getCPUConsumption() {
			if (type.equals("WORKER")) {
				float op1 = new Random().nextFloat()*Float.valueOf(this.cpu); //TODO	
				float op2 = Float.valueOf(this.cpu);
				
				//Protection
				if (op2 == Float.valueOf(0)) {
					return 0;
				}
				
				//Normal result
				float result = (op1/op2)*Float.valueOf(100);
				return (int)result;	
			}
			//Type SERVICE or UNKNOWN
			return 0;
		}
		
		public float getMemoryConsumption() {
			if (type.equals("WORKER")) {
				float op1 = new Random().nextFloat()*this.memory; //TODO
				
				//Protection
				if (this.memory == Float.valueOf(0)) {
					return Float.valueOf(0);
				}
				
				//Normal result
				float result = (op1/this.memory)*Float.valueOf(100);
				return result;
			}
			//Type SERVICE or UNKNOWN
			return Float.valueOf(0);
		}
	}

}
