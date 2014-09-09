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

package integratedtoolkit.components;

import integratedtoolkit.types.data.DataAccessId;
import integratedtoolkit.types.data.DataInstanceId;

import java.util.List;

// To request or update information about a file
public interface DataInformation {

    List<String> dataHasBeenRead(List<DataAccessId> dataIds, int readerId);

    void setObjectVersionValue(String renaming, Object value);

    // Returns the renaming of the last version of the object
    String getLastRenaming(int hashCode);

    // Returns an object with this renaming, result of invoking a WS task
    Object getObject(String renaming);

    boolean isHere(DataInstanceId dId);
    //List<Object> stopAndGetStopTransferFiles(String hostName);
}
