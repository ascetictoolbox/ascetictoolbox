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
package integratedtoolkit.types.request.tp.debug;

import integratedtoolkit.types.data.DataInstanceId;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class GetInstancesRequest extends TPDebugRequest {

    private String host;
    private String path;
    private String fileName;
    private Semaphore sem;
    private LinkedList<DataInstanceId> response;

    public GetInstancesRequest(String host, String path, String fileName, Semaphore sem) {
        super(DebugRequestType.GET_INSTANCES);
        this.host = host;
        this.path = path;
        this.fileName = fileName;
        this.sem = sem;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setSem(Semaphore sem) {
        this.sem = sem;
    }

    public Semaphore getSem() {
        return sem;
    }

    public void setResponse(LinkedList<DataInstanceId> response) {
        this.response = response;
    }

    public LinkedList<DataInstanceId> isResponse() {
        return response;
    }

}
