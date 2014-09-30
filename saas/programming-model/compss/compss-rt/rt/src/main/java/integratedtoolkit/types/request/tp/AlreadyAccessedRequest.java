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


public class AlreadyAccessedRequest extends TPRequest {

	private String fileName;
	private String path;
	private String host;
	
	private Semaphore sem;
	
	private boolean response;

	public AlreadyAccessedRequest(String fileName, String path, String host,
			Semaphore sem) {
		super(TPRequestType.ALREADY_ACCESSED);
		this.fileName = fileName;
		this.path = path;
		this.host = host;
		this.sem = sem;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
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
