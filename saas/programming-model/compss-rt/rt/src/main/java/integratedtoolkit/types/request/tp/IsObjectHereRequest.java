/*
 *  Copyright 2002-2012 Barcelona Supercomputing Center (www.bsc.es)
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


package integratedtoolkit.types.request.tp;

import integratedtoolkit.types.data.DataInstanceId;

import java.util.concurrent.Semaphore;


public class IsObjectHereRequest extends TPRequest {

	private DataInstanceId dId;
	private Semaphore sem;
	
	private boolean response;

	public IsObjectHereRequest(DataInstanceId dId, Semaphore sem) {
		super(TPRequestType.IS_OBJECT_HERE);
		this.dId = dId;
		this.sem = sem;
	}

	public DataInstanceId getdId() {
		return dId;
	}

	public void setdId(DataInstanceId dId) {
		this.dId = dId;
	}

	public Semaphore getSemaphore() {
		return sem;
	}

	public void setSemaphore(Semaphore sem) {
		this.sem = sem;
	}

	public boolean getResponse() {
		return response;
	}

	public void setResponse(boolean response) {
		this.response = response;
	}
	
}
