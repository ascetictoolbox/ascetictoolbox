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

package integratedtoolkit.types.data;

import integratedtoolkit.comm.Comm;
import static integratedtoolkit.comm.Comm.appHost;
import integratedtoolkit.types.data.location.DataLocation.Type;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.types.data.location.URI;
import integratedtoolkit.types.data.operation.Copy;
import integratedtoolkit.types.data.operation.SafeCopyListener;
import integratedtoolkit.types.resources.Resource;
import integratedtoolkit.util.Serializer;
import integratedtoolkit.util.SharedDiskManager;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

public class LogicalData {

    protected String name;

    // Value in memory, null if value in disk
    protected Object value;
    protected boolean onFile;

    // List of existing copies
    protected final TreeSet<DataLocation> locations = new TreeSet<DataLocation>();

    // List of copies in progress
    private static final ConcurrentHashMap<String, LinkedList<CopyInProgress>> inProgress = new ConcurrentHashMap<String, LinkedList<CopyInProgress>>();

    public static final TreeMap<Resource, HashSet<LogicalData>> hostToPrivateFiles = new TreeMap<Resource, HashSet<LogicalData>>();
    public static final TreeMap<String, HashSet<LogicalData>> sharedDiskToSharedFiles = new TreeMap<String, HashSet<LogicalData>>();

    private boolean isBeingSaved = false;

    public LogicalData() {
    }

    // Create an empty LogicalData
    public LogicalData(String name) {
        this.name = name;
        this.value = null;
        onFile = false;
    }

    // Get the name of the file
    public String getName() {
        return name;
    }

    public HashSet<Resource> getAllHosts() {
        HashSet<Resource> list = new HashSet<Resource>();
        synchronized (locations) {
            for (DataLocation loc : locations) {
                list.addAll(loc.getHosts());
            }
        }
        return list;
    }

    // Obtain all the URIs that refer all the files
    public LinkedList<URI> getURIs() {
        LinkedList<URI> list = new LinkedList<URI>();
        synchronized (locations) {
            for (DataLocation loc : locations) {
                list.addAll(loc.getURIs());
            }
        }
        return list;
    }

    // Obtain one URI per file copy
    // (files in shared disks will only
    // return one URI)
    public LinkedList<URI> getRepresentativeURIs() {
        LinkedList<URI> list = new LinkedList<URI>();
        synchronized (locations) {
            for (DataLocation loc : locations) {
                list.addAll(loc.getURIs());
            }
        }
        return list;
    }

    public void addLocation(DataLocation loc) {
        synchronized (locations) {
            isBeingSaved = false;
            locations.add(loc);
            switch (loc.getType()) {
                case PRIVATE:
                    for (Resource host : loc.getHosts()) {
                        if (host == null) {
                            host = Comm.appHost;
                        }
                        HashSet<LogicalData> files = hostToPrivateFiles.get(host);
                        if (files == null) {
                            files = new HashSet<LogicalData>();
                            hostToPrivateFiles.put(host, files);
                        }
                        files.add(this);
                    }
                    if (loc.getPath().startsWith(File.separator)) {
                        onFile = true;
                    }
                    break;
                case SHARED:
                    String shared = loc.getSharedDisk();
                    HashSet<LogicalData> files = sharedDiskToSharedFiles.get(shared);
                    if (files == null) {
                        files = new HashSet<LogicalData>();
                        sharedDiskToSharedFiles.put(shared, files);
                    }
                    files.add(this);
                    onFile = true;
                    break;
            }
        }
    }

    public Object removeValue() {
        DataLocation location = DataLocation.getLocation(appHost, name);
        Object val = value;
        synchronized (locations) {
            value = null;
            locations.remove(location);
        }
        return val;
    }

    public DataLocation removeHostAndCheckLocationToSave(Resource host, HashMap<String, String> sharedMountPoints) {
        DataLocation hostLocation = null;
        boolean hasToSave = true;
        if (isBeingSaved) {
            return null;
        }
        synchronized (locations) {
            Iterator<DataLocation> it = locations.iterator();
            while (it.hasNext()) {
                DataLocation loc = it.next();
                if (loc.getType() == Type.PRIVATE) {
                    if (loc.getURIInHost(host) != null) {
                        hostLocation = loc;
                        it.remove();
                    } else {
                        return null;
                    }
                } else { //is a SharedLocation
                    if (loc.getHosts().isEmpty()) {
                        String sharedDisk;
                        String mountPoint;
                        if ((sharedDisk = loc.getSharedDisk()) != null) {
                            if ((mountPoint = sharedMountPoints.get(sharedDisk)) != null) {
                                hostLocation = DataLocation.getPrivateLocation(host, mountPoint + loc.getPath());
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
            }
        }
        if (hasToSave) {
            this.isBeingSaved = true;
            return hostLocation;
        } else {
            return null;
        }
    }

    public boolean isInMemory() {
        return this.value != null;
    }

    public boolean isOnFile() {
        return this.onFile;
    }

    public void setValue(Object o) {
        this.value = o;
    }

    public Object getValue() {
        return this.value;
    }

    public void writeToFile() throws Exception {
        synchronized (locations) {
            String path = Comm.appHost.getWorkingDirectory() + name;
            DataLocation loc = DataLocation.getLocation(Comm.appHost, path);
            Serializer.serialize(value, path);
            addLocation(loc);
        }
    }

    public Collection<Copy> getCopiesInProgress() {
        LinkedList<CopyInProgress> stored;
        synchronized (inProgress) {
            stored = inProgress.get(this.name);
        }
        if (stored == null) {
            return null;
        }
        LinkedList<Copy> copies = new LinkedList<Copy>();
        synchronized (stored) {
            for (CopyInProgress cp : stored) {
                copies.add(cp.getCopy());
            }
        }
        return copies;
    }

    public URI alreadyAvailable(Resource targetHost) {
        synchronized (locations) {
            for (DataLocation loc : locations) {
                URI u = loc.getURIInHost(targetHost);
                if (u != null) {
                    return u;
                }
            }
        }
        return null;
    }

    public Copy alreadyCopying(DataLocation target) {
        LinkedList<CopyInProgress> copying = inProgress.get(this.name);
        if (copying != null) {
            synchronized (copying) {
                for (CopyInProgress cip : copying) {
                    if (cip.hasTarget(target)) {
                        return cip.getCopy();
                    }
                }
            }
        }
        return null;
    }

    public void startCopy(Copy c, DataLocation target) {
        synchronized (inProgress) {
            LinkedList<CopyInProgress> cips = inProgress.get(this.name);
            if (cips == null) {
                cips = new LinkedList<CopyInProgress>();
                inProgress.put(this.name, cips);
            }
            cips.add(new CopyInProgress(c, target));
        }
    }

    public DataLocation finishedCopy(Copy c) {
        DataLocation loc = null;
        synchronized (inProgress) {
            LinkedList<CopyInProgress> cips = inProgress.get(this.name);
            Iterator<CopyInProgress> it = cips.iterator();
            while (it.hasNext()) {
                CopyInProgress cip = it.next();
                if (cip.c == c) {
                    it.remove();
                    loc = cip.loc;
                    break;
                }
            }

            if (cips.isEmpty()) {
                inProgress.remove(this.name);
            }
        }
        return loc;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Logical Data name: ").append(this.name).append(":\n");
        sb.append("\t Value: ").append(value).append("\n");
        sb.append("\t Locations:\n");
        synchronized (locations) {
            for (DataLocation dl : locations) {
                sb.append("\t\t * ").append(dl).append("\n");
            }
        }
        return sb.toString();
    }

    public static HashSet<LogicalData> getAllDataFromHost(Resource host) {

        LinkedList<String> shareds = SharedDiskManager.getAllSharedNames(host);
        if (shareds.isEmpty()) {
            if (hostToPrivateFiles.get(host) != null) {
                return hostToPrivateFiles.get(host);
            } else {
                return new HashSet<LogicalData>();
            }
        }
        HashSet<LogicalData> data = new HashSet<LogicalData>();
        for (String shared : shareds) {
            HashSet<LogicalData> sharedData = sharedDiskToSharedFiles.get(shared);
            if (sharedData != null) {
                data.addAll(sharedData);
            }
        }
        if (hostToPrivateFiles.get(host) != null) {
            data.addAll(hostToPrivateFiles.get(host));
        }
        return data;
    }

    public void notifyToInProgressCopiesEnd(SafeCopyListener listener) {
        synchronized (inProgress) {
            LinkedList<CopyInProgress> copies = inProgress.get(this.name);
            if (copies != null) {
                for (CopyInProgress copy : copies) {
                    listener.addOperation();
                    copy.c.addEventListener(listener);
                }
            }
        }
    }

    public void isObsolete() {
        for (Resource res : getAllHosts()) {
            res.addObsolete(name);

        }

    }

    public void loadFromFile() throws Exception {
        synchronized (locations) {
            if (value != null) {
                return;
            }
            for (DataLocation loc : locations) {
                URI u = loc.getURIInHost(Comm.appHost);
                if (u == null) {
                    continue;
                }
                String path = u.getPath();
                if (path.startsWith("/")) {
                    value = Serializer.deserialize(path);
                    DataLocation tgtLoc = DataLocation.getLocation(Comm.appHost, name);
                    addLocation(tgtLoc);
                }
                return;
            }
        }
        if (value == null) {
            throw new Exception("File does not exists in the master");
        }
    }

    private static class CopyInProgress {

        private final Copy c;
        private final DataLocation loc;

        CopyInProgress(Copy c, DataLocation loc) {
            this.c = c;
            this.loc = loc;
        }

        public Copy getCopy() {
            return this.c;
        }

        public DataLocation getTarget() {
            return loc;
        }

        private boolean hasTarget(DataLocation target) {
            return loc.isTarget(target);
        }

        public String toString() {
            return c.getName() + " to " + loc.toString();
        }
    }
}
