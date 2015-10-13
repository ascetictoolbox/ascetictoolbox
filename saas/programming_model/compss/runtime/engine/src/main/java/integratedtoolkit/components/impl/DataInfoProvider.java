/**
 *
 *   Copyright 2015-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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

package integratedtoolkit.components.impl;

import integratedtoolkit.comm.Comm;
import integratedtoolkit.types.data.location.DataLocation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.data.*;
import integratedtoolkit.types.data.AccessParams.*;
import integratedtoolkit.types.data.DataAccessId.*;
import integratedtoolkit.types.data.operation.FileTransferable;
import integratedtoolkit.types.data.operation.ObjectTransferable;
import integratedtoolkit.types.data.operation.OneOpWithSemListener;
import integratedtoolkit.types.data.operation.ResultListener;
import integratedtoolkit.types.request.ap.TransferObjectRequest;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.util.ResourceManager;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class DataInfoProvider {

    // Constants definition
    private static final String RES_FILE_TRANSFER_ERR = "Error transferring result files";
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

    private LinkedList<Integer> blockedData;
    // Map: Object_Version_Renaming -> Is_Serialized?
    private HashMap<String, Boolean> renamingToIsSerialized; // TODO: Remove obsolete from here

    LinkedList<String> pendingObsoleteRenamings = new LinkedList<String>();

    // Component logger - No need to configure, ProActive does
    private static final Logger logger = Logger.getLogger(Loggers.DIP_COMP);
    private static final boolean debug = logger.isDebugEnabled();

    public DataInfoProvider() {
        nameToId = new TreeMap<String, Integer>();
        codeToId = new TreeMap<Integer, Integer>();
        idToData = new TreeMap<Integer, DataInfo>();
        renamingToValue = new TreeMap<String, Object>();
        renamingToIsSerialized = new HashMap<String, Boolean>();
        blockedData = new LinkedList<Integer>();
        pendingObsoleteRenamings = new LinkedList<String>();
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
                    fAccess.getLocation(),
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
                        fAccess.getLocation(),
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
            DataLocation location,
            int readerId) {
        DataInfo fileInfo;
        String locationKey = location.getLocationKey();
        Integer fileId = nameToId.get(locationKey);

        // First access to this file
        if (fileId == null) {
            if (debug) {
                logger.debug("FIRST access to " + location.getLocationKey());
            }
            // Update mappings
            fileInfo = new FileInfo(location);
            fileId = fileInfo.getDataId();
            nameToId.put(locationKey, fileId);
            idToData.put(fileId, fileInfo);

            // Register the initial location of the file
            if (mode != AccessMode.W) {
                Comm.registerLocation(fileInfo.getLastDataInstanceId().getRenaming(), location);
            }
        } else {
            // The file has already been accessed, all location are already registered
            if (debug) {
                logger.debug("Another access to " + location.getLocationKey());
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

            // Serialize this first version of the object to a file
            DataInstanceId lastDID = oInfo.getLastDataInstanceId();
            String renaming = lastDID.getRenaming();

            // Inform the File Transfer Manager about the new file containing the object
            if (mode != AccessMode.W) {
                Comm.registerValue(renaming, value);
            }

        } else {// The datum has already been accessed
            if (debug) {
                logger.debug("Another access to object " + code);
            }

            oInfo = idToData.get(aoId);
        }
        // Version management
        return oInfo.manageAccess(mode, readerId, debug, logger);
    }

    public boolean alreadyAccessed(DataLocation loc) {
        String locationKey = loc.getLocationKey();
        Integer fileId = nameToId.get(locationKey);
        return fileId != null;
    }

    // DataInformation interface
    public String getLastRenaming(int code) {
        Integer aoId = codeToId.get(code);
        DataInfo oInfo = idToData.get(aoId);
        return oInfo.getLastDataInstanceId().getRenaming();
    }

    public DataLocation getOriginalLocation(int fileId) {
        FileInfo info = (FileInfo) idToData.get(fileId);
        return info.getOriginalLocation();
    }

    public void dataHasBeenRead(List<DataAccessId> dataIds, int readerId) {
        if (!pendingObsoleteRenamings.isEmpty() && blockedData.isEmpty()) {// Flush pending obsolete renamings when there's no blocked data
            for (String renaming : pendingObsoleteRenamings) {
                Comm.removeData(renaming);
            }
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
                        logger.debug("File " + rRenaming + " is in pending obsolete renamings");
                        pendingObsoleteRenamings.add(rRenaming);
                    } else {
                        logger.debug("Detected file " + rRenaming + " as obsolete");
                        Comm.removeData(rRenaming);
                    }
                }
            }
            if (wDataId != null) {
                DataInfo fileInfo = idToData.get(wDataId);
                if (fileInfo == null) {
                    if (blockedData.contains(wDataId)) {
                        logger.debug("File " + wRenaming + " is in pending obsolete renamings");
                        pendingObsoleteRenamings.add(wRenaming);
                    } else {
                        logger.debug("Detected file " + wRenaming + " as obsolete");
                        Comm.removeData(wRenaming);
                    }
                } else if (fileInfo.isToDelete()) {
                    idToData.remove(wDataId);
                    if (blockedData.contains(wDataId)) {
                        logger.debug("File " + wRenaming + " is in pending obsolete renamings");
                        pendingObsoleteRenamings.add(wRenaming);
                    } else {
                        logger.debug("Detected file " + wRenaming + " as obsolete");
                        Comm.removeData(wRenaming);
                    }
                }
            }
        }
    }

    public void setObjectVersionValue(String renaming, Object value) {
        renamingToValue.put(renaming, value);
        Comm.registerValue(renaming, value);
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

    public FileInfo deleteData(DataLocation loc) {
        String locationKey = loc.getLocationKey();
        Integer fileId = nameToId.get(locationKey);
        if (fileId == null) {
            return null;
        }

        FileInfo fileInfo = (FileInfo) idToData.get(fileId);
        if (fileInfo.getReaders() == 0) {
            nameToId.remove(locationKey);
            idToData.remove(fileId);
            Comm.removeData(fileInfo.getLastDataInstanceId().getRenaming());
        } else {
            fileInfo.setToDelete(true);
        }
        return fileInfo;
    }

    public void transferObjectValue(TransferObjectRequest toRequest) {
        Semaphore sem = toRequest.getSemaphore();
        DataAccessId daId = toRequest.getDaId();
        RWAccessId rwaId = (RWAccessId) daId;

        String sourceName = rwaId.getReadDataInstance().getRenaming();
        String targetName = rwaId.getWrittenDataInstance().getRenaming();

        LogicalData ld = Comm.getData(sourceName);

        if (ld.isInMemory()) {
            if (!ld.isOnFile()) { //Només s'haurà de fer si no hi ha readers
                try {
                    ld.writeToFile();
                } catch (Exception e) {
                    logger.fatal("Exception  writing object to file.", e);
                }
            }
            Object o = ld.removeValue();
            toRequest.setResponse(ld.getValue());
            toRequest.getSemaphore().release();
        } else {
            DataLocation targetLocation = DataLocation.getLocation(Comm.appHost, Comm.appHost.getTempDirPath() + sourceName);
            Comm.appHost.getData(sourceName, targetLocation, new ObjectTransferable(), new OneOpWithSemListener(sem));
        }
    }

    public ResultFile blockDataAndGetResultFile(int dataId, ResultListener listener) {
        DataInstanceId lastVersion;
        FileInfo fileInfo = (FileInfo) idToData.get(dataId);
        if (fileInfo != null) {
            String[] splitPath = fileInfo.getOriginalLocation().getPath().split(File.separator);
            String origName = splitPath[splitPath.length - 1];
            if (origName.startsWith("compss-serialized-obj_")) { // Do not transfer objects serialized by the bindings
                if (debug) {
                    logger.debug("Discarding file " + origName + " as a result");
                }
                return null;
            }
            lastVersion = fileInfo.getLastDataInstanceId();
            blockedData.add(dataId);
            ResultFile rf = new ResultFile(lastVersion, fileInfo.getOriginalLocation());

            DataInstanceId fId = rf.getFileInstanceId();
            String renaming = fId.getRenaming();

            // Look for the last available version
            while (renaming != null && !Comm.existsData(renaming)) {
                renaming = DataInstanceId.previousVersionRenaming(renaming);
            }
            if (renaming == null) {
                logger.error(RES_FILE_TRANSFER_ERR + ": Cannot transfer file " + fId.getRenaming() + " nor any of its previous versions");
                return null;
            }

            listener.addOperation();
            Comm.appHost.getData(renaming, rf.getOriginalLocation(), new FileTransferable(), listener);
            return rf;
        }
        return null;
    }

    public void shutdown() {
        Comm.appHost.deleteIntermediate();
        Collection<Worker> hosts = ResourceManager.getStaticResources();
        for (Worker worker : hosts) {
            worker.deleteIntermediate();
        }
    }

}
