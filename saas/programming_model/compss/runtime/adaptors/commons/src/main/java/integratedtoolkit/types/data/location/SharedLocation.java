/**
 *
 *   Copyright 2013-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package integratedtoolkit.types.data.location;

import integratedtoolkit.types.resources.Resource;
import integratedtoolkit.util.SharedDiskManager;
import java.util.LinkedList;

public class SharedLocation extends DataLocation {

    private final String diskName;
    private final String path;

    SharedLocation(String sharedDisk, String path) {
        this.diskName = sharedDisk;
        this.path = path;
    }

    @Override
    public URI getURIInHost(Resource host) {
        String diskPath = SharedDiskManager.getMounpoint(host, diskName);
        if (diskPath == null) {
            return null;
        }
        return new URI(host, diskPath + path);
    }

    @Override
    public DataLocation.Type getType() {
        return DataLocation.Type.SHARED;
    }

    @Override
    public LinkedList<URI> getURIs() {
        LinkedList<URI> uris = new LinkedList<URI>();
        for (Resource host : SharedDiskManager.getAllMachinesfromDisk(diskName)) {
            String diskPath = SharedDiskManager.getMounpoint(host, diskName);
            uris.add(new URI(host, diskPath + path));
        }
        return uris;
    }

    @Override
    public LinkedList<Resource> getHosts() {
        return SharedDiskManager.getAllMachinesfromDisk(diskName);
    }

    @Override
    public boolean isTarget(DataLocation target) {
        String targetDisk;
        String targetPath;
        if (target.getType() == DataLocation.Type.PRIVATE) {
            PrivateLocation privateLoc = (PrivateLocation) target;
            targetDisk = null;//TODO: extract from URI
            targetPath = privateLoc.uri.getPath();
        } else {
            SharedLocation sharedloc = (SharedLocation) target;
            targetDisk = sharedloc.diskName;
            targetPath = sharedloc.path;
        }
        return (targetDisk != null && targetDisk.contentEquals(diskName) && targetPath.contentEquals(targetPath));
    }

    public String toString() {
        return "shared:" + diskName + "/" + path;
    }

    @Override
    public String getSharedDisk() {
        return diskName;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getLocationKey() {
        return path + ":shared:" + diskName;
    }

    @Override
    public int compareTo(DataLocation o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (o.getClass() != SharedLocation.class) {
            return (this.getClass().getName()).compareTo("integratedtoolkit.types.data.location.SharedLocation");
        } else {
            SharedLocation sl = (SharedLocation) o;
            int compare = diskName.compareTo(sl.diskName);
            if (compare == 0) {
                compare = path.compareTo(sl.path);
            }
            return compare;
        }
    }
}
