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



public class NewVersionSameValueRequest extends TPRequest {

	private String rRenaming;
	private String wRenaming;
	
	public NewVersionSameValueRequest(String rRenaming, String wRenaming) {
		super(TPRequestType.NEW_VERSION_SAME_VALUE);
		this.rRenaming = rRenaming;
		this.wRenaming = wRenaming;
	}

	public String getrRenaming() {
		return rRenaming;
	}

	public void setrRenaming(String rRenaming) {
		this.rRenaming = rRenaming;
	}

	public String getwRenaming() {
		return wRenaming;
	}

	public void setwRenaming(String wRenaming) {
		this.wRenaming = wRenaming;
	}
	
}
