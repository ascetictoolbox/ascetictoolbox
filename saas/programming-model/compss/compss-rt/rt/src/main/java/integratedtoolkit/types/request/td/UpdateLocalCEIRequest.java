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

package integratedtoolkit.types.request.td;

import java.util.concurrent.Semaphore;

import integratedtoolkit.types.request.td.TDRequest;

public class UpdateLocalCEIRequest extends TDRequest {
	
	private Class<?> ceiClass;
	private Semaphore sem;
	public UpdateLocalCEIRequest() {
		super(TDRequestType.UPDATE_LOCAL_CEI);
		
	}
	
	public UpdateLocalCEIRequest(Class<?> ceiClass, Semaphore sem) {
		super(TDRequestType.UPDATE_LOCAL_CEI);
		this.ceiClass = ceiClass;
		this.sem = sem;
	}
	
	/**
     * Returns the semaphore where to synchronize until the operation is done
     * @return Semaphore where to synchronize until the operation is done
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /**
     * Sets the semaphore where to synchronize until the operation is done
     * @param sem Semaphore where to synchronize until the operation is done
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }
    
    public void setCeiClass(Class<?> ceiClass){
    	this.ceiClass = ceiClass;
    }
    
    public Class<?> getCeiClass(){
    	return this.ceiClass;
    }

}
