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
package integratedtoolkit.components.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import integratedtoolkit.components.DataAccess;
import integratedtoolkit.components.DataInformation;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.data.*;
import integratedtoolkit.types.data.AccessParams.*;
import integratedtoolkit.types.data.DataAccessId.*;
import integratedtoolkit.util.Serializer;
import java.io.File;
import java.util.LinkedList;

public class DataInfoProvider implements DataAccess, DataInformation {

    // Constants definition
    private static final String SERIALIZATION_ERR = "Error serializing object to a file";
    // Client interfaces
    private TaskDispatcher TD;
    // Map: filename:host:path -> file identifier
    private TreeMap<String, Integer> nameToId;
    // Map: hash code -> object identifier
    private TreeMap<Integer, Integer> codeToId;
    // Map: file identifier -> file information
    private TreeMap<Integer, DataInfo> idToData;
    // Map: Object_Version_Renaming -> Object value
    private TreeMap<String, Object> renamingToValue; // TODO: Remove obsolete from here
    // Map: Object_Version_Renaming -> Is_Serialized?
    private HashMap<String, Boolean> renamingToIsSerialized; // TODO: Remove obsolete from here
    // Map: Blocked data ids
    private LinkedList<Integer> blockedData;
    LinkedList<String> pendingObsoleteRenamings = new LinkedList<String>();
    // Temporary directory to serialize objects to/from
    private String serialDir;
    // Host where the application runs
    private String appHost;
    // Component logger - No need to configure, ProActive does
    private static final Logger logger = Logger.getLogger(Loggers.DIP_COMP);
    private static final boolean debug = logger.isDebugEnabled();

    public DataInfoProvider(String appHost, String serialDir) {
        nameToId = new TreeMap<String, Integer>();
        codeToId = new TreeMap<Integer, Integer>();
        idToData = new TreeMap<Integer, DataInfo>();
        renamingToValue = new TreeMap<String, Object>();
        renamingToIsSerialized = new HashMap<String, Boolean>();
        blockedData = new LinkedList<Integer>();
        pendingObsoleteRenamings = new LinkedList<String>();

        this.serialDir = serialDir;
        this.appHost = appHost;

        DataInfo.init();

        logger.info("Initialization finished");
    }

    public void setCoWorkers(TaskDispatcher TD) {
        this.TD = TD;
    }

    // DataAccess interface
    public DataAccessId registerDataAccess(AccessParams access) {

        if (access instanceof FileAccessParams) {
            FileAccessParams fAccess = (FileAccessParams) access;
            return registerFileAccess(fAccess.getMode(),
                    fAccess.getName(),
                    fAccess.getPath(),
                    fAccess.getHost(),
                    -1);
        } else {
            ObjectAccessParams oAccess = (ObjectAccessParams) access;
            return registerObjectAccess(oAccess.getMode(),
                    oAccess.getValue(),
                    oAccess.getCode(),
                    -1);
        }
    }

    public List<DataAccessId> registerDataAccesses(List<AccessParams> accesses, int readerMethodId) {
        ArrayList<DataAccessId> daIds = new ArrayList<DataAccessId>(accesses.size());
        for (AccessParams access : accesses) {
            if (access instanceof FileAccessParams) {
                FileAccessParams fAccess = (FileAccessParams) access;
                daIds.add(registerFileAccess(fAccess.getMode(),
                        fAccess.getName(),
                        fAccess.getPath(),
                        fAccess.getHost(),
                        readerMethodId));
            } else {
                ObjectAccessParams oAccess = (ObjectAccessParams) access;
                daIds.add(registerObjectAccess(oAccess.getMode(),
                        oAccess.getValue(),
                        oAccess.getCode(),
                        readerMethodId));
            }
        }

        return daIds;
    }

    private DataAccessId registerFileAccess(AccessMode mode,
            String fileName,
            String path,
            String host,
            int readerId) {
        DataInfo fileInfo;
        String locationKey = fileName + ":" + host + ":" + path;
        Integer fileId = nameToId.get(locationKey);

        // First access to this file
        if (fileId == null) {
            if (debug) {
                logger.debug("FIRST access to " + host + ":" + path + fileName);
            }

            // Update mappings
            fileInfo = new FileInfo(fileName, host, path);
            fileId = fileInfo.getDataId();
            nameToId.put(locationKey, fileId);
            idToData.put(fileId, fileInfo);

            // Inform the File Transfer Manager about the new file
            if (mode != AccessMode.W) {
                TD.newDataVersion(fileInfo.getLastDataInstanceId(),
                        fileName,
                        new Location(host, path));
            }
        } // The file has already been accessed
        else {
            if (debug) {
                logger.debug("Another access to " + host + ":" + path + fileName);
            }

            fileInfo = idToData.get(fileId);
        }

        // Version management
        return fileInfo.manageAccess(mode, readerId, debug, logger);
    }

    // Object access
    private DataAccessId registerObjectAccess(AccessMode mode,
            Object value,
            int code,
            int readerId) {
        DataInfo oInfo;
        Integer aoId = codeToId.get(code);

        // First access to this datum
        if (aoId == null) {
            if (debug) {
                logger.debug("FIRST access to object " + code);
            }

            // Update mappings
            oInfo = new ObjectInfo(code);
            aoId = oInfo.getDataId();
            codeToId.put(code, aoId);
            idToData.put(aoId, oInfo);

            if (readerId != -1) { // Not read from the main program
                // Serialize this first version of the object to a file
                DataInstanceId lastDID = oInfo.getLastDataInstanceId();
                String renaming = lastDID.getRenaming();
                try {
                    Serializer.serialize(value, serialDir + renaming);
                } catch (Exception e) {
                    logger.error(SERIALIZATION_ERR, e);
                    return null;
                }

                // Inform the File Transfer Manager about the new file containing the object
                if (mode != AccessMode.W) {
                    logger.debug("Location for object: " + appHost + "/" + serialDir);
                    TD.newDataVersion(lastDID,
                            renaming,
                            new Location(appHost, serialDir));
                }
            }
        } else {// The datum has already been accessed
            if (debug) {
                logger.debug("Another access to object " + code);
            }

            oInfo = idToData.get(aoId);

            if (mode != AccessMode.W) {
                DataInstanceId readInstance = oInfo.getLastDataInstanceId();
                String renaming = readInstance.getRenaming();
                if (renamingToValue.get(renaming) != null && renamingToIsSerialized.get(renaming) == null) {
                    // Serialize data accessed by main program
                    renamingToIsSerialized.put(renaming, true);
                    try {
                        logger.debug("Serialize Main Program Object " + readInstance + " to dir " + serialDir + readInstance.getRenaming());
                        Serializer.serialize(getObject(renaming), serialDir + renaming);
                        TD.newDataVersion(readInstance, renaming, new Location(appHost, serialDir));
                    } catch (Exception e) {
                        logger.error(SERIALIZATION_ERR, e);
                        return null;
                    }
                }
            }
        }

        // Version management
        return oInfo.manageAccess(mode, readerId, debug, logger);
    }

    public boolean alreadyAccessed(String fileName, String path, String host) {
        String locationKey = fileName + ":" + host + ":" + path;
        Integer fileId = nameToId.get(locationKey);

        return fileId != null;
    }

    // DataInformation interface
    public String getLastRenaming(int code) {
        Integer aoId = codeToId.get(code);
        DataInfo oInfo = idToData.get(aoId);
        return oInfo.getLastDataInstanceId().getRenaming();
    }

    public String getOriginalName(int fileId) {
        FileInfo info = (FileInfo) idToData.get(fileId);
        return info.getOriginalName();
    }

    public Location getOriginalLocation(int fileId) {
        FileInfo info = (FileInfo) idToData.get(fileId);
        return info.getOriginalLocation();
    }

    public LinkedList<String> dataHasBeenRead(List<DataAccessId> dataIds, int readerId) {
        LinkedList<String> obsoleteRenamings = new LinkedList<String>();
        if (!pendingObsoleteRenamings.isEmpty() && blockedData.isEmpty()) {// Flush pending obsolete renamings when there's no blocked data
            obsoleteRenamings.addAll(pendingObsoleteRenamings);
            pendingObsoleteRenamings.clear();
        }

        for (DataAccessId dAccId : dataIds) {
            Integer rDataId = null;
            Integer rVersionId = null;
            String rRenaming = null;
            Integer wDataId = null;
            Integer wVersionId = null;
            String wRenaming = null;
            if (dAccId instanceof RAccessId) {
                rDataId = ((RAccessId) dAccId).getReadDataInstance().getDataId();
                rVersionId = ((RAccessId) dAccId).getReadDataInstance().getVersionId();
                rRenaming = ((RAccessId) dAccId).getReadDataInstance().getRenaming();
            } else if (dAccId instanceof RWAccessId) {
                rDataId = ((RWAccessId) dAccId).getReadDataInstance().getDataId();
                rVersionId = ((RWAccessId) dAccId).getReadDataInstance().getVersionId();
                rRenaming = ((RWAccessId) dAccId).getReadDataInstance().getRenaming();
                wDataId = ((RWAccessId) dAccId).getWrittenDataInstance().getDataId();
                wVersionId = ((RWAccessId) dAccId).getWrittenDataInstance().getVersionId();
                wRenaming = ((RWAccessId) dAccId).getWrittenDataInstance().getRenaming();
            } else {
                wDataId = ((WAccessId) dAccId).getWrittenDataInstance().getDataId();
                wVersionId = ((WAccessId) dAccId).getWrittenDataInstance().getVersionId();
                wRenaming = ((WAccessId) dAccId).getWrittenDataInstance().getRenaming();
            }
            if (rDataId != null) {
                DataInfo fileInfo = idToData.get(rDataId);
                if (fileInfo.versionHasBeenRead(rVersionId, readerId) == 0 && (fileInfo.getLastVersionId() != rVersionId || fileInfo.isToDelete())) {
                    if (blockedData.contains(rDataId)) {
                        pendingObsoleteRenamings.add(rRenaming);
                    } else {
                        obsoleteRenamings.add(rRenaming);
                    }
                }
            }
            if (wDataId != null) {
                DataInfo fileInfo = idToData.get(wDataId);
                if (fileInfo == null) {
                    if (blockedData.contains(wDataId)) {
                        pendingObsoleteRenamings.add(wRenaming);
                    } else {
                        obsoleteRenamings.add(wRenaming);
                    }
                } else if (fileInfo.isToDelete()) {
                    idToData.remove(wDataId);
                    if (blockedData.contains(wDataId)) {
                        pendingObsoleteRenamings.add(wRenaming);
                    } else {
                        obsoleteRenamings.add(wRenaming);
                    }
                }
            }
        }

        if (debug) {
            for (String renaming : obsoleteRenamings) {
                logger.debug("Detected file " + renaming + " as obsolete");
            }
            for (String renaming : pendingObsoleteRenamings) {
                logger.debug("File " + renaming + " is in pending obsolete renamings");
            }
        }
        return obsoleteRenamings;
    }

    public void setObjectVersionValue(String renaming, Object value) {
        renamingToValue.put(renaming, value);
    }

    public boolean isHere(DataInstanceId dId) {
        return renamingToValue.get(dId.getRenaming()) != null;
    }

    public Object getObject(String renaming) {
        return renamingToValue.get(renaming);
    }

    public void newVersionSameValue(String rRenaming, String wRenaming) {
        renamingToValue.put(wRenaming, renamingToValue.get(rRenaming));
    }

    public DataInstanceId getLastDataAccess(int code) {
        Integer aoId = codeToId.get(code);
        DataInfo oInfo = idToData.get(aoId);
        return oInfo.getLastDataInstanceId();
    }

    public List<DataInstanceId> getLastVersions(TreeSet<Integer> dataIds) {
        List<DataInstanceId> versionIds = new ArrayList<DataInstanceId>(dataIds.size());
        for (Integer dataId : dataIds) {
            DataInfo dataInfo = idToData.get(dataId);
            if (dataInfo != null) {
                versionIds.add(dataInfo.getLastDataInstanceId());
            } else {
                versionIds.add(null);
            }
        }
        return versionIds;
    }

    public void blockDataIds(TreeSet<Integer> dataIds) {
        blockedData.addAll(dataIds);
    }

    public void unblockDataId(Integer dataId) {
        blockedData.remove(dataId);
    }

    public FileInfo deleteData(String host, String path, String fileName) {
        String locationKey = fileName + ":" + host + ":" + path;
        Integer fileId = nameToId.get(locationKey);
        if (fileId == null) {
            return null;
        }

        FileInfo fileInfo = (FileInfo) idToData.get(fileId);
        if (fileInfo.getReaders() == 0) {
            nameToId.remove(locationKey);
            idToData.remove(fileId);
        } else {
            fileInfo.setToDelete(true);
        }
        return fileInfo;
    }

    public LinkedList<DataInstanceId> getInstances(String host, String path, String fileName) {
        String locationKey = fileName + ":" + host + ":" + path;
        Integer fileId = nameToId.get(locationKey);
        if (fileId == null) {
            return new LinkedList();
        }
        FileInfo fileInfo = (FileInfo) idToData.get(fileId);
        return fileInfo.getAllDataInstances();
    }
}
