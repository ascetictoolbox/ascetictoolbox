package com.bsc.compss.ui;

import java.util.Vector;


public class LoadData {
	private int timestamp;
	//Global values
	private float totalLoad;
	private int totalCoresRunning;
	private int totalCoresPending;
	
	//Values per core/resource
	private Vector<Float> loadInfo;
	private Vector<String> resources;
	private Vector<Integer> runningCores;
	private Vector<Integer> pendingCores;
	
	public LoadData() {
		this.timestamp = 0;
		this.totalLoad = Float.valueOf(0);
		this.totalCoresRunning = 0;
		this.totalCoresPending = 0;
		
		this.loadInfo = new Vector<Float>();
		this.resources = new Vector<String>();
		this.runningCores = new Vector<Integer> ();
		this.pendingCores = new Vector<Integer> ();
	}
	
	public LoadData(int timestamp) {
		this.timestamp = timestamp;
		this.totalLoad = Float.valueOf(0);
		this.totalCoresRunning = 0;
		this.totalCoresPending = 0;
		
		this.loadInfo = new Vector<Float>();
		this.resources = new Vector<String>();
		this.runningCores = new Vector<Integer> ();
		this.pendingCores = new Vector<Integer> ();
	}
	
	public int getTimestamp() {
		return this.timestamp;
	}
	
	public float getTotalLoad() {
		return this.totalLoad;
	}
	
	public Vector<Float> getLoadInfo() {
		return this.loadInfo;
	}
	
	public int getTotalResources() {
		return this.resources.size();
	}
	
	public int getTotalCoresRunning() {
		return this.totalCoresRunning;
	}
	
	public Vector<Integer> getRunningCoresInfo() {
		return this.runningCores;
	}
	
	public int getTotalCoresPending() {
		return this.totalCoresPending;
	}
	
	public Vector<Integer> getPendingCoresInfo() {
		return this.pendingCores;
	}
	
	public void addCoreLoad(int id, float load) {
		//Resize if needed
		if (id >= this.loadInfo.size()) {
				this.loadInfo.setSize(id + 1); 
		}
		//Update structures
		totalLoad = totalLoad + load;
		this.loadInfo.set(id, load);
	}
	
	public void addResource(String resourceName) {
		this.resources.add(resourceName);
	}
	
	public void addCoreRunning(int coreId, int running) { 
		//TODO running flag is not working on resources.log from runtime
		//Update global variable
		this.totalCoresRunning = this.totalCoresRunning + 1; //+running
		//Resize if needed
		if (coreId >= this.runningCores.size()) {
				this.runningCores.setSize(coreId + 1); 
		}
		//Update
		if (this.runningCores.get(coreId) != null) {
			this.runningCores.set(coreId, this.runningCores.get(coreId) + 1); //+running
		} else {
			this.runningCores.set(coreId, 1); //+running
		}
	}
	
	public void addCorePending(int coreId, int newPending) {
		//Update global variable
		this.totalCoresPending = this.totalCoresPending + newPending;
		//Resize if needed
		if (coreId >= this.pendingCores.size()) {
				this.pendingCores.setSize(coreId + 1); 
		}
		//Update
		if (this.pendingCores.get(coreId) != null) {
			this.pendingCores.set(coreId, this.pendingCores.get(coreId) + newPending);
		} else {
			this.pendingCores.set(coreId, newPending);
		}
	}

}
