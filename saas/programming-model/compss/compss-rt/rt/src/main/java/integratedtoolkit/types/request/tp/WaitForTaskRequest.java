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


package integratedtoolkit.types.request.tp;

import java.util.concurrent.Semaphore;
import integratedtoolkit.components.DataAccess.AccessMode;

public class WaitForTaskRequest extends TPRequest {

	private int dataId;
        private AccessMode am;
	private Semaphore sem;
        

	public WaitForTaskRequest(int dataId, AccessMode mode, Semaphore sem) {
		super(TPRequestType.WAIT_FOR_TASK);
		this.dataId = dataId;
                this.am=mode;
		this.sem = sem;
	}
	
	public Semaphore getSemaphore() {
		return sem;
	}

	public void setSemaphore(Semaphore sem) {
		this.sem = sem;
	}

	public int getDataId() {
		return dataId;
	}
	public AccessMode getAccessMode() {
		return am;
	}
	public void setDataId(int dataId) {
		this.dataId = dataId;
	}
	
}
