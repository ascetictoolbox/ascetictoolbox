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

package integratedtoolkit.util;

import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.data.Location;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * The Shared Disk Manager is an utility to manage the disk shared by many 
 * resources. It keeps information about which disk are mounted in a machine, 
 * the path where they are mounted and which files are present on the disk.
 */
public class SharedDiskManager {

    /** Relation fileName --> shared disk where it is placed*/
    private static HashMap<String, SharedFile> fileName2SharedFile = new HashMap();
    /** Relation shared disk name --> worker names where it is mounted*/
    private static HashMap<String, LinkedList<String>> shared2Machines = new HashMap();
    /** Relation resource name --> Shared disks contained */
    private static HashMap<String, Machine> machine2Shareds = new HashMap();
    
    /**
     * Adds a new resource to be managed
     * @param name Resource name
     */
    public static void addMachine(String name) {
        Machine m = new Machine(name);
        machine2Shareds.put(name, m);
    }

    /**
     * Links a shared disk with a resource
     * @param diskName shared disk identifier
     * @param mountpoint path where the shared disk is mounted
     * @param machineName name of the containing resource  
     */
    public static void addSharedToMachine(String diskName, String mountpoint, String machineName) {
        LinkedList<String> machines = shared2Machines.get(diskName);
        if (machines == null) {
            machines = new LinkedList<String>();
            shared2Machines.put(diskName, machines);
        }
        machines.add(machineName);
        machine2Shareds.get(machineName).addSharedDisk(diskName, mountpoint);
    }

    /**
     * Gets the name of a shared disk which contains the files in a resource 
     * path
     * @param machineName Name of the resource
     * @param path File path contained by the disk
     * @return null if there is no shared disk containing that file path on the 
     * resource. The shared disk identifier containing that file path.
     */
    public static String getSharedName(String machineName, String path) {
        Machine m = machine2Shareds.get(machineName);
        if (m == null) {
            return null;
        }
        return m.getSharedName(path);
    }

    /**
     * Returns a string describing the current state of the shared disk 
     * configuration and the files contained on them
     * @return description of the current state of the shared disk 
     * configuration and the files contained on them
     */
    public static String getSharedStatus() {
        StringBuilder sb = new StringBuilder("Shared disk in machines:\n");
        for (Entry<String, LinkedList<String>> e : shared2Machines.entrySet()) {
            sb.append(e.getKey()).append("--> {");
            for (int i = 0; i < e.getValue().size(); i++) {
                sb.append(e.getValue().get(i)).append(", ");
            }
            sb.append("}\n");
        }

        sb.append("Machines :\n");
        for (Entry<String, Machine> e : machine2Shareds.entrySet()) {
            sb.append(e.getKey()).append("--> {");
            for (Entry<String, String> me : e.getValue().name2Mountpoint.entrySet()) {
                sb.append(me.getKey()).append("@").append(me.getValue()).append(", ");
            }
            sb.append("}\n");
        }

        sb.append("Files under shared versions:\n");
        for (Entry<String, SharedFile> e : fileName2SharedFile.entrySet()) {
            sb.append(e.getKey()).append("--> {");
            sb.append(e.getValue().showLocations());
            sb.append("}\n");
        }


        return sb.toString();
    }

    /**
     * Registers the presence of a file inside a shared disks
     * @param logicalId renaming of the file
     * @param location machine and path where that file can be found
     * @param name real file name on the location
     */
    public static void registerFile(String logicalId, Location location, String name) {
    	SharedFile shareds = fileName2SharedFile.get(logicalId);
        if (shareds == null) {
            shareds = new SharedFile();
            fileName2SharedFile.put(logicalId, shareds);
        }
        if (location.getHost().startsWith("shared:")) {
            shareds.addLocation(location.getHost().substring(7), (location.getPath() + "/" + name).substring(1));
        } else {
            Machine m = machine2Shareds.get(location.getHost());
            String sharedDisk = m.getSharedName(location.getPath());
            if (sharedDisk != null) {
                shareds.addLocation(sharedDisk, (location.getPath() + "/" + name).substring(m.name2Mountpoint.get(sharedDisk).length()));
            }
        }
    }

    /**
     * Unregisters the presence of many files in a shared disk
     * @param obsoletes list of file names to be unregistered
     */
    public static void unregisterFiles(LinkedList<String> obsoletes) {
        for (String fileName : obsoletes) {
            fileName2SharedFile.remove(fileName);
        }
    }

    /**
     * Unregisters the presence of a file in a shared disk
     * @param fileName name of the file to be unregistered
     */
    public static void unregisterFile(String fileName) {
        fileName2SharedFile.remove(fileName);
    }

    /**
     * Checks if a file already is inside a shared disk of the destination 
     * resource
     * @param destHost destination resource id
     * @param fileName name of the file that is suposed to be in the host
     * @return null if the file is not in the machine. The path where the file 
     * can be found
     */
    public static String isShared(String destHost, String fileName) {
    	SharedFile sharedFile = fileName2SharedFile.get(fileName);
        if (sharedFile == null) {
            return null;
        }
        Machine m = machine2Shareds.get(destHost);
        if (m == null) {
            return null;
        }
        
        return sharedFile.getPath(m);
    }

    /**
     * Returns a list with all the name of all the shared disks mounted on a 
     * resource
     * @param host resource name
     * @return a list with all the name of all the shared disks mounted on a 
     * resource
     */
    public static LinkedList<String> getAllSharedNames(String host) {
        Machine m = machine2Shareds.get(host);
        if (m == null) {
            return new LinkedList<String>();
        }
        return m.getAllSharedNames();
    }

    /**
     * Returns the mountpoint of a shared disk in a resource
     * @param host resource name
     * @param sharedDisk shared disk name
     * @return  mountpoint of the shared disk in the resource
     */
    public static String getMounpoint(String host, String sharedDisk) {
        Machine m = machine2Shareds.get(host);
        if (m == null) {
            return null;
        }
        return m.getPath(sharedDisk);
    }

    /**
     * Returns a list of machines with a shared disk mounted 
     * @param diskName name of the shared disk we are looking for
     * @return list of machines with a shared disk mounted 
     */
    public static LinkedList<String> getAllMachinesfromDisk(String diskName) {
        return shared2Machines.get(diskName);
    }

    /**
     * Returns a list of machines with a shared disk mounted which contain a
     * certain file
     * @param fileName name of the file we are looking for
     * @return list of machines with a shared disk mounted which contain a 
     * certain file
     */
    public static TreeSet<String> getAllMachinesfromFile(String fileName) {
        TreeSet<String> machines = new TreeSet<String>();
        SharedFile sharedfile = fileName2SharedFile.get(fileName);
        LinkedList<String> sharedDisks = sharedfile.sharedNames;
        for (String sharedDisk : sharedDisks) {
            machines.addAll(shared2Machines.get(sharedDisk));
        }
        return machines;
    }

    /**
     * Removes all the information of a resource
     * @param resourceName Machine name
     */
    public static void terminate(String resourceName) {
        Machine m = machine2Shareds.remove(resourceName);
        for (String sharedName : m.allShared) {
            shared2Machines.get(sharedName).remove(resourceName);
        }
    }
}

class Machine {

    String name;
    LinkedList<String> allShared;
    HashMap<String, String> mountpoint2Name;
    HashMap<String, String> name2Mountpoint;
    

    public Machine(String name) {
        this.name = name;
        allShared = new LinkedList();
        mountpoint2Name = new HashMap();
        name2Mountpoint = new HashMap();
    }

    public void addSharedDisk(String diskName, String mountpoint) {
        allShared.add(diskName);
        if (!mountpoint.endsWith("/")) {
            mountpoint += "/";
        }
        mountpoint2Name.put(mountpoint, diskName);
        name2Mountpoint.put(diskName, mountpoint);
    }

    public String getSharedName(String path) {
        for (Entry<String, String> e : mountpoint2Name.entrySet()) {
            if (path.startsWith(e.getKey())) {
                return e.getValue();
            }
        }
        return null;
    }

    public String getPath(String sharedDisk) {
        return name2Mountpoint.get(sharedDisk);
    }

    public String getPath(LinkedList<String> sharedDisks) {
        for (int i = 0; i < sharedDisks.size(); i++) {
            String path = name2Mountpoint.get(sharedDisks.get(i));
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    public LinkedList<String> getAllSharedNames() {
        return allShared;
    }
}

class SharedFile {

    HashMap<String, String> shared2Location;
    LinkedList<String> sharedNames;

    public SharedFile() {
        sharedNames = new LinkedList();
        shared2Location = new HashMap();
    }

    public void addLocation(String diskName, String path) {
    	sharedNames.add(diskName);
        shared2Location.put(diskName, path);
    }

    public void removeLocation(String diskName) {
        sharedNames.remove(diskName);
        shared2Location.remove(diskName);
    }

    public String getPath(Machine m) {
        LinkedList<String> sharedDisks = m.allShared;
        for (String sharedDisk : sharedDisks) {
        	String path = shared2Location.get(sharedDisk);
            if (path != null) {
                return m.name2Mountpoint.get(sharedDisk) + path;
            }
        }
        return null;
    }

    public String showLocations() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> e : shared2Location.entrySet()) {
            sb.append(e.getKey()).append("-->").append(e.getValue());
        }
        return sb.toString();
    }
}
