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

import java.util.List;
import java.util.concurrent.Semaphore;

import integratedtoolkit.types.data.ResultFile;

public class BlockAndGetResultFilesRequest extends TPRequest {

	private Long appId;
	private Semaphore sem;
	
	private List<ResultFile> response;

	public BlockAndGetResultFilesRequest(Long appId, Semaphore sem) {
		super(TPRequestType.BLOCK_AND_GET_RESULT_FILES);
		this.appId = appId;
		this.sem = sem;
	}
	
	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

	public Semaphore getSemaphore() {
		return sem;
	}

	public void setSemaphore(Semaphore sem) {
		this.sem = sem;
	}
	
	public List<ResultFile> getResponse() {
		return response;
	}

	public void setResponse(List<ResultFile> response) {
		this.response = response;
	}
	
}
