package eu.ascetic.vmc.libvirt;

import static eu.ascetic.vmc.libvirt.Library.libvirt;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import eu.ascetic.vmc.libvirt.jna.DomainPointer;
import eu.ascetic.vmc.libvirt.jna.DomainSnapshotPointer;
import eu.ascetic.vmc.libvirt.jna.Libvirt;
import eu.ascetic.vmc.libvirt.jna.virDomainBlockInfo;
import eu.ascetic.vmc.libvirt.jna.virDomainBlockStats;
import eu.ascetic.vmc.libvirt.jna.virDomainInfo;
import eu.ascetic.vmc.libvirt.jna.virDomainInterfaceStats;
import eu.ascetic.vmc.libvirt.jna.virDomainJobInfo;
import eu.ascetic.vmc.libvirt.jna.virDomainMemoryStats;
import eu.ascetic.vmc.libvirt.jna.virSchedParameter;
import eu.ascetic.vmc.libvirt.jna.virVcpuInfo;

/**
 * A virtual machine defined within libvirt.
 */
@SuppressWarnings("unused")
public class Domain {

    public static final class BlockResizeFlags {
        /**
         * size is in bytes instead of KiB
         */
        public static final int BYTES = 1;
    }

    static final class CreateFlags {
        static final int VIR_DOMAIN_NONE = 0;
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_REDEFINE    = (1 << 0); /* Restore or alter
                                                                               metadata */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_CURRENT     = (1 << 1); /* With redefine, make
                                                                               snapshot current */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_NO_METADATA = (1 << 2); /* Make snapshot without
                                                                               remembering it */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_HALT        = (1 << 3); /* Stop running guest
                                                                               after snapshot */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_DISK_ONLY   = (1 << 4); /* disk snapshot, not
                                                                               system checkpoint */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_REUSE_EXT   = (1 << 5); /* reuse any existing
                                                                               external files */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_QUIESCE     = (1 << 6); /* use guest agent to
                                                                               quiesce all mounted
                                                                               file systems within
                                                                               the domain */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_ATOMIC      = (1 << 7); /* atomically avoid
                                                                               partial changes */
    }

    static final class MigrateFlags {
        static final int VIR_MIGRATE_LIVE              = (1 << 0); /* live migration */
        static final int VIR_MIGRATE_PEER2PEER         = (1 << 1); /* direct source -> dest host control channel */
        /* Note the less-common spelling that we're stuck with:
           VIR_MIGRATE_TUNNELLED should be VIR_MIGRATE_TUNNELED */
        static final int VIR_MIGRATE_TUNNELLED         = (1 << 2); /* tunnel migration data over libvirtd connection */
        static final int VIR_MIGRATE_PERSIST_DEST      = (1 << 3); /* persist the VM on the destination */
        static final int VIR_MIGRATE_UNDEFINE_SOURCE   = (1 << 4); /* undefine the VM on the source */
        static final int VIR_MIGRATE_PAUSED            = (1 << 5); /* pause on remote side */
        static final int VIR_MIGRATE_NON_SHARED_DISK   = (1 << 6); /* migration with non-shared storage with full disk copy */
        static final int VIR_MIGRATE_NON_SHARED_INC    = (1 << 7); /* migration with non-shared storage with incremental copy */
                                                                   /* (same base image shared between source and destination) */
        static final int VIR_MIGRATE_CHANGE_PROTECTION = (1 << 8); /* protect for changing domain configuration through the
                                                                    * whole migration process; this will be used automatically
                                                                    * when supported */
        static final int VIR_MIGRATE_UNSAFE            = (1 << 9); /* force migration even if it is considered unsafe */
    }

    static final class XMLFlags {
        /**
         * dump security sensitive information too
         */
        static final int VIR_DOMAIN_XML_SECURE = 1;
        /**
         * dump inactive domain information
         */
        static final int VIR_DOMAIN_XML_INACTIVE = 2;
        static final int VIR_DOMAIN_XML_UPDATE_CPU   = (1 << 2); /* update guest CPU requirements according to host CPU */
    }

    public static final class UndefineFlags {
        /**
         * Also remove any managed save
         */
        public static final int MANAGED_SAVE = (1 << 0);
        /**
         * If last use of domain, then also remove any snapshot metadata
         */
        public static final int SNAPSHOTS_METADATA = (1 << 1);
    }

    public static final class SnapshotListFlags {
        /**
         * Filter by snapshots with no parents, when listing a domain
         */
        public static final int ROOTS       = (1 << 0);

        /**
         * List all descendants, not just children, when listing a snapshot
         */
        public static final int DESCENDANTS = (1 << 0);

        /** For historical reasons, groups do not use contiguous bits. */

        /**
         * Filter by snapshots with no children
         */
        public static final int LEAVES      = (1 << 2);

        /**
         * Filter by snapshots that have children
         */
        public static final int NO_LEAVES   = (1 << 3);

        /**
         * Filter by snapshots which have metadata
         */
        public static final int METADATA    = (1 << 1);

        /**
         * Filter by snapshots with no metadata
         */
        public static final int NO_METADATA = (1 << 4);
    }

    /**
     * the native virDomainPtr.
     */
    DomainPointer VDP;

    /**
     * The Connect Object that represents the Hypervisor of this Domain
     */
    private Connect virConnect;

    /**
     * Constructs a Domain object from a known native DomainPointer, and a
     * Connect object.
     *
     * @param virConnect
     *            the Domain's hypervisor
     * @param VDP
     *            the native virDomainPtr
     */
    Domain(Connect virConnect, DomainPointer VDP) {
        this.virConnect = virConnect;
        this.VDP = VDP;
    }

    /**
     * Requests that the current background job be aborted at the soonest
     * opportunity. This will block until the job has either completed, or
     * aborted.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainAbortJob">Libvirt
     *      Documentation</a>
     * @return 0 in case of success and -1 in case of failure.
     * @throws LibvirtException
     */
    public int abortJob() throws LibvirtException {
        int returnValue = libvirt.virDomainAbortJob(VDP);
        processError();
        return returnValue;
    }

    /**
     * Creates a virtual device attachment to backend.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainAttachDevice">Libvirt
     *      Documentation</a>
     * @param xmlDesc
     *            XML description of one device
     * @throws LibvirtException
     */
    public void attachDevice(String xmlDesc) throws LibvirtException {
        libvirt.virDomainAttachDevice(VDP, xmlDesc);
        processError();
    }

    /**
     * Creates a virtual device attachment to backend.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainAttachDeviceFlags">Libvirt
     *      Documentation</a>
     * @param xmlDesc
     *            XML description of one device
     * @param flags
     *            the an OR'ed set of virDomainDeviceModifyFlags
     * @throws LibvirtException
     */
    public void attachDeviceFlags(String xmlDesc, int flags) throws LibvirtException {
        libvirt.virDomainAttachDeviceFlags(VDP, xmlDesc, flags);
        processError();
    }

    /**
     * This function returns block device (disk) stats for block devices
     * attached to the domain.
     *
     * @param path
     *            the path to the block device
     * @return the info, or null if an error
     * @throws LibvirtException
     */
    public DomainBlockInfo blockInfo(String path) throws LibvirtException {
        virDomainBlockInfo info = new virDomainBlockInfo();
        int success = libvirt.virDomainGetBlockInfo(VDP, path, info, 0);
        processError();
        return success == 0 ? new DomainBlockInfo(info) : null;
    }

    /**
     * Returns block device (disk) stats for block devices attached to this
     * domain. The path parameter is the name of the block device. Get this by
     * calling virDomainGetXMLDesc and finding the <target dev='...'> attribute
     * within //domain/devices/disk. (For example, "xvda"). Domains may have
     * more than one block device. To get stats for each you should make
     * multiple calls to this function. Individual fields within the
     * DomainBlockStats object may be returned as -1, which indicates that the
     * hypervisor does not support that particular statistic.
     *
     * @param path
     *            path to the block device
     * @return the statistics in a DomainBlockStats object
     * @throws LibvirtException
     */
    public DomainBlockStats blockStats(String path) throws LibvirtException {
        virDomainBlockStats stats = new virDomainBlockStats();
        int success = libvirt.virDomainBlockStats(VDP, path, stats, stats.size());
        processError();
        return success == 0 ? new DomainBlockStats(stats) : null;
    }

    /**
     * Resize a block device of domain while the domain is running.
     *
     * @param disk
     *           path to the block image, or shorthand (like vda)
     * @param size
     *           the new size of the block devices
     * @param flags
     *           bitwise OR'ed values of {@link BlockResizeFlags}
     * @throws LibvirtException
     */
    public void blockResize(String disk, long size, int flags) throws LibvirtException {
		int returnValue = libvirt.virDomainBlockResize(VDP, disk, size, flags);
        processError();
    }


    /**
     * Dumps the core of this domain on a given file for analysis. Note that for
     * remote Xen Daemon the file path will be interpreted in the remote host.
     *
     * @param to
     *            path for the core file
     * @param flags
     *            extra flags, currently unused
     * @throws LibvirtException
     */
    public void coreDump(String to, int flags) throws LibvirtException {
        libvirt.virDomainCoreDump(VDP, to, flags);
        processError();
    }

    /**
     * It returns the length (in bytes) required to store the complete CPU map
     * between a single virtual & all physical CPUs of a domain.
     */
    public int cpuMapLength(int maxCpus) {
        return (((maxCpus) + 7) / 8);
    }

    /**
     * Launches this defined domain. If the call succeed the domain moves from
     * the defined to the running domains pools.
     *
     * @throws LibvirtException
     */
    public int create() throws LibvirtException {
        int returnValue = libvirt.virDomainCreate(VDP);
        processError();
        return returnValue;
    }

    /**
     * Launches this defined domain with the provide flags.
     * If the call succeed the domain moves from
     * the defined to the running domains pools.
     *
     * @throws LibvirtException
     */
    public int create(int flags) throws LibvirtException {
        int returnValue = libvirt.virDomainCreateWithFlags(VDP, flags);
        processError();
        return returnValue;
    }

    /**
     * Destroys this domain object. The running instance is shutdown if not down
     * already and all resources used by it are given back to the hypervisor.
     * The data structure is freed and should not be used thereafter if the call
     * does not return an error. This function may requires priviledged access
     *
     * @throws LibvirtException
     */
    public void destroy() throws LibvirtException {
        libvirt.virDomainDestroy(VDP);
        processError();
    }

    /**
     * Destroys a virtual device attachment to backend.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainDetachDevice">Libvirt
     *      Documentation</a>
     * @param xmlDesc
     *            XML description of one device
     * @throws LibvirtException
     */
    public void detachDevice(String xmlDesc) throws LibvirtException {
        libvirt.virDomainDetachDevice(VDP, xmlDesc);
        processError();
    }

    /**
     * Destroys a virtual device attachment to backend.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainDetachDeviceFlags">Libvirt
     *      Documentation</a>
     * @param xmlDesc
     *            XML description of one device
     * @throws LibvirtException
     */
    public void detachDeviceFlags(String xmlDesc, int flags) throws LibvirtException {
        libvirt.virDomainDetachDeviceFlags(VDP, xmlDesc, flags);
        processError();
    }

    @Override
    protected void finalize() throws LibvirtException {
        free();
    }

    /**
     * Frees this domain object. The running instance is kept alive. The data
     * structure is freed and should not be used thereafter.
     *
     * @throws LibvirtException
     * @return number of references left (>= 0) for success, -1 for failure.
     */
    public int free() throws LibvirtException {
        int success = 0;
        if (VDP != null) {
            success = libvirt.virDomainFree(VDP);
            processError();
            VDP = null;
        }

        return success;
    }

    /**
     * Provides a boolean value indicating whether the network is configured to
     * be automatically started when the host machine boots.
     *
     * @return the result
     * @throws LibvirtException
     */
    public boolean getAutostart() throws LibvirtException {
        IntByReference autoStart = new IntByReference();
        libvirt.virDomainGetAutostart(VDP, autoStart);
        processError();
        return autoStart.getValue() != 0 ? true : false;
    }

    /**
     * Provides the connection object associated with a domain.
     *
     * @return the Connect object
     */
    public Connect getConnect() {
        return virConnect;
    }

    /**
     * Gets the hypervisor ID number for the domain
     *
     * @return the hypervisor ID
     * @throws LibvirtException
     */
    public int getID() throws LibvirtException {
        int returnValue = libvirt.virDomainGetID(VDP);
        processError();
        return returnValue;
    }

    /**
     * Extract information about a domain. Note that if the connection used to
     * get the domain is limited only a partial set of the information can be
     * extracted.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainGetInfo">Libvirt
     *      Documentation</a>
     *
     * @return a DomainInfo object describing this domain
     * @throws LibvirtException
     */
    public DomainInfo getInfo() throws LibvirtException {
        DomainInfo returnValue = null;
        virDomainInfo vInfo = new virDomainInfo();
        int success = libvirt.virDomainGetInfo(VDP, vInfo);
        processError();
        if (success == 0) {
            returnValue = new DomainInfo(vInfo);
        }
        return returnValue;
    }

    /**
     * Extract information about progress of a background job on a domain. Will
     * return an error if the domain is not active.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainGetJobInfo">Libvirt
     *      Documentation</a>
     * @return a DomainJobInfo object describing this domain
     * @throws LibvirtException
     */
    public DomainJobInfo getJobInfo() throws LibvirtException {
        DomainJobInfo returnValue = null;
        virDomainJobInfo vInfo = new virDomainJobInfo();
        int success = libvirt.virDomainGetJobInfo(VDP, vInfo);
        processError();
        if (success == 0) {
            returnValue = new DomainJobInfo(vInfo);
        }
        return returnValue;
    }

    /**
     * Retrieve the maximum amount of physical memory allocated to a domain.
     *
     * @return the memory in kilobytes
     * @throws LibvirtException
     */
    public long getMaxMemory() throws LibvirtException {
        NativeLong returnValue = libvirt.virDomainGetMaxMemory(VDP);
        processError();
        return returnValue.longValue();
    }

    /**
     * Provides the maximum number of virtual CPUs supported for the guest VM.
     * If the guest is inactive, this is basically the same as
     * virConnectGetMaxVcpus. If the guest is running this will reflect the
     * maximum number of virtual CPUs the guest was booted with.
     *
     * @return the number of VCPUs
     * @throws LibvirtException
     */
    public int getMaxVcpus() throws LibvirtException {
        int returnValue = libvirt.virDomainGetMaxVcpus(VDP);
        processError();
        return returnValue;
    }

    /**
     * Gets the public name for this domain
     *
     * @return the name
     * @throws LibvirtException
     */
    public String getName() throws LibvirtException {
        String returnValue = libvirt.virDomainGetName(VDP);
        processError();
        return returnValue;
    }

    /**
     * Gets the type of domain operation system.
     *
     * @return the type
     * @throws LibvirtException
     */
    public String getOSType() throws LibvirtException {
        Pointer ptr = libvirt.virDomainGetOSType(VDP);
        processError();
        try {
            return Library.getString(ptr);
        } finally {
            Library.free(ptr);
        }
    }

    /**
     * Gets the scheduler parameters.
     *
     * @return an array of SchedParameter objects
     * @throws LibvirtException
     */
    public SchedParameter[] getSchedulerParameters() throws LibvirtException {
        IntByReference nParams = new IntByReference();
        SchedParameter[] returnValue = new SchedParameter[0];
        Pointer pScheduler = libvirt.virDomainGetSchedulerType(VDP, nParams);
        processError();
        if (pScheduler != null) {
			String scheduler = Library.getString(pScheduler);
            Library.free(pScheduler);
            virSchedParameter[] nativeParams = new virSchedParameter[nParams.getValue()];
            returnValue = new SchedParameter[nParams.getValue()];
            libvirt.virDomainGetSchedulerParameters(VDP, nativeParams, nParams);
            processError();
            for (int x = 0; x < nParams.getValue(); x++) {
                returnValue[x] = SchedParameter.create(nativeParams[x]);
            }
        }

        return returnValue;
    }

    // getSchedulerType
    // We don't expose the nparams return value, it's only needed for the
    // SchedulerParameters allocations,
    // but we handle that in getSchedulerParameters internally.
    /**
     * Gets the scheduler type.
     *
     * @return the type of the scheduler
     * @throws LibvirtException
     */
    public String[] getSchedulerType() throws LibvirtException {
        IntByReference nParams = new IntByReference();
        Pointer pScheduler = libvirt.virDomainGetSchedulerType(VDP, nParams);
        processError();
        String[] array = new String[1];
        array[0] = Library.getString(pScheduler);
        Library.free(pScheduler);
        return array;
    }

    /**
     * Get the UUID for this domain.
     *
     * @return the UUID as an unpacked int array
     * @throws LibvirtException
     * @see <a href="http://www.ietf.org/rfc/rfc4122.txt">rfc4122</a>
     */
    public int[] getUUID() throws LibvirtException {
        byte[] bytes = new byte[Libvirt.VIR_UUID_BUFLEN];
        int success = libvirt.virDomainGetUUID(VDP, bytes);
        processError();
        int[] returnValue = new int[0];
        if (success == 0) {
            returnValue = Connect.convertUUIDBytes(bytes);
        }
        return returnValue;
    }

    /**
     * Gets the UUID for this domain as string.
     *
     * @return the UUID in canonical String format
     * @throws LibvirtException
     * @see <a href="http://www.ietf.org/rfc/rfc4122.txt">rfc4122</a>
     */
    public String getUUIDString() throws LibvirtException {
        byte[] bytes = new byte[Libvirt.VIR_UUID_STRING_BUFLEN];
        int success = libvirt.virDomainGetUUIDString(VDP, bytes);
        processError();
        String returnValue = null;
        if (success == 0) {
            returnValue = Native.toString(bytes);
        }
        return returnValue;
    }

    /**
     * Returns the cpumaps for this domain Only the lower 8 bits of each int in
     * the array contain information.
     *
     * @return a bitmap of real CPUs for all vcpus of this domain
     * @throws LibvirtException
     */
    public int[] getVcpusCpuMaps() throws LibvirtException {
        int[] returnValue = new int[0];
        int cpuCount = getMaxVcpus();

        if (cpuCount > 0) {
            NodeInfo nodeInfo = virConnect.nodeInfo();
            int maplength = cpuMapLength(nodeInfo.maxCpus());
            virVcpuInfo[] infos = new virVcpuInfo[cpuCount];
            returnValue = new int[cpuCount * maplength];
            byte[] cpumaps = new byte[cpuCount * maplength];
            libvirt.virDomainGetVcpus(VDP, infos, cpuCount, cpumaps, maplength);
            processError();
            for (int x = 0; x < cpuCount * maplength; x++) {
                returnValue[x] = cpumaps[x];
            }
        }
        return returnValue;
    }

    /**
     * Extracts information about virtual CPUs of this domain
     *
     * @return an array of VcpuInfo object describing the VCPUs
     * @throws LibvirtException
     */
    public VcpuInfo[] getVcpusInfo() throws LibvirtException {
        int cpuCount = getMaxVcpus();
        VcpuInfo[] returnValue = new VcpuInfo[cpuCount];
        virVcpuInfo[] infos = new virVcpuInfo[cpuCount];
        libvirt.virDomainGetVcpus(VDP, infos, cpuCount, null, 0);
        processError();
        for (int x = 0; x < cpuCount; x++) {
            returnValue[x] = new VcpuInfo(infos[x]);
        }
        return returnValue;
    }

    /**
     * Provides an XML description of the domain. The description may be reused
     * later to relaunch the domain with createLinux().
     *
     * @param flags
     *            not used
     * @return the XML description String
     * @throws LibvirtException
     * @see <a href="http://libvirt.org/format.html#Normal1" >The XML
     *      Description format </a>
     */
    public String getXMLDesc(int flags) throws LibvirtException {
        Pointer ptr = libvirt.virDomainGetXMLDesc(VDP, flags);
        processError();
        try {
            return Library.getString(ptr);
        } finally {
            Library.free(ptr);
        }
    }

    /**
     * Determine if the domain has a snapshot
     *
     * @see <a href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainHasCurrentSnapshot>Libvi
     *      r t Documentation</a>
     * @return 1 if running, 0 if inactive, -1 on error
     * @throws LibvirtException
     */
    public int hasCurrentSnapshot() throws LibvirtException {
        int returnValue = libvirt.virDomainHasCurrentSnapshot(VDP, 0);
        processError();
        return returnValue;
    }

    /**
     * Determine if the domain has a managed save image
     *
     * @see <a href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainHasManagedSaveImage>Libvi
     *      r t Documentation</a>
     * @return 0 if no image is present, 1 if an image is present, and -1 in
     *         case of error
     * @throws LibvirtException
     */
    public int hasManagedSaveImage() throws LibvirtException {
        int returnValue = libvirt.virDomainHasManagedSaveImage(VDP, 0);
        processError();
        return returnValue;
    }

    /**
     * Returns network interface stats for interfaces attached to this domain.
     * The path parameter is the name of the network interface. Domains may have
     * more than network interface. To get stats for each you should make
     * multiple calls to this function. Individual fields within the
     * DomainInterfaceStats object may be returned as -1, which indicates that
     * the hypervisor does not support that particular statistic.
     *
     * @param path
     *            path to the interface
     * @return the statistics in a DomainInterfaceStats object
     * @throws LibvirtException
     */
    public DomainInterfaceStats interfaceStats(String path) throws LibvirtException {
        virDomainInterfaceStats stats = new virDomainInterfaceStats();
        libvirt.virDomainInterfaceStats(VDP, path, stats, stats.size());
        processError();
        return new DomainInterfaceStats(stats);
    }

    /**
     * Determine if the domain is currently running
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainIsActive">Libvirt
     *      Documentation</a>
     * @return 1 if running, 0 if inactive, -1 on error
     * @throws LibvirtException
     */
    public int isActive() throws LibvirtException {
        int returnValue = libvirt.virDomainIsActive(VDP);
        processError();
        return returnValue;
    }

    /**
     * Determine if the domain has a persistent configuration which means it
     * will still exist after shutting down
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainIsPersistent">Libvirt
     *      Documentation</a>
     * @return 1 if persistent, 0 if transient, -1 on error
     * @throws LibvirtException
     */
    public int isPersistent() throws LibvirtException {
        int returnValue = libvirt.virDomainIsPersistent(VDP);
        processError();
        return returnValue;
    }

    /**
     * suspend a domain and save its memory contents to a file on disk.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainManagedSave">Libvirt
     *      Documentation</a>
     * @return 0 in case of success or -1 in case of failure
     * @throws LibvirtException
     */
    public int managedSave() throws LibvirtException {
        int returnValue = libvirt.virDomainManagedSave(VDP, 0);
        processError();
        return returnValue;
    }

    /**
     * Remove any managed save images from the domain
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainManagedSaveRemove">Libvirt
     *      Documentation</a>
     * @return 0 in case of success, and -1 in case of error
     * @throws LibvirtException
     */
    public int managedSaveRemove() throws LibvirtException {
        int returnValue = libvirt.virDomainManagedSaveRemove(VDP, 0);
        processError();
        return returnValue;
    }

    /**
     * This function provides memory statistics for the domain.
     *
     * @param number
     *            the number of stats to retrieve
     * @return the collection of stats, or null if an error occurs.
     * @throws LibvirtException
     */
    public MemoryStatistic[] memoryStats(int number) throws LibvirtException {
        virDomainMemoryStats[] stats = new virDomainMemoryStats[number];
        MemoryStatistic[] returnStats = null;
        int result = libvirt.virDomainMemoryStats(VDP, stats, number, 0);
        processError();
        if (result >= 0) {
            returnStats = new MemoryStatistic[result];
            for (int x = 0; x < result; x++) {
                returnStats[x] = new MemoryStatistic(stats[x]);
            }
        }
        return returnStats;
    }

    /**
     * Migrate this domain object from its current host to the destination host
     * given by dconn (a connection to the destination host).
     * <p>
     * Flags may be bitwise OR'ed values of
     * {@link eu.ascetic.vmc.libvirt.Domain.MigrateFlags MigrateFlags}.
     * <p>
     * If a hypervisor supports renaming domains during migration, then you may
     * set the dname parameter to the new name (otherwise it keeps the same name).
     * <p>
     * If this is not supported by the hypervisor, dname must be {@code null} or
     * else you will get an exception.
     * <p>
     * Since typically the two hypervisors connect directly to each other in order
     * to perform the migration, you may need to specify a path from the source
     * to the destination. This is the purpose of the uri parameter.
     * <p>
     * If uri is {@code null}, then libvirt will try to find the best method.
     * <p>
     * Uri may specify the hostname or IP address of the destination host as seen
     * from the source, or uri may be a URI giving transport, hostname, user,
     * port, etc. in the usual form.
     * <p>
     * Uri should only be specified if you want to migrate over a specific interface
     * on the remote host.
     * <p>
     * For Qemu/KVM, the URI should be of the form {@code "tcp://hostname[:port]"}.
     * <p>
     * This does not require TCP auth to be setup between the connections, since
     * migrate uses a straight TCP connection (unless using the PEER2PEER flag,
     * in which case URI should be a full fledged libvirt URI).
     * <p>
     * Refer also to driver documentation for the particular URIs supported.
     * <p>
     * The maximum bandwidth (in Mbps) that will be used to do
     * migration can be specified with the bandwidth parameter. If
     * set to 0, libvirt will choose a suitable default.
     * <p>
     * Some hypervisors do not support this feature and will return an
     * error if bandwidth is not 0. To see which features are
     * supported by the current hypervisor, see
     * Connect.getCapabilities, /capabilities/host/migration_features.
     * <p>
     * There are many limitations on migration imposed by the underlying technology
     * for example it may not be possible to migrate between different processors
     * even with the same architecture, or between different types of hypervisor.
     * <p>
     * If the hypervisor supports it, dxml can be used to alter
     * host-specific portions of the domain XML that will be used on
     * the destination.
     *
     * @param dconn
     *            destination host (a Connect object)
     * @param dxml
     *            (optional) XML config for launching guest on target
     * @param flags
     *            flags
     * @param dname
     *            (optional) rename domain to this at destination
     * @param uri
     *            (optional) dest hostname/URI as seen from the source host
     * @param bandwidth
     *            (optional) specify migration bandwidth limit in Mbps
     * @return the new domain object if the migration was
     *         successful. Note that the new domain object exists in
     *         the scope of the destination connection (dconn).
     * @throws LibvirtException if the migration fails
     */
    public Domain migrate(Connect dconn, long flags, String dxml, String dname, String uri, long bandwidth) throws LibvirtException {
        DomainPointer newPtr = libvirt.virDomainMigrate2(VDP, dconn.VCP, dxml, new NativeLong(flags), dname, uri, new NativeLong(bandwidth));
        processError();
        return new Domain(dconn, newPtr);
    }

    /**
     * Migrate this domain object from its current host to the destination host
     * given by dconn (a connection to the destination host). Flags may be one
     * of more of the following: Domain.VIR_MIGRATE_LIVE Attempt a live
     * migration. If a hypervisor supports renaming domains during migration,
     * then you may set the dname parameter to the new name (otherwise it keeps
     * the same name). If this is not supported by the hypervisor, dname must be
     * NULL or else you will get an error. Since typically the two hypervisors
     * connect directly to each other in order to perform the migration, you may
     * need to specify a path from the source to the destination. This is the
     * purpose of the uri parameter.If uri is NULL, then libvirt will try to
     * find the best method. Uri may specify the hostname or IP address of the
     * destination host as seen from the source, or uri may be a URI giving
     * transport, hostname, user, port, etc. in the usual form. Uri should only
     * be specified if you want to migrate over a specific interface on the
     * remote host. For Qemu/KVM, the uri should be of the form
     * "tcp://hostname[:port]". This does not require TCP auth to be setup
     * between the connections, since migrate uses a straight TCP connection
     * (unless using the PEER2PEER flag, in which case URI should be a full
     * fledged libvirt URI). Refer also to driver documentation for the
     * particular URIs supported. If set to 0, libvirt will choose a suitable
     * default. Some hypervisors do not support this feature and will return an
     * error if bandwidth is not 0. To see which features are supported by the
     * current hypervisor, see Connect.getCapabilities,
     * /capabilities/host/migration_features. There are many limitations on
     * migration imposed by the underlying technology - for example it may not
     * be possible to migrate between different processors even with the same
     * architecture, or between different types of hypervisor.
     *
     * @param dconn
     *            destination host (a Connect object)
     * @param flags
     *            flags
     * @param dname
     *            (optional) rename domain to this at destination
     * @param uri
     *            (optional) dest hostname/URI as seen from the source host
     * @param bandwidth
     *            optional) specify migration bandwidth limit in Mbps
     * @return the new domain object if the migration was successful, or NULL in
     *         case of error. Note that the new domain object exists in the
     *         scope of the destination connection (dconn).
     * @throws LibvirtException
     */
    public Domain migrate(Connect dconn, long flags, String dname, String uri, long bandwidth) throws LibvirtException {
        DomainPointer newPtr = libvirt.virDomainMigrate(VDP, dconn.VCP, new NativeLong(flags), dname, uri, new NativeLong(bandwidth));
        processError();
        return new Domain(dconn, newPtr);
    }

    /**
     * Sets maximum tolerable time for which the domain is allowed to be paused
     * at the end of live migration.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainMigrateSetMaxDowntime">LIbvirt
     *      Documentation</a>
     * @param downtime
     *            the time to be down
     * @return 0 in case of success, -1 otherwise.
     * @throws LibvirtException
     */
    public int migrateSetMaxDowntime(long downtime) throws LibvirtException {
        int returnValue = libvirt.virDomainMigrateSetMaxDowntime(VDP, downtime, 0);
        processError();
        return returnValue;
    }

    /**
     * Migrate the domain object from its current host to the destination
     * denoted by a given URI.
     * <p>
     * The destination is given either in dconnuri (if the
     * {@link MigrateFlags#VIR_MIGRATE_PEER2PEER PEER2PEER}
     * is flag set), or in miguri (if neither the
     * {@link MigrateFlags#VIR_MIGRATE_PEER2PEER PEER2PEER} nor the
     * {@link MigrateFlags#VIR_MIGRATE_TUNNELLED TUNNELLED} migration
     * flag is set in flags).
     *
     * @see <a
     * href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainMigrateToURI">
     * virDomainMigrateToURI</a>
     *
     * @param dconnuri
     *            (optional) URI for target libvirtd if @flags includes VIR_MIGRATE_PEER2PEER
     * @param miguri
     *            (optional) URI for invoking the migration, not if @flags includs VIR_MIGRATE_TUNNELLED
     * @param dxml
     *            (optional) XML config for launching guest on target
     * @param flags
     *            Controls the migrate
     * @param dname
     *            The name at the destnation
     * @param bandwidth
     *            Specify the migration bandwidth
     * @return 0 if successful
     * @throws LibvirtException
     */
    public int migrateToURI(String dconnuri, String miguri, String dxml, long flags, String dname, long bandwidth) throws LibvirtException {
        int returnValue = libvirt.virDomainMigrateToURI2(VDP, dconnuri, miguri, dxml, new NativeLong(flags), dname, new NativeLong(bandwidth));
        processError();
        return returnValue;
    }

    /**
     * Migrate the domain object from its current host to the destination host
     * given by duri.
     *
     * @see <a
     *       href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainMigrateToURI">
     *       virDomainMigrateToURI</a>
     *
     * @param uri
     *            The destination URI
     * @param flags
     *            Controls the migrate
     * @param dname
     *            The name at the destnation
     * @param bandwidth
     *            Specify the migration bandwidth
     * @return 0 if successful, -1 if not
     * @throws LibvirtException
     */
    public int migrateToURI(String uri, long flags, String dname, long bandwidth) throws LibvirtException {
        int returnValue = libvirt.virDomainMigrateToURI(VDP, uri, new NativeLong(flags), dname, new NativeLong(bandwidth));
        processError();
        return returnValue;
    }

    /**
     * Dynamically changes the real CPUs which can be allocated to a virtual
     * CPU. This function requires priviledged access to the hypervisor.
     *
     * @param vcpu
     *            virtual cpu number
     * @param cpumap
     *            bit map of real CPUs represented by the the lower 8 bits of
     *            each int in the array. Each bit set to 1 means that
     *            corresponding CPU is usable. Bytes are stored in little-endian
     *            order: CPU0-7, 8-15... In each byte, lowest CPU number is
     *            least significant bit.
     * @throws LibvirtException
     */
    public void pinVcpu(int vcpu, int[] cpumap) throws LibvirtException {
        byte[] packedMap = new byte[cpumap.length];
        for (int x = 0; x < cpumap.length; x++) {
            packedMap[x] = (byte) cpumap[x];
        }
        libvirt.virDomainPinVcpu(VDP, vcpu, packedMap, cpumap.length);
        processError();
    }

    /**
     * Error handling logic to throw errors. Must be called after every libvirt
     * call.
     */
    protected void processError() throws LibvirtException {
        virConnect.processError();
    }

    /**
     * Reboot this domain, the domain object is still usable there after but the
     * domain OS is being stopped for a restart. Note that the guest OS may
     * ignore the request.
     *
     * @param flags
     *            extra flags for the reboot operation, not used yet
     * @throws LibvirtException
     */
    public void reboot(int flags) throws LibvirtException {
        libvirt.virDomainReboot(VDP, flags);
        processError();
    }

    /**
     * Resume this suspended domain, the process is restarted from the state
     * where it was frozen by calling virSuspendDomain(). This function may
     * requires privileged access
     *
     * @throws LibvirtException
     */
    public void resume() throws LibvirtException {
        libvirt.virDomainResume(VDP);
        processError();
    }

    /**
     * Adds a callback to receive notifications of IOError domain events
     * occurring on this domain.
     *
     * @see <a
     * href="http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny">Libvirt
     *      Documentation</a>
     * @param cb
     *            the IOErrorCallback instance
     * @return The return value from this method is a positive integer identifier for the callback.
     * @throws LibvirtException on failure
     */
    public int register(final Connect.DomainEvent.IOErrorCallback cb) throws LibvirtException {
        return virConnect.domainEventRegister(this, cb);
    }

    /**
     * Adds a callback to receive notifications of Reboot domain events
     * occurring on this domain.
     *
     * @see <a
     * href="http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny">Libvirt
     *      Documentation</a>
     * @param cb
     *            the RebootCallback instance
     * @return The return value from this method is a positive integer identifier for the callback.
     * @throws LibvirtException on failure
     */
    public int domainEventRegister(final Connect.DomainEvent.RebootCallback cb)
        throws LibvirtException
    {
        return virConnect.domainEventRegister(this, cb);
    }

    /**
     * Adds a callback to receive notifications of domain lifecycle events
     * occurring on this domain.
     *
     * @see <a
     * href="http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny">Libvirt
     *      Documentation</a>
     * @param cb
     *            the LifecycleCallback instance
     * @return The return value from this method is a positive integer identifier for the callback.
     * @throws LibvirtException on failure
     */
    public int register(final Connect.DomainEvent.LifecycleCallback cb) throws LibvirtException
    {
        return virConnect.domainEventRegister(this, cb);
    }

    /**
     * Adds a callback to receive notifications of PMWakeup events
     * occurring on some domain.
     *
     * @see <a
     * href="http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny">Libvirt
     *      Documentation</a>
     * @param cb
     *            the PMWakeupCallback instance
     * @return The return value from this method is a positive integer identifier for the callback.
     * @throws LibvirtException on failure
     */
    public int register(final Connect.DomainEvent.PMWakeupCallback cb) throws LibvirtException
    {
        return virConnect.domainEventRegister(this, cb);
    }

    /**
     * Adds a callback to receive notifications of PMSuspend events
     * occurring on this domain.
     *
     * @see <a
     * href="http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny">Libvirt
     *      Documentation</a>
     * @param cb
     *            the PMSuspendCallback instance
     * @return The return value from this method is a positive integer identifier for the callback.
     * @throws LibvirtException on failure
     */
    public int domainEventRegister(final Connect.DomainEvent.PMSuspendCallback cb) throws LibvirtException
    {
        return virConnect.domainEventRegister(this, cb);
    }

    /**
     * Revert the domain to a given snapshot.
     *
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virDomainRevertToSnapshot"
     *      >Libvirt Documentation</>
     * @param snapshot
     *            the snapshot to revert to
     * @return 0 if the creation is successful, -1 on error.
     * @throws LibvirtException
     */
    public int revertToSnapshot(DomainSnapshot snapshot) throws LibvirtException {
        int returnCode = libvirt.virDomainRevertToSnapshot(snapshot.VDSP, 0);
        processError();
        return returnCode;
    }

    /**
     * Suspends this domain and saves its memory contents to a file on disk.
     * After the call, if successful, the domain is not listed as running
     * anymore (this may be a problem). Use Connect.virDomainRestore() to
     * restore a domain after saving.
     *
     * @param to
     *            path for the output file
     * @throws LibvirtException
     */
    public void save(String to) throws LibvirtException {
        libvirt.virDomainSave(VDP, to);
        processError();
    }

    /**
     * Configures the network to be automatically started when the host machine
     * boots.
     *
     * @param autostart
     * @throws LibvirtException
     */
    public void setAutostart(boolean autostart) throws LibvirtException {
        int autoValue = autostart ? 1 : 0;
        libvirt.virDomainSetAutostart(VDP, autoValue);
        processError();
    }

    /**
     * * Dynamically change the maximum amount of physical memory allocated to a
     * domain. This function requires priviledged access to the hypervisor.
     *
     * @param memory
     *            the amount memory in kilobytes
     * @throws LibvirtException
     */
    public void setMaxMemory(long memory) throws LibvirtException {
        libvirt.virDomainSetMaxMemory(VDP, new NativeLong(memory));
        processError();
    }

    /**
     * Dynamically changes the target amount of physical memory allocated to
     * this domain. This function may requires priviledged access to the
     * hypervisor.
     *
     * @param memory
     *            in kilobytes
     * @throws LibvirtException
     */
    public void setMemory(long memory) throws LibvirtException {
        libvirt.virDomainSetMemory(VDP, new NativeLong(memory));
        processError();
    }

    /**
     * Changes the scheduler parameters
     *
     * @param params
     *            an array of SchedParameter objects to be changed
     * @throws LibvirtException
     */
    public void setSchedulerParameters(SchedParameter[] params) throws LibvirtException {
        virSchedParameter[] input = new virSchedParameter[params.length];
        for (int x = 0; x < params.length; x++) {
            input[x] = SchedParameter.toNative(params[x]);
        }
        libvirt.virDomainSetSchedulerParameters(VDP, input, params.length);
        processError();
    }

    /**
     * Dynamically changes the number of virtual CPUs used by this domain. Note
     * that this call may fail if the underlying virtualization hypervisor does
     * not support it or if growing the number is arbitrary limited. This
     * function requires priviledged access to the hypervisor.
     *
     * @param nvcpus
     *            the new number of virtual CPUs for this domain
     * @throws LibvirtException
     */
    public void setVcpus(int nvcpus) throws LibvirtException {
        libvirt.virDomainSetVcpus(VDP, nvcpus);
        processError();
    }

    /**
     * Shuts down this domain, the domain object is still usable there after but
     * the domain OS is being stopped. Note that the guest OS may ignore the
     * request. TODO: should we add an option for reboot, knowing it may not be
     * doable in the general case ?
     *
     * @throws LibvirtException
     */
    public void shutdown() throws LibvirtException {
        libvirt.virDomainShutdown(VDP);
        processError();
    }

    /**
     * Creates a new snapshot of a domain based on the snapshot xml contained in
     * xmlDesc.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotCreateXML">Libvirt
     *      Documentation</a>
     * @param xmlDesc
     *            string containing an XML description of the domain
     * @param flags
     *            flags for creating the snapshot, see the virDomainSnapshotCreateFlags for the flag options
     * @return the snapshot, or null on Error
     * @throws LibvirtException
     */
    public DomainSnapshot snapshotCreateXML(String xmlDesc, int flags) throws LibvirtException {
        DomainSnapshotPointer ptr = libvirt.virDomainSnapshotCreateXML(VDP, xmlDesc, flags);
        processError();
        DomainSnapshot returnValue = null;
        if (ptr != null) {
            returnValue = new DomainSnapshot(virConnect, ptr);
        }
        return returnValue;
    }

    /**
     * Creates a new snapshot of a domain based on the snapshot xml contained in
     * xmlDesc.
     * <p>
     * This is just a convenience method, it has the same effect
     * as calling {@code snapshotCreateXML(xmlDesc, 0);}.
     *
     * @see #snapshotCreateXML(int)
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotCreateXML">Libvirt
     *      Documentation</a>
     * @param xmlDesc
     *            string containing an XML description of the domain
     * @return the snapshot, or null on Error
     * @throws LibvirtException
     */
    public DomainSnapshot snapshotCreateXML(String xmlDesc) throws LibvirtException {
        return snapshotCreateXML(xmlDesc, 0);
    }

    /**
     * Get the current snapshot for a domain, if any.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotCurrent">Libvirt
     *      Documentation</a>
     * @return the snapshot, or null on Error
     * @throws LibvirtException
     */
    public DomainSnapshot snapshotCurrent() throws LibvirtException {
        DomainSnapshotPointer ptr = libvirt.virDomainSnapshotCurrent(VDP, 0);
        processError();
        DomainSnapshot returnValue = null;
        if (ptr != null) {
            returnValue = new DomainSnapshot(virConnect, ptr);
        }
        return returnValue;
    }

    /**
     * Collect the list of domain snapshots for the given domain. With the option to pass flags
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotListNames">Libvirt
     *      Documentation</a>
     * @return The list of names, or null if an error
     * @throws LibvirtException
     */
    public String[] snapshotListNames(int flags) throws LibvirtException {
        String[] returnValue = null;
        int num = snapshotNum();
        if (num >= 0) {
            returnValue = new String[num];
            if (num > 0) {
                libvirt.virDomainSnapshotListNames(VDP, returnValue, num, flags);
                processError();
            }
        }
        return returnValue;
    }

    /**
     * Collect the list of domain snapshots for the given domain.
     * <p>
     * This is just a convenience method, it has the same effect
     * as calling {@code snapshotListNames(0);}.
     *
     * @see #snapshotListNames(int)
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotListNames">
     *        virDomainSnapshotListNames</a>
     * @return The list of names, or null if an error
     * @throws LibvirtException
     */
    public String[] snapshotListNames() throws LibvirtException {
        return snapshotListNames(0);
    }

    /**
     * Retrieve a snapshot by name
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotLookupByName">Libvirt
     *      Documentation</a>
     * @param name
     *            the name
     * @return The located snapshot, or null if an error
     * @throws LibvirtException
     */
    public DomainSnapshot snapshotLookupByName(String name) throws LibvirtException {
        DomainSnapshotPointer ptr = libvirt.virDomainSnapshotLookupByName(VDP, name, 0);
        processError();
        DomainSnapshot returnValue = null;
        if (ptr != null) {
            returnValue = new DomainSnapshot(virConnect, ptr);
        }
        return returnValue;
    }

    /**
     * Provides the number of domain snapshots for this domain..
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotNum">Libvirt
     *      Documentation</a>
     */
    public int snapshotNum() throws LibvirtException {
        int returnValue = libvirt.virDomainSnapshotNum(VDP, 0);
        processError();
        return returnValue;
    }

    /**
     * Suspends this active domain, the process is frozen without further access
     * to CPU resources and I/O but the memory used by the domain at the
     * hypervisor level will stay allocated. Use Domain.resume() to reactivate
     * the domain. This function requires priviledged access.
     *
     * @throws LibvirtException
     */
    public void suspend() throws LibvirtException {
        libvirt.virDomainSuspend(VDP);
        processError();
    }

    /**
     * undefines this domain but does not stop it if it is running
     *
     * @throws LibvirtException
     */
    public void undefine() throws LibvirtException {
        libvirt.virDomainUndefine(VDP);
        processError();
    }

    /**
     * Undefines this domain but does not stop if it it is running. With option for passing flags
     *
     * @see <a href="http://libvirt.org/html/libvirt-libvirt.html#virDomainUndefineFlags">Libvirt Documentation</a>
     * @param flags
     *            flags for undefining the domain. See virDomainUndefineFlagsValues for more information
     * @throws LibvirtException
    */
    public void undefine(int flags) throws LibvirtException {
        libvirt.virDomainUndefineFlags(VDP, flags);
        processError();
    }

    /**
     * Change a virtual device on a domain
     *
     * @see <a href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainUpdateDeviceFlags">Libvirt Documentation</a>
     * @param xml
     *            the xml to update with
     * @param flags
     *            controls the update
     * @return 0 in case of success, -1 in case of failure.
     * @throws LibvirtException
     */
    public int updateDeviceFlags(String xml, int flags) throws LibvirtException {
        int returnValue = libvirt.virDomainUpdateDeviceFlags(VDP, xml, flags);
        processError();
        return returnValue;
    }

}
