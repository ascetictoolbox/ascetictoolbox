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


public class ResultFile implements Comparable<ResultFile> {
	
	DataInstanceId fId;
	String originalName;
	Location originalLocation;
	
	
	public ResultFile (DataInstanceId fId, String name, Location loc) {
		this.fId = fId;
		this.originalName = name;
		this.originalLocation = loc;
	}

	public DataInstanceId getFileInstanceId() {
		return fId;
	}
	
	public String getOriginalName() {
		return originalName;
	}
	
	public Location getOriginalLocation() {
		return originalLocation;
	}
	
	
	// Comparable interface implementation
	public int compareTo(ResultFile resFile) throws NullPointerException {
		if (resFile == null)
			throw new NullPointerException();
		
		// Compare file identifiers
	    return this.getFileInstanceId().compareTo(resFile.getFileInstanceId());
	}
        public String toString(){
            return fId.getRenaming();
        }
	
}
