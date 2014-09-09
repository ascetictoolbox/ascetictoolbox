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

import integratedtoolkit.ITConstants;
import integratedtoolkit.api.ITExecution.ParamDirection;
import static integratedtoolkit.api.impl.IntegratedToolkitImpl.masterSafeLocation;
import integratedtoolkit.components.DataInfoUpdate;
import integratedtoolkit.components.FileTransfer;
import integratedtoolkit.components.TransferStatus.TransferState;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.Parameter.DependencyParameter;
import integratedtoolkit.types.Parameter.DependencyParameter.FileParameter;
import integratedtoolkit.types.Parameter.DependencyParameter.ObjectParameter;
import integratedtoolkit.types.WSJob;
import integratedtoolkit.types.data.*;
import integratedtoolkit.types.data.DataAccessId.RAccessId;
import integratedtoolkit.types.data.DataAccessId.RWAccessId;
import integratedtoolkit.types.data.DataAccessId.WAccessId;
import integratedtoolkit.types.data.FileOperation.Copy;
import integratedtoolkit.types.data.FileOperation.Delete;
import integratedtoolkit.types.data.FileOperation.OpEndState;
import integratedtoolkit.types.request.td.TransferObjectRequest;
import integratedtoolkit.types.ResourceDestructionRequest;
import integratedtoolkit.util.Cleaner;
import integratedtoolkit.util.ElementNotFoundException;
import integratedtoolkit.util.GroupManager;
import integratedtoolkit.util.ProjectManager;
import integratedtoolkit.util.RequestDispatcher;
import integratedtoolkit.util.RequestQueue;
import integratedtoolkit.util.SharedDiskManager;
import integratedtoolkit.util.ThreadPool;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.LogicalFile;

public class FileTransferManager implements FileTransfer, DataInfoUpdate {

    // Constants definition
    private static final int POOL_SIZE = 5;
    private static final String POOL_NAME = "FTM";
    private static final int SAFE_POOL_SIZE = 1;
    private static final String SAFE_POOL_NAME = "SAFE_FTM";
    private static final String CLEAN_SCRIPT = "clean.sh";
    private static final String ANY_PROT = "any://";
    private static final String THREAD_POOL_ERR = "Error starting pool of threads";
    private static final String DELETE_ERR = "Error deleting intermediate files";
    private static final String LF_PREPARATION_ERR = "Error preparing logical file to replicate";
    private static final String VERSION_CREATION_ERR = "Error creating logical file";
    private static final String TRANSFER_ERR = "Error transferring file";
    private static final String TRANSFERS_LEFT_ERR = "Error calculating the number of transfers left in a group";
    private static final String POOL_ERR = "Error stopping threads of pool";
    private static final String NO_SAFE_COPY = "Error saving a file before terminating a VM";
    private static final String OBJ_SERIALIZE_ERR = "Error serializing object to file for transfer";
    private static final String RES_FILE_TRANSFER_ERR = "Error transferring result files";
    // Client interfaces
    private JobManager JM;
    private TaskDispatcher TD;
    // Map : hostname --> All logical files contained in it.
    private TreeMap<String, LinkedList<LogicalFile>> hostToFiles;
    /* Map : logical file name -> logical file
     * It acts as a logical file repository, where logical files can be
     * retrieved giving their logical name
     */
    private Map<String, PhysicalDataInfo> nameToPhysicalData;
    // Transfers in progress
    private Map<URI, Copy> inProgress;
    // Transfers to SharedDisks in progress
    private Map<String, Copy> inProgressShared;
    // Pool of worker threads and queue of requests
    private ThreadPool pool;
    private RequestQueue<FileOperation> copyQueue;
    // Pool of worker threads and queue of requests
    private ThreadPool safePool;
    private RequestQueue<FileOperation> safeQueue;
    // Manager for groups of operations
    private GroupManager opGroups;
    // GAT context
    private GATContext context;
    private boolean userNeeded;
    // Component logger - No need to configure, ProActive does
    private static final Logger logger = Logger.getLogger(Loggers.FTM_COMP);
    private static final boolean debug = logger.isDebugEnabled();
    // Tracing
    private static final boolean tracing = System.getProperty(ITConstants.IT_TRACING) != null
            && System.getProperty(ITConstants.IT_TRACING).equals("true")
            ? true : false;
    private TreeMap<Integer, ResourceDestructionRequest> group2Host;
    // List of hosts waiting for the end of all its safe transfers
    private LinkedList<String> stoppingHosts;

    public FileTransferManager() {
        // GAT adaptor path
        context = new GATContext();
        String adaptor = System.getProperty(ITConstants.GAT_FILE_ADAPTOR);
        /* We need to try the local adaptor when both source and target hosts
         * are local, because ssh file adaptor cannot perform local operations
         */
        context.addPreference("File.adaptor.name", adaptor + ", srcToLocalToDestCopy, local");
        for (Entry<String, String> e : ProjectManager.getFileAdaptorPreferences().entrySet()) {
            context.addPreference(e.getKey(), e.getValue());
        }

        userNeeded = adaptor.regionMatches(true, 0, "ssh", 0, 3);

        opGroups = new GroupManager();

        // Create threads that will handle (blocking) file transfer requests
        copyQueue = new RequestQueue<FileOperation>();
        safeQueue = new RequestQueue<FileOperation>();
        int poolSize = POOL_SIZE + SAFE_POOL_SIZE;
        inProgress = new HashMap<URI, Copy>(poolSize + poolSize / 2);
        inProgressShared = new HashMap<String, Copy>(poolSize + poolSize / 2);
        pool = new ThreadPool(POOL_SIZE, POOL_NAME, new TransferDispatcher(copyQueue, POOL_SIZE));
        try {
            pool.startThreads();
        } catch (Exception e) {
            logger.error(THREAD_POOL_ERR, e);
            System.exit(1);
        }

        safePool = new ThreadPool(SAFE_POOL_SIZE, SAFE_POOL_NAME, new TransferDispatcher(safeQueue, SAFE_POOL_SIZE));
        try {
            safePool.startThreads();
        } catch (Exception e) {
            logger.error(THREAD_POOL_ERR, e);
            System.exit(1);
        }

        nameToPhysicalData = Collections.synchronizedMap(new TreeMap<String, PhysicalDataInfo>());
        group2Host = new TreeMap<Integer, ResourceDestructionRequest>();
        hostToFiles = new TreeMap<String, LinkedList<LogicalFile>>();

        stoppingHosts = new LinkedList<String>();

        logger.info("Initialization finished");
    }

    public void setCoWorkers(TaskDispatcher TD, JobManager JM) {
        this.TD = TD;
        this.JM = JM;
    }

    public void cleanup() {
        // Make pool threads finish
        try {
            pool.stopThreads();
            safePool.stopThreads();
        } catch (Exception e) {
            logger.error(POOL_ERR, e);
        }

        GAT.end();

        logger.info("Cleanup done");
    }

    public int transferFiles(Integer groupId, List<DependencyParameter> fileAccesses, Location targetLocation) {
        if (debug) {
            logger.debug("Job files");
        }

        return transferFiles(groupId, fileAccesses, targetLocation, FileRole.JOB_FILE, null);
    }

    public int getTransferId(int numTransfers, FileRole role) {
        int groupId = opGroups.addGroup(numTransfers, role, null);
        return groupId;
    }
    // Private method that performs file transfers

    private int transferFiles(
            Integer groupId,
            List<DependencyParameter> parameters,
            Location targetLocation,
            FileRole role,
            Semaphore sem) {
        if (parameters.isEmpty()) {
            return FileTransfer.FILES_READY;
        }

        RAccessId raId;
        WAccessId waId;
        RWAccessId rwaId;
        PhysicalDataInfo dip = null, originalDip = null;
        DataInstanceId sourceFile = null, targetFile;
        String targetName = null;
        int transfersLeft = parameters.size();
        // Create the group of operations for files
        if (groupId == null) {
            groupId = opGroups.addGroup(parameters.size(), role, sem);
        }
        for (DependencyParameter dp : parameters) {
            boolean workOnCopy = false;
            DataAccessId faId = dp.getDataAccessId();
            if (faId instanceof RAccessId) {
                raId = (RAccessId) faId;
                sourceFile = raId.getReadDataInstance();
                targetName = sourceFile.getRenaming();
                dip = nameToPhysicalData.get(targetName);
            } else {
                if (faId instanceof WAccessId) { // Not possible for an open file role
                    // "False" transfer, no real transfer is needed
                    waId = (WAccessId) faId;
                    targetFile = waId.getWrittenDataInstance();

                    try {
                        transfersLeft = opGroups.removeMember(groupId);
                    } catch (ElementNotFoundException e) {
                        String errMessage = TRANSFERS_LEFT_ERR + ": group is " + groupId
                                + ", target location is " + targetLocation + ", role is " + role;
                        logger.fatal(errMessage, e);
                        System.exit(1);
                    }

                } else { // instance of RWAccessId
                    rwaId = (RWAccessId) faId;
                    sourceFile = rwaId.getReadDataInstance();
                    targetFile = rwaId.getWrittenDataInstance();
                    originalDip = nameToPhysicalData.get(sourceFile.getRenaming());
                    LogicalFile origLogicalFile = originalDip.getLogicalFile();
                    workOnCopy = true;
                    dip = new PhysicalDataInfo(originalDip.getValue(), origLogicalFile);

                }
                targetName = targetFile.getRenaming();
            }
            
            if (!(faId instanceof WAccessId)) {
                Copy c = new Copy(groupId,
                        dip,
                        targetName,
                        targetLocation,
                        workOnCopy,
                        dp);

                if (debug) {
                    logger.debug("File: " + faId.getClass().getSimpleName()
                            + sourceFile
                            + " to " + targetLocation + "/" + targetName);
                }
                copyQueue.enqueue(c);
            } else {
                dp.setDataRemotePath(ProjectManager.getResourceProperty(targetLocation.getHost(), ITConstants.WORKING_DIR) + "/" + targetName);
                logger.debug("Set remote path OK to " + ProjectManager.getResourceProperty(targetLocation.getHost(), ITConstants.WORKING_DIR) + "/" + targetName);
                if (debug) {
                    logger.debug("File: " + faId.getClass().getSimpleName()
                            + " will be generated in "
                            + targetLocation + "/" + targetName);
                }
            }
        }
        // Check if all requested transfers are ready
        if (transfersLeft == 0) {
            opGroups.removeGroup(groupId);
            return FileTransfer.FILES_READY;
        }

        return groupId;
    }

    // FileTransfer interface
    public void transferBackResultFiles(List<ResultFile> resFiles, Semaphore sem) {
        ListIterator<ResultFile> rfIterator = resFiles.listIterator();
        while (rfIterator.hasNext()) {
            String origName = rfIterator.next().getOriginalName();
            if (origName.startsWith("compss-serialized-obj_")) { // Do not transfer objects serialized by the bindings
                logger.debug("Discarding file " + origName + " as a result");
                rfIterator.remove();
            }
        }
        int size = resFiles.size();
        if (size == 0) {
            sem.release();
            return;
        }

        // Create the group of operations for result files transfer
        int opGId = opGroups.addGroup(size, FileRole.RESULT_FILE, sem);

        for (ResultFile resFile : resFiles) {
            DataInstanceId fId = resFile.getFileInstanceId();
            String renaming = fId.getRenaming();

            // Look for the last available version
            while (!nameToPhysicalData.containsKey(renaming) && renaming != null) {
                renaming = DataInstanceId.previousVersionRenaming(renaming);
            }

            if (renaming == null) {
                logger.error(RES_FILE_TRANSFER_ERR + ": Cannot transfer file " + fId.getRenaming() + " nor any of its previous versions");
                sem.release();
                return;
            }

            Copy c = new Copy(opGId,
                    nameToPhysicalData.get(renaming),
                    resFile.getOriginalName(),
                    resFile.getOriginalLocation(),
                    false,
                    new FileParameter(ParamDirection.IN, "", "", ""));
            copyQueue.enqueue(c);
        }
    }

    public void transferFileForOpen(DataAccessId faId, Location targetLocation, Semaphore sem) {
        /* If the application requests to open a file in W mode, no transfer is actually
         * performed, we only update the information about the location of the new version.
         * And then we inform the application that it can continue
         */
        if (faId instanceof WAccessId) {
            WAccessId waId = (WAccessId) faId;
            DataInstanceId targetFile = waId.getWrittenDataInstance();
            String targetName = targetFile.getRenaming();

            if (debug) {
                logger.debug(targetFile + " to be opened as " + targetLocation + targetName);
            }
            newDataVersion(targetFile.getRenaming(), targetName, targetLocation);
            sem.release();
        } else {
            List<DependencyParameter> list = new LinkedList<DependencyParameter>();
            FileParameter fp = new FileParameter(ParamDirection.IN, "", "", "");
            fp.setDataAccessId(faId);
            list.add(fp);
            transferFiles(null, list, targetLocation, FileRole.OPEN_FILE, sem);
        }
    }

    public void transferFileRaw(DataAccessId faId, Location targetLocation, Semaphore sem) {
        RAccessId raId = (RAccessId) faId;
        DataInstanceId sourceFile = raId.getReadDataInstance();
        int groupId = opGroups.addGroup(1, FileRole.RAW_FILE, sem); // Actually we don't care about the group
        String targetName = sourceFile.getRenaming();
        // Make a copy of the original logical file, we don't want to leave track
        LogicalFile logicalFile = nameToPhysicalData.get(targetName).getLogicalFile();
        PhysicalDataInfo pdi = new PhysicalDataInfo(logicalFile);
        Copy c = new Copy(groupId,
                pdi,
                targetName,
                targetLocation,
                true,
                new FileParameter(ParamDirection.IN, "", "", ""));
        copyQueue.enqueue(c);
    }

    public void transferTraceFiles(Location targetLocation, Semaphore sem) {
        List<String> hosts = ProjectManager.getPhysicWorkers();
        int groupId = opGroups.addGroup(hosts.size(), FileRole.TRACE_FILE, sem);

        for (String host : hosts) {
            int nslots = Integer.parseInt(ProjectManager.getResourceProperty(host, ITConstants.LIMIT_OF_TASKS));
            if (nslots <= 0) {
                try {
                    opGroups.removeMember(groupId);
                    if (!opGroups.hasMembers(groupId)) {
                        sem.release();
                    }
                } catch (ElementNotFoundException e) {
                    break;
                }
                continue;
            }

            String fileName = host + "_compss_trace.tar.gz";
            String workingDir = ProjectManager.getResourceProperty(host, ITConstants.WORKING_DIR);
            String user = ProjectManager.getResourceProperty(host, ITConstants.USER);
            if (userNeeded && user != null) {
                user += "@";
            } else {
                user = "";
            }

            LogicalFile logicalFile = null;
            try {
                logicalFile = GAT.createLogicalFile(context, fileName, LogicalFile.TRUNCATE);
                logicalFile.addURI(new URI(ANY_PROT + user + host + "/" + workingDir + "/" + fileName));
            } catch (Exception e) {
                logger.error("Error getting trace file for host " + host, e);
                continue;
            }

            Copy c = new Copy(groupId,
                    new PhysicalDataInfo(logicalFile),
                    fileName,
                    targetLocation,
                    true,
                    new FileParameter(ParamDirection.IN, "", "", ""));
            copyQueue.enqueue(c);
        }

    }

    public void deleteIntermediateFiles(Semaphore sem) {
        List<String> hosts = ProjectManager.getPhysicWorkers();

        if (hosts.isEmpty()) {
            sem.release();
            return;
        }

        LinkedList<URI> cleanScripts = new LinkedList<URI>();
        LinkedList<String> cleanParams = new LinkedList<String>();
        for (String host : hosts) {
            int nslots = Integer.parseInt(ProjectManager.getResourceProperty(host, ITConstants.LIMIT_OF_TASKS));
            if (nslots <= 0) {
                continue;
            }

            String installDir = ProjectManager.getResourceProperty(host, ITConstants.INSTALL_DIR);
            String workingDir = ProjectManager.getResourceProperty(host, ITConstants.WORKING_DIR);

            String user = ProjectManager.getResourceProperty(host, ITConstants.USER);
            if (user == null) {
                user = "";
            } else {
                user += "@";
            }

            try {
                cleanScripts.add(new URI(ANY_PROT + user + host + "/" + installDir + "/" + CLEAN_SCRIPT));
            } catch (URISyntaxException e) {
                logger.error(DELETE_ERR, e);
            }
            String pars = workingDir + " " + tracing;
            if (tracing) {
                pars += " " + host;
            }
            cleanParams.add(pars);
        }

        new Cleaner(cleanScripts, cleanParams);

        sem.release();
    }

    // DataInfoUpdate interface
    public void newDataVersion(String newDataId, String fileName, Location location) {
        if (debug) {
            logger.debug("Create new file version:");
            logger.debug("  * File Id: " + newDataId);
            logger.debug("  * Location: " + location + "/" + fileName);
        }

        String user = "";
        String host = location.getHost();
        if (host.startsWith("shared:")) {
            String sharedDisk = host.substring(7);
            LinkedList<String> hosts = SharedDiskManager.getAllMachinesfromDisk(sharedDisk);
            if (hosts != null && hosts.isEmpty()) {
                String errMessage = VERSION_CREATION_ERR + ": Name is " + newDataId
                        + ", Initial location is " + location;
                logger.fatal(errMessage);
                System.exit(1);
            }
            host = hosts.get(0);
        }
        user = ProjectManager.getResourceProperty(host, ITConstants.USER);
        if (userNeeded && user != null) {
            user += "@";
        } else {
            user = "";
        }

        LogicalFile logicalFile = null;
        try {
            logicalFile = GAT.createLogicalFile(context, newDataId, LogicalFile.TRUNCATE);
            URI u = new URI("any://" + user + host + "/" + location.getPath() + "/" + fileName);
            synchronized (logicalFile.getURIs()) {
                logicalFile.addURI(u);
            }
        } catch (Exception e) {
            String errMessage = VERSION_CREATION_ERR + ": Name is " + newDataId
                    + ", Initial location is " + location;
            logger.fatal(errMessage, e);
            System.exit(1);
        }
        PhysicalDataInfo pdi = new PhysicalDataInfo(logicalFile);
        nameToPhysicalData.put(newDataId, pdi);
        LinkedList<LogicalFile> files = hostToFiles.get(location.getHost());
        if (files == null) {
            files = new LinkedList<LogicalFile>();
            hostToFiles.put(location.getHost(), files);
        }
        files.add(logicalFile);
        synchronized (inProgressShared) {
            SharedDiskManager.registerFile(newDataId, location, fileName);
        }
    }

    public void newDataVersion(String newDataId,
            String fileName,
            Object object) {

        if (debug) {
            logger.debug("Create new object version:");
            logger.debug("  * Data Id: " + newDataId);
            logger.debug("  * Location: " + masterSafeLocation + fileName);
        }

        LogicalFile logicalFile = null;
        try {
            logicalFile = GAT.createLogicalFile(context, newDataId, LogicalFile.TRUNCATE);
            URI u = new URI("any://" + masterSafeLocation.getHost() + "/" + masterSafeLocation.getPath() + "/" + fileName);
            synchronized (logicalFile.getURIs()) {
                logicalFile.addURI(u);
            }
        } catch (Exception e) {
            String errMessage = VERSION_CREATION_ERR + ": Name is " + newDataId
                    + ", Initial location is " + masterSafeLocation;
            logger.fatal(errMessage, e);
            System.exit(1);
        }
        PhysicalDataInfo pdi = new PhysicalDataInfo(object, logicalFile);
        nameToPhysicalData.put(newDataId, pdi);
        synchronized (inProgressShared) {
            SharedDiskManager.registerFile(newDataId, new Location(masterSafeLocation.getHost(), masterSafeLocation.getPath()), newDataId);
        }
    }

    public TreeSet<String> getHosts(DataInstanceId dId) {
        String renaming = dId.getRenaming();
        TreeSet<String> hosts = SharedDiskManager.getAllMachinesfromFile(renaming);
        PhysicalDataInfo pdi = nameToPhysicalData.get(renaming);
        LogicalFile lf = pdi.getLogicalFile();
        if (lf == null) {
            return hosts;
        }
        List<URI> uris;
        try {
            uris = lf.getURIs();
        } catch (Exception e) {
            return hosts;
        }
        synchronized (uris) {
            for (URI u : uris) {
                hosts.add(u.getHost());
            }
        }
        return hosts;
    }

    public TreeSet<String> getLocations(DataInstanceId daId) {
        TreeSet<String> locations = new TreeSet<String>();
        String renaming = daId.getRenaming();
        PhysicalDataInfo pdi = nameToPhysicalData.get(renaming);
        if (pdi == null) {
            return locations;
        }
        LogicalFile lf = pdi.getLogicalFile();
        if (lf == null) {
            return locations;
        }
        List<URI> uris;
        try {
            uris = lf.getURIs();
        } catch (Exception e) {
            return locations;
        }
        synchronized (uris) {
            for (URI u : uris) {
                locations.add(u.toString());
            }
        }
        return locations;
    }

    void transferObjectValue(TransferObjectRequest toRequest) {
        DataAccessId daId = toRequest.getDaId();
        String path = toRequest.getPath();
        String host = toRequest.getHost();
        RWAccessId rwaId = (RWAccessId) daId;
        DataInstanceId targetData = rwaId.getWrittenDataInstance();
        String rRenaming = rwaId.getReadDataInstance().getRenaming();
        String wRenaming = targetData.getRenaming();

        Object o = WSJob.getObjectVersionValue(rRenaming);
        if (o == null) {
            Location targetLocation = new Location(host, path);
            List<DependencyParameter> list = new LinkedList<DependencyParameter>();
            ObjectParameter op = new ObjectParameter(ParamDirection.IN, null, 0);
            op.setDataAccessId(daId);
            list.add(op);
            transferFiles(null, list, targetLocation, FileRole.OPEN_FILE, toRequest.getSemaphore());
        } else {
            newDataVersion(targetData.getRenaming(), wRenaming, o);
            toRequest.setResponse(o);
            toRequest.getSemaphore().release();
        }
    }

    public void transferStopFiles(ResourceDestructionRequest rdr, String[] workerPerMethod) {
        String hostName = rdr.getRequested().getName();
        //int maxMethod = 0;
        LinkedList<LogicalFile> safeFiles = new LinkedList<LogicalFile>();
        int orderedOperations = 0;
        synchronized (inProgress) {
            //Ask for all useful files & delete them from FIP
            LinkedList<LogicalFile> allHostFiles = hostToFiles.get(hostName);
            if (allHostFiles == null) {
                TD.safeResourceEnd(rdr);
                return;
            }
            stoppingHosts.add(hostName);
            int opGId = opGroups.addGroup(0, FileRole.SAFE_FILE, null);
            for (LogicalFile lf : allHostFiles) {
                try {
                    synchronized (lf.getURIs()) {
                        boolean replicated = false;
                        URI targetURI = null;
                        for (URI u : lf.getURIs()) {
                            if (!stoppingHosts.contains(u.getHost())) {
                                replicated = true;
                                break;
                            }
                            synchronized (inProgressShared) {
                                if (u.getHost().compareTo(hostName) == 0) {
                                    String diskName = SharedDiskManager.getSharedName(hostName, u.getPath());
                                    if (diskName != null) {
                                        LinkedList<String> candidates = SharedDiskManager.getAllMachinesfromDisk(diskName);
                                        for (String candidate : candidates) {
                                            if (!stoppingHosts.contains(candidate)) {
                                                String targetUser = ProjectManager.getResourceProperty(candidate,
                                                        ITConstants.USER);
                                                if (userNeeded && targetUser != null) {
                                                    targetUser += "@";
                                                } else {
                                                    targetUser = "";
                                                }
                                                String path = u.getPath();
                                                path = path.substring(SharedDiskManager.getMounpoint(hostName, diskName).length());
                                                path = SharedDiskManager.getMounpoint(candidate, diskName) + path;
                                                try {
                                                    targetURI = new URI("any://"
                                                            + targetUser
                                                            + candidate + "/"
                                                            + path + "/"
                                                            + "");
                                                } catch (URISyntaxException e) {
                                                    continue;
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (targetURI != null) {
                            lf.addURI(targetURI);
                            replicated = true;
                        }
                        if (!replicated) {
                            PhysicalDataInfo pdi = new PhysicalDataInfo(lf);
                            // Order copy
                            Copy c;
                            //if (maxMethod < 0 || maxMethod > workerPerMethod.length || workerPerMethod[maxMethod] == null) {
                            c = new Copy(opGId,
                                    pdi,
                                    lf.getName(),
                                    masterSafeLocation,
                                    false,
                                    new FileParameter(ParamDirection.IN, "", "", ""));
                            /*} else {
                             String host = workerPerMethod[maxMethod];
                             String workingDir = ProjectManager.getResourceProperty(host, ITConstants.WORKING_DIR);
                             c = new Copy(opGId,
                             pdi,
                             lf.getName(),
                             new Location(host, workingDir),
                             false);
                             }*/
                            orderedOperations++;
                            safeQueue.enqueue(c);
                            opGroups.addMember(opGId);
                            safeFiles.add(lf);
                            //maxMethod = ((maxMethod + 2) % workerPerMethod.length) - 1;
                        }
                    }
                } catch (Exception e) {
                    String errMessage = LF_PREPARATION_ERR + ":  target location is " + new Location("bsccs189", "/home/flordan/Escritorio/SafeFiles");
                    logger.fatal(errMessage, e);
                    System.exit(1);
                }
            }
            if (orderedOperations == 0) {
                // REMOVE ALL HOST URIS;
                for (LogicalFile lf : allHostFiles) {
                    try {
                        LinkedList<URI> ownedURIs = new LinkedList<URI>();
                        synchronized (lf.getURIs()) {
                            for (URI u : lf.getURIs()) {
                                if (u.getHost().compareTo(hostName) == 0) {
                                    ownedURIs.add(u);
                                }
                            }
                            lf.getURIs().removeAll(ownedURIs);
                        }
                    } catch (Exception e) {
                        String errMessage = "Can not remove all locations on " + hostName;
                        logger.fatal(errMessage, e);
                        System.exit(1);

                    }
                }

                if (inProgress.isEmpty()) { //NO MORE TRANSFERS IN PROGRESS --> SHUTDOWN
                    opGroups.removeGroup(opGId);
                    stoppingHosts.remove(hostName);
                    hostToFiles.remove(hostName);
                    TD.safeResourceEnd(rdr);
                } else {    //WAIT FOR IN PROGRESS TRANSFERS END
                    try {
                        Integer gId = opGroups.addGroup(inProgress.size(), FileRole.SHUTDOWN, null);
                        opGroups.addMembers(opGId, inProgress.size());
                        group2Host.put(gId, rdr);
                        for (Copy c : inProgress.values()) {
                            c.addGroupId(gId);
                        }
                    } catch (Exception e) {
                        String errMessage = "Can not wait for all current transfers to shutdown " + hostName;
                        logger.fatal(errMessage, e);
                        System.exit(1);

                    }
                }
            } else {
                group2Host.put(opGId, rdr);
            }
        }
    }

    public void obsoleteVersions(LinkedList<String> obsoletes) {
        HashMap<String, LinkedList<String>> obsoletesMap = new HashMap<String, LinkedList<String>>();
        if (obsoletes == null) {
            return;
        }
        synchronized (inProgressShared) {
            SharedDiskManager.unregisterFiles(obsoletes);
        }
        for (String renaming : obsoletes) {
            PhysicalDataInfo pdi = nameToPhysicalData.remove(renaming);
            if (pdi.isInMemory()) {
                continue;
            }
            LogicalFile lf = pdi.getLogicalFile();
            try {
                synchronized (lf.getURIs()) {
                    for (URI u : lf.getURIs()) {
                        hostToFiles.get(u.getHost()).remove(lf);
                        LinkedList<String> resourceList = obsoletesMap.get(u.getHost());
                        if (resourceList == null) {
                            resourceList = new LinkedList<String>();
                            obsoletesMap.put(u.getHost(), resourceList);
                        }
                        resourceList.add(renaming);
                    }
                }

            } catch (Exception e) {
            }
        }
        JM.obsoleteVersions(obsoletesMap);

    }

    void waitForTransfers(Semaphore sem) {
        int gId = opGroups.addGroup();
        opGroups.setSemaphore(gId, sem);
        synchronized (inProgress) {
            int copyCount = 0;
            copyCount += inProgress.size();
            for (Copy c : inProgress.values()) {
                c.addGroupId(gId);
            }
            copyCount += copyQueue.getNumRequests();
            for (FileOperation c : copyQueue.getQueue()) {
                ((Copy) c).addGroupId(gId);
            }
            copyCount += safeQueue.getNumRequests();
            for (FileOperation c : safeQueue.getQueue()) {
                ((Copy) c).addGroupId(gId);
            }
            try {
                opGroups.addMembers(gId, copyCount);
            } catch (Exception e) {
            }
            if (copyCount == 0) {
                sem.release();
            }
        }
    }

    // Threads that handle file transfer requests
    private class TransferDispatcher extends RequestDispatcher<FileOperation> {

        public TransferDispatcher(RequestQueue<FileOperation> queue, int poolSize) {
            super(queue);
        }

        public void processRequests() {
            FileOperation fOp;
            Copy c;
            Delete d;

            while (true) {
                fOp = queue.dequeue();
                if (fOp == null) {
                    break;
                }
                // What kind of operation is requested?
                if (fOp instanceof Copy) { 		// File transfer (copy)
                    c = (Copy) fOp;
                    doCopy(c);
                } else { // fOp instanceof Delete
                    d = (Delete) fOp;
                    doDelete(d);
                }

                // Check end state of the operation
                switch (fOp.getEndState()) {
                    case OP_OK:
                        checkNotifications(fOp);
                        break;
                    case OP_IN_PROGRESS:
                        break;
                    case OP_WAITING_SOURCES:
                        break;
                    default: // OP_FAILED or OP_PREPARATION_FAILED
                        notifyFailure(fOp);
                        break;
                }
            }
        }

        private void doCopy(Copy c) {
            boolean worksOnShare = false;
            String sharedDisk = null;
            String targetUser = ProjectManager.getResourceProperty(c.getTargetLocation().getHost(),
                    ITConstants.USER);
            if (userNeeded && targetUser != null) {
                targetUser += "@";
            } else {
                targetUser = "";
            }

            URI targetURI = null;
            try {
                targetURI = new URI("any://"
                        + targetUser
                        + c.getTargetLocation().getHost() + "/"
                        + c.getTargetLocation().getPath() + "/"
                        + c.getTargetName());
            } catch (URISyntaxException e) {
                c.setEndState(OpEndState.OP_PREPARATION_FAILED);
                c.setException(e);
                return;
            }

            PhysicalDataInfo pdi = c.getPhysicalDataInfo();

            try {
                if (pdi.isInMemory()) { // To avoid synchronize in case we don't have any object to serialize
                    synchronized (pdi) {
                        if (pdi.isInMemory()) {
                            pdi.writeToFile();
                        }
                    }
                }
            } catch (Exception e) {
                logger.fatal(OBJ_SERIALIZE_ERR, e);
                System.exit(1);
            }
            LogicalFile logicalFile = pdi.getLogicalFile();
            if (debug) {
                logger.debug("THREAD " + Thread.currentThread().getName()
                        + " - Copy file " + c.getName() + " to " + c.getTargetLocation() + "/" + c.getTargetName());
            }
            synchronized (inProgress) {
                if (c.workOnCopy()) {
                    //If works on a Copy (Raw Transfer or IO acces)--> the destination
                    //file is not in any shared disk
                    try {
                        LogicalFile copyLogicalFile = GAT.createLogicalFile(context, logicalFile.getName(), LogicalFile.TRUNCATE);
                        synchronized (logicalFile.getURIs()) {
                            // Make a copy of the logical file, to avoid modifying the original when replicating
                            for (URI u : logicalFile.getURIs()) {
                                copyLogicalFile.addURI(u);
                            }
                        }
                        logicalFile = copyLogicalFile;
                    } catch (Exception e) {
                        String errMessage = LF_PREPARATION_ERR + ": " + c.getName() + ", target location is " + c.getTargetLocation();
                        logger.fatal(errMessage, e);
                        System.exit(1);
                    }

                } else {
                    synchronized (inProgressShared) {
                        String path = SharedDiskManager.isShared(targetURI.getHost(), c.getTargetName());
                        if (path != null) {
                            //File is already present on the machine at location path
                            c.getDependencyParameter().setDataRemotePath(path);
                            c.setEndState(OpEndState.OP_OK);
                            if (debug) {
                                logger.debug("THREAD " + Thread.currentThread().getName()
                                        + " - A copy of " + c.getTargetName() + " is already present at " + targetURI.getHost() + " on path " + path);
                            }
                            return;
                        }

                        Copy copyInProgress;
                        LinkedList<String> sharedDisks = SharedDiskManager.getAllSharedNames(targetURI.getHost());
                        for (int i = 0; i < sharedDisks.size(); i++) {
                            copyInProgress = inProgressShared.get(sharedDisks.get(i) + "/" + c.getTargetName());
                            if (copyInProgress != null) {
                                path = SharedDiskManager.getMounpoint(targetURI.getHost(), sharedDisks.get(i)) + c.getTargetName();
                                c.getDependencyParameter().setDataRemotePath(path);
                                // The same operation is already in progress - no need to repeat it
                                c.setEndState(OpEndState.OP_IN_PROGRESS);

                                // This group must be notified as well when the operation finishes
                                synchronized (copyInProgress.getGroupIds()) {
                                    copyInProgress.addGroupId(c.getGroupIds().get(0));
                                }

                                if (debug) {
                                    logger.debug("THREAD " + Thread.currentThread().getName()
                                            + " - A copy to " + targetURI + " is already in progress, skipping replication");
                                }
                                return;
                            }
                        }

                        copyInProgress = inProgress.get(targetURI);
                        if (copyInProgress != null) {
                        	path = c.getTargetLocation().getPath();
                            if (!path.endsWith("/")) {
                                path += "/";
                            }
                            c.getDependencyParameter().setDataRemotePath(path + c.getTargetName());
                        	
                            // The same operation is already in progress - no need to repeat it
                            c.setEndState(OpEndState.OP_IN_PROGRESS);

                            // This group must be notified as well when the operation finishes
                            synchronized (copyInProgress.getGroupIds()) {
                                copyInProgress.addGroupId(c.getGroupIds().get(0));
                            }
                            if (debug) {
                                logger.debug("THREAD " + Thread.currentThread().getName()
                                        + " - A copy to " + targetURI + " is already in progress, skipping replication");
                            }
                            return;
                        }

                        sharedDisk = SharedDiskManager.getSharedName(c.getTargetLocation().getHost(), c.getTargetLocation().getPath());
                        if (sharedDisk != null) {
                            worksOnShare = true;
                            inProgressShared.put(sharedDisk + "/" + c.getTargetName(), c);
                        }
                    }
                }
                inProgress.put(targetURI, c);
            }

            //FileTransferUsageRecord ftur = null;
            try {
                // Perform the copy
                if (tracing) {
                    /* We ignore which source file will be chosen by GAT in the replication.
                     * Therefore, we create the usage record with the logical file name as source name.
                     * Nevertheless, we also pass a source file to create a UR with information about
                     * the physical file (such as the size)
                     */
                    /*String logicName = logicalFile.getName();
                     ftur = getUsageRecord(logicalFile.getFiles().get(0),
                     logicName,
                     targetURI.toString());*/
                }
                logicalFile.replicate(targetURI);
            } catch (Exception e) {
                c.setEndState(OpEndState.OP_FAILED);
                c.setException(e);
                return;
            } finally {
                synchronized (inProgress) {
                    Copy finishedCopy = inProgress.remove(targetURI);
                    if (tracing) {
                        //ftur.stop(finishedCopy.getGroupIds());
                    }
                    if (!c.workOnCopy()) {
                        LinkedList<LogicalFile> files = hostToFiles.get(c.getTargetLocation().getHost());
                        if (files == null) {
                            files = new LinkedList<LogicalFile>();
                            hostToFiles.put(c.getTargetLocation().getHost(), files);
                        }
                        files.add(logicalFile);
                        if (worksOnShare) {
                            synchronized (inProgressShared) {
                                inProgressShared.remove(sharedDisk + "/" + c.getTargetName());
                            }
                        }
                    }
                }
            }
            String path = c.getTargetLocation().getPath();
            if (!path.endsWith("/")) {
                path += "/";
            }
            c.getDependencyParameter().setDataRemotePath(path + c.getTargetName());
            c.setEndState(OpEndState.OP_OK);
            if (!c.workOnCopy()) {
                if (worksOnShare) {
                    synchronized (inProgressShared) {
                        sharedDisk = SharedDiskManager.getSharedName(c.getTargetLocation().getHost(), c.getTargetLocation().getPath());
                        inProgressShared.remove(sharedDisk + "/" + c.getTargetName());
                        SharedDiskManager.registerFile(c.getTargetName(), c.getTargetLocation(), c.getTargetName());
                    }
                }
            }
            if (debug) {
                logger.debug("THREAD " + Thread.currentThread().getName() + " - New source for " + c.getTargetName() + " has been created.");
            }
        }

        private void doDelete(Delete d) {
            if (debug) {
                logger.debug("THREAD " + Thread.currentThread().getName() + " Delete " + d.getFile());
            }

            try {
                d.getFile().delete();
            } catch (Exception e) {
                d.setEndState(OpEndState.OP_FAILED);
                d.setException(e);
                return;
            }

            d.setEndState(OpEndState.OP_OK);
        }

        private void checkNotifications(FileOperation fOp) {
            List<Integer> groupIds = fOp.getGroupIds();
            synchronized (fOp.getGroupIds()) {
                for (int groupId : groupIds) {
                    int numOps = 0;
                    try {
                        numOps = opGroups.removeMember(groupId);
                    } catch (ElementNotFoundException e) {
                        /* An operation belonging to the same group as the current one
                         * has failed and the group has been removed, don't do anything
                         */
                        continue;
                    }
                    // Are there any operations of this group left?
                    if (numOps == 0) {
                        int failedMembers = opGroups.hasFailedMembers(groupId);
                        FileRole fr = opGroups.removeGroup(groupId);
                        if (fr == null) {
                            continue;
                        }
                        ResourceDestructionRequest rdr;
                        String hostName;
                        // Notify the end of the group of operations
                        switch (fr) {
                            case JOB_FILE:
                                if (failedMembers == 0) {
                                    JM.fileTransferInfo(groupId, TransferState.DONE, null);
                                } else {
                                    JM.fileTransferInfo(groupId, TransferState.FAILED, failedMembers + " transfers failed.");
                                }
                                break;
                            case OPEN_FILE:
                                break;
                            case RESULT_FILE:
                                break;
                            /*case DELETE_FILE:
                             fOp.getSemaphore().release();
                             break;*/
                            case RAW_FILE:
                                break;
                            case SAFE_FILE:
                                rdr = group2Host.remove(groupId);
                                hostName = rdr.getRequested().getName();
                                synchronized (inProgress) {
                                    for (LogicalFile lf : hostToFiles.get(hostName)) {
                                        try {
                                            LinkedList<URI> ownedURIs = new LinkedList<URI>();
                                            synchronized (lf.getURIs()) {
                                                for (URI u : lf.getURIs()) {
                                                    if (u.getHost().compareTo(hostName) == 0) {
                                                        ownedURIs.add(u);
                                                    }
                                                }
                                                lf.getURIs().removeAll(ownedURIs);
                                            }
                                        } catch (Exception e) {
                                        }
                                    }
                                    if (inProgress.isEmpty()) {
                                        stoppingHosts.remove(hostName);
                                        hostToFiles.remove(hostName);
                                        SharedDiskManager.terminate(hostName);
                                        TD.safeResourceEnd(rdr);
                                    } else {
                                        Integer gId = opGroups.addGroup(inProgress.size(), FileRole.SHUTDOWN, null);
                                        group2Host.put(gId, rdr);
                                        for (Copy c : inProgress.values()) {
                                            c.addGroupId(gId);
                                        }
                                    }
                                }
                                break;
                            case TRACE_FILE:
                                break;
                            case SHUTDOWN:
                                rdr = group2Host.remove(groupId);
                                hostName = rdr.getRequested().getName();
                                stoppingHosts.remove(hostName);
                                hostToFiles.remove(hostName);
                                SharedDiskManager.terminate(hostName);
                                TD.safeResourceEnd(rdr);
                                break;
                        }
                    }
                }
            }
        }

        private void notifyFailure(FileOperation fOp) {
            synchronized (fOp.getGroupIds()) {
                for (int groupId : fOp.getGroupIds()) {
                    if (!opGroups.exists(groupId)) {
                        // A previous failure in the same group has already been notified, do nothing
                        continue;
                    }
                    FileRole fr = opGroups.getRole(groupId);
                    if (fr == null) {
                        continue;
                    }
                    if (debug) {
                        try {
                            logger.error("THREAD " + Thread.currentThread().getName() + " File Operation failed on " + fOp.getName()
                                    + ", file role is " + fr
                                    + ", operation end state is " + fOp.getEndState(),
                                    fOp.getException());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        logger.error("THREAD " + Thread.currentThread().getName() + " File Operation failed on " + fOp.getName()
                                + ", file role is " + fr
                                + ", operation end state is " + fOp.getEndState());
                    }
                    int numOps;
                    ResourceDestructionRequest rdr;
                    String hostName;
                    // Notify the end of the group of operations
                    switch (fr) {
                        case JOB_FILE:
                            try {
                                numOps = opGroups.failedMember(groupId);
                                if (numOps == 0) {
                                    int failedMembers = opGroups.hasFailedMembers(groupId);
                                    JM.fileTransferInfo(groupId,
                                            TransferState.FAILED,
                                            failedMembers + " transfers failed.");
                                }
                            } catch (Exception e) {
                                logger.fatal("Can not find transfer group");
                                System.exit(1);
                            }
                            break;
                        case OPEN_FILE:

                            opGroups.removeGroup(groupId);
                            logger.fatal(TRANSFER_ERR + ": File " + fOp.getName()
                                    + ", Role " + fr + "."
                                    + " Operation end state is " + fOp.getEndState(),
                                    fOp.getException());
                            System.exit(1);
                        case RESULT_FILE:
                            try {
                                numOps = opGroups.failedMember(groupId);
                                if (numOps == 0) {
                                    opGroups.removeGroup(groupId);
                                }
                                logger.error(TRANSFER_ERR + ": File " + fOp.getName()
                                        + ", Role " + fr + "."
                                        + " Operation end state is " + fOp.getEndState(),
                                        fOp.getException());
                            } catch (Exception e) {
                            }
                        case DELETE_FILE:
                            try {
                                numOps = opGroups.failedMember(groupId);
                                if (numOps == 0) {
                                    opGroups.removeGroup(groupId);
                                }
                                Delete d = (Delete) fOp;
                                String fileName = d.getFile().getName();
                                logger.error(DELETE_ERR + ": File " + fileName
                                        + ", Role " + fr + "."
                                        + " Operation end state is " + fOp.getEndState(),
                                        fOp.getException());
                            } catch (Exception e) {
                            }
                            break;
                        case RAW_FILE:
                            opGroups.removeGroup(groupId);
                            logger.fatal(TRANSFER_ERR + ": File " + fOp.getName()
                                    + ", Role " + fr + "."
                                    + " Operation end state is " + fOp.getEndState(),
                                    fOp.getException());
                            System.exit(1);
                            break;
                        case SAFE_FILE:
                            try {
                                numOps = opGroups.failedMember(groupId);
                                if (numOps == 0) {
                                    opGroups.removeGroup(groupId);
                                }
                            } catch (Exception e) {
                            }
                            logger.error(NO_SAFE_COPY
                                    + ": File " + fOp.getName()
                                    + ", Role " + fr + "."
                                    + " Operation end state is " + fOp.getEndState(),
                                    fOp.getException());
                            rdr = group2Host.remove(groupId);
                            hostName = rdr.getRequested().getName();
                            synchronized (inProgress) {
                                for (LogicalFile lf : hostToFiles.get(hostName)) {
                                    try {
                                        LinkedList<URI> ownedURIs = new LinkedList<URI>();
                                        synchronized (lf.getURIs()) {
                                            for (URI u : lf.getURIs()) {
                                                if (u.getHost().compareTo(hostName) == 0) {
                                                    ownedURIs.add(u);
                                                }
                                            }
                                            lf.getURIs().removeAll(ownedURIs);
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                                if (inProgress.isEmpty()) {
                                    stoppingHosts.remove(hostName);
                                    hostToFiles.remove(hostName);
                                    SharedDiskManager.terminate(hostName);
                                    TD.safeResourceEnd(rdr);
                                } else {
                                    Integer gId = opGroups.addGroup(inProgress.size(), FileRole.SHUTDOWN, null);
                                    group2Host.put(gId, rdr);
                                    for (Copy c : inProgress.values()) {
                                        c.addGroupId(gId);
                                    }
                                }
                            }
                            break;

                        case TRACE_FILE:
                            try {
                                numOps = opGroups.failedMember(groupId);
                                if (numOps == 0) {
                                    opGroups.removeGroup(groupId);
                                }
                                logger.error(TRANSFER_ERR + ": File " + fOp.getName()
                                        + ", Role " + fr + "."
                                        + " Operation end state is " + fOp.getEndState(),
                                        fOp.getException());
                            } catch (Exception e) {
                            }
                            break;

                        case SHUTDOWN:
                            try {
                                numOps = opGroups.failedMember(groupId);
                                if (numOps == 0) {
                                    rdr = group2Host.remove(groupId);
                                    hostName = rdr.getRequested().getName();
                                    stoppingHosts.remove(hostName);
                                    hostToFiles.remove(hostName);
                                    SharedDiskManager.terminate(hostName);
                                    TD.safeResourceEnd(rdr);
                                }
                            } catch (Exception e) {
                                logger.fatal("Can not find transfer group");
                                System.exit(1);
                            }

                            break;
                    }
                }
            }
        }
    }
}
