/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package integratedtoolkit.types.data;

import java.io.Serializable;


// A File Instance is identified by its file and version identifiers 
public class DataInstanceId implements Serializable, Comparable<DataInstanceId> {

	// Time stamp
    private static String timeStamp = Long.toString(System.currentTimeMillis());
	
	// File instance identifier fields
	private int dataId;
	private int versionId;
	
	// Renaming for this file version
	private String renaming;

	
	public DataInstanceId() { }
	
	public DataInstanceId(int dataId, int versionId) {
		this.dataId = dataId;
		this.versionId = versionId;
		this.renaming = "d" + dataId + "v" + versionId + "_" + timeStamp + ".IT";
	}
	
	public int getDataId() {
		return dataId;
	}
	
	public int getVersionId() {
		return versionId;
	}
	
	public String getRenaming() {
		return renaming;
	}
	
	// Override the toString method
	public String toString() {
		return "d" + dataId + "v" + versionId;
	}
	
	// Comparable interface implementation
	public int compareTo(DataInstanceId dId) throws NullPointerException {
		if (dId == null)
			throw new NullPointerException();
		
	    DataInstanceId dataId = (DataInstanceId)dId;
	    
	    // First compare file identifiers
	    if (dataId.getDataId() != this.getDataId())
	    	return dataId.getDataId() - this.getDataId();
	    // If same file identifier, compare version identifiers
	    else
	    	return dataId.getVersionId() - this.getVersionId();	
	}
	
	public static String previousVersionRenaming(String renaming) {
		int d = renaming.indexOf('d');
		int v = renaming.indexOf('v');
		int _ = renaming.indexOf('_');
		if (v == 1) return null;
		int dataId = Integer.parseInt(renaming.substring(d + 1, v));
		int previousVersion = Integer.parseInt(renaming.substring(v + 1, _)) - 1;
		return "d" + dataId + "v" + previousVersion + "_" + timeStamp + ".IT";
	}
	
}
