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
package integratedtoolkit.types.data.location;

import integratedtoolkit.types.resources.Resource;
import integratedtoolkit.util.SharedDiskManager;
import java.util.LinkedList;

public abstract class DataLocation implements Comparable<DataLocation>{

    public enum Type {

        PRIVATE,
        SHARED
    }

    public static DataLocation getSharedLocation(String sharedDisk, String path) {
        return new SharedLocation(sharedDisk, path);
    }

    public static DataLocation getPrivateLocation(Resource host, String path) {
        return new PrivateLocation(host, path);
    }

    public static DataLocation getLocation(Resource host, String path) {
        String diskName = SharedDiskManager.getSharedName(host, path);
        if (diskName != null) {
            String mountpoint = SharedDiskManager.getMounpoint(host, diskName);
            return new SharedLocation(diskName, path.substring(mountpoint.length()));
        } else {
            return new PrivateLocation(host, path);
        }
    }

    public abstract Type getType();

    public abstract LinkedList<URI> getURIs();

    public abstract String getSharedDisk();

    public abstract LinkedList<Resource> getHosts();

    public abstract String getPath();

    public abstract URI getURIInHost(Resource targetHost);

    public abstract boolean isTarget(DataLocation target);

    public abstract String getLocationKey();

}
