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


package integratedtoolkit.components;

import integratedtoolkit.types.Parameter.DependencyParameter;
import integratedtoolkit.types.data.DataAccessId;
import integratedtoolkit.types.data.Location;
import integratedtoolkit.types.data.ResultFile;
import integratedtoolkit.types.ResourceDestructionRequest;

import java.util.List;
import java.util.concurrent.Semaphore;


/* To request the transfer of a local or remote file to a destination host.
 * The file can be a regular file or it can contain the serialization of an object.
 */
public interface FileTransfer {
	
	public static final int FILES_READY = 0;
	
	// Different roles for a file involved in an operation
	public enum FileRole {
		NO_ROLE,        // Unknown kind of file
		JOB_FILE,	// Input file of a job
		OPEN_FILE,	// File to be opened by the application
		RESULT_FILE,    // Result file of the application
		DELETE_FILE,    // Intermediate renamed file to be deleted
		RAW_FILE,	// File which must not be tracked by the FM
		SAFE_FILE,      // File to be saved before stopping a VM
		TRACE_FILE,		// File containing trace files
		SHUTDOWN;       // Transfer needed to terminate a machine
	}
	
	/* Returns the identifier of the group of transfers requested,
	 * or FILES_READY if no transfer has been necessary
	 */
	int transferFiles(Integer groupId, List<DependencyParameter> dataAccesses, Location targetLocation);
	
	// Transfers a list of result files back to the application host
	void transferBackResultFiles(List<ResultFile> resFiles, Semaphore sem);

	void transferFileForOpen(DataAccessId faId, Location targetLocation, Semaphore sem);

	void transferFileRaw(DataAccessId faId, Location targetLocation, Semaphore sem);

	void deleteIntermediateFiles(Semaphore sem);

        // Transfers all unique files from host to other machines
        void transferStopFiles(ResourceDestructionRequest rdr, String[] resourcesPerMethod);
}
