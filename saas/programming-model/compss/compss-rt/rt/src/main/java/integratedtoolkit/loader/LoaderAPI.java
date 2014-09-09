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




package integratedtoolkit.loader;

import integratedtoolkit.api.IntegratedToolkit.OpenMode;
import integratedtoolkit.loader.total.ObjectRegistry;


public interface LoaderAPI {

	// Returns the renaming of the last file version just transferred
	String getFile(String fileName, String destDir);
	
	// Returns a copy of the last object version
	Object getObject(Object o, int hashCode, String destDir);
	
	// Returns the renaming of the file version opened
	String openFile(String fileName, OpenMode m);
	
	void serializeObject(Object o, int hashCode, String destDir);
	
	void setObjectRegistry(ObjectRegistry oReg);
	
	// Returns the directory where to store temp files
	String getTempDir();
	
}
