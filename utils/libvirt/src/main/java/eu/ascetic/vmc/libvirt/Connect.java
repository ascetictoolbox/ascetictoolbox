package eu.ascetic.vmc.libvirt;

import java.util.UUID;

import static eu.ascetic.vmc.libvirt.Library.libvirt;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;

import eu.ascetic.vmc.libvirt.jna.ConnectionPointer;
import eu.ascetic.vmc.libvirt.jna.DevicePointer;
import eu.ascetic.vmc.libvirt.jna.DomainPointer;
import eu.ascetic.vmc.libvirt.jna.InterfacePointer;
import eu.ascetic.vmc.libvirt.jna.Libvirt;
import eu.ascetic.vmc.libvirt.jna.NetworkFilterPointer;
import eu.ascetic.vmc.libvirt.jna.NetworkPointer;
import eu.ascetic.vmc.libvirt.jna.SecretPointer;
import eu.ascetic.vmc.libvirt.jna.StoragePoolPointer;
import eu.ascetic.vmc.libvirt.jna.StorageVolPointer;
import eu.ascetic.vmc.libvirt.jna.StreamPointer;
import eu.ascetic.vmc.libvirt.jna.virConnectAuth;
import eu.ascetic.vmc.libvirt.jna.virNodeInfo;

/**
 * The Connect object represents a connection to a local or remote
 * hypervisor/driver.
 *
 * @author stoty
 */
public class Connect {

    /**
     * Event IDs.
     */
    protected static final class DomainEventID {
        static final int LIFECYCLE = 0;
        static final int REBOOT = 1;
        static final int RTC_CHANGE = 2;
        static final int WATCHDOG = 3;
        static final int IO_ERROR = 4;
        static final int GRAPHICS = 5;
        static final int IO_ERROR_REASON = 6;
        static final int CONTROL_ERROR = 7;
        static final int BLOCK_JOB = 8;
        static final int DISK_CHANGE = 9;
        static final int TRAY_CHANGE = 10;
        static final int PMWAKEUP = 11;
        static final int PMSUSPEND = 12;
    }

    public static abstract class DomainEvent {
        /* Event Callbacks */

        interface IOErrorCallback {
            final int eventID = DomainEventID.IO_ERROR;

            void onIOError(Connect connect, Domain domain, String srcPath,
                    String devAlias, int action);
        }

        public interface RebootCallback {
            final int eventID = DomainEventID.REBOOT;

            void onReboot(Connect connect, Domain domain);
        }

        /**
         * @see <a href=
         *      "http://libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventCallback">
         *      virConnectDomainEventCallback</a>
         */
        public interface LifecycleCallback {
            public static enum Event {
                DEFINED, UNDEFINED, STARTED, SUSPENDED, RESUMED, STOPPED, SHUTDOWN;
            }

            final int eventID = DomainEventID.LIFECYCLE;

            void onLifecycleChange(Connect connect, Domain domain, Event event,
                    int detail);
        }

        public interface PMWakeupCallback {
            final int eventID = DomainEventID.PMWAKEUP;

            void onPMWakeup(Connect connect, Domain domain, int reason);
        }

        public interface PMSuspendCallback {
            final int eventID = DomainEventID.PMSUSPEND;

            void onPMSuspend(Connect connect, Domain domain, int reason);
        }
    }

    /**
     * Get the version of a connection.
     *
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virConnectGetLibVersion">
     *      Libvirt Documentation</a>
     * @param conn
     *            the connection to use.
     * @return -1 in case of failure, versions have the format major * 1,000,000
     *         + minor * 1,000 + release.
     */
    public static long connectionVersion(Connect conn) {
        LongByReference libVer = new LongByReference();
        int result = Libvirt.INSTANCE.virConnectGetLibVersion(conn.VCP, libVer);
        return result != -1 ? libVer.getValue() : -1;
    }

    /**
     * Helper function to convert bytes into ints for the UUID calls
     */
    public static int[] convertUUIDBytes(byte bytes[]) {
        int[] returnValue = new int[Libvirt.VIR_UUID_BUFLEN];
        for (int x = 0; x < Libvirt.VIR_UUID_BUFLEN; x++) {
            // For some reason, the higher bytes come back wierd.
            // We only want the lower 2 bytes.
            returnValue[x] = (bytes[x] & 255);
        }
        return returnValue;
    }

    /**
     * Helper function to convert UUIDs into a stirng for the UUID calls
     */
    public static byte[] createUUIDBytes(int[] UUID) {
        byte[] bytes = new byte[Libvirt.VIR_UUID_BUFLEN];
        for (int x = 0; x < Libvirt.VIR_UUID_BUFLEN; x++) {
            bytes[x] = (byte) UUID[x];
        }
        return bytes;
    }

    /**
     * Sets the error function to a user defined callback
     *
     * @param callback
     *            a Class to perform the callback
     */
    public static void setErrorCallback(Libvirt.VirErrorCallback callback)
            throws LibvirtException {
        Libvirt.INSTANCE.virSetErrorFunc(null, callback);
        ErrorHandler.processError(Libvirt.INSTANCE);
    }

    /**
     * The native virConnectPtr.
     */
    protected ConnectionPointer VCP;

    /**
     * Protected constructor to return a Connection with ConnectionPointer
     */
    Connect(ConnectionPointer ptr) {
        VCP = ptr;
    }

    /**
     * Construct a Connect object from a known native virConnectPtr For use when
     * native libvirt returns a virConnectPtr, i.e. error handling.
     *
     * @param VCP
     *            the virConnectPtr pointing to an existing native virConnect
     *            structure
     */
    @Deprecated
    Connect(long VCP) {
        throw new RuntimeException("No longer supported");
    }

    /**
     * Constructs a read-write Connect object from the supplied URI.
     *
     * @param uri
     *            The connection URI
     * @throws LibvirtException
     * @see <a href="http://libvirt.org/uri.html">The URI documentation</a>
     */
    public Connect(String uri) throws LibvirtException {
        VCP = libvirt.virConnectOpen(uri);
        // Check for an error
        processError(VCP);
        ErrorHandler.processError(Libvirt.INSTANCE);
    }

    /**
     * Constructs a Connect object from the supplied URI.
     *
     * @param uri
     *            The connection URI
     * @param readOnly
     *            Whether the connection is read-only
     * @throws LibvirtException
     * @see <a href="http://libvirt.org/uri.html">The URI documentation</a>
     */
    public Connect(String uri, boolean readOnly) throws LibvirtException {
        if (readOnly) {
            VCP = libvirt.virConnectOpenReadOnly(uri);
        } else {
            VCP = libvirt.virConnectOpen(uri);
        }
        // Check for an error
        processError(VCP);
        ErrorHandler.processError(Libvirt.INSTANCE);
    }

    /**
     * Constructs a Connect object from the supplied URI, using the supplied
     * authentication callback
     *
     * @param uri
     *            The connection URI
     * @param auth
     *            a ConnectAuth object
     * @param flags
     * @throws LibvirtException
     * @see <a href="http://libvirt.org/uri.html">The URI documentation</a>
     */
    public Connect(String uri, ConnectAuth auth, int flags)
            throws LibvirtException {
        virConnectAuth vAuth = new virConnectAuth();
        vAuth.cb = auth;
        vAuth.cbdata = null;
        vAuth.ncredtype = auth.credType.length;
        int[] authInts = new int[vAuth.ncredtype];

        for (int x = 0; x < vAuth.ncredtype; x++) {
            authInts[x] = auth.credType[x].mapToInt();
        }

        Memory mem = new Memory(4 * vAuth.ncredtype);
        mem.write(0, authInts, 0, vAuth.ncredtype);
        vAuth.credtype = mem.share(0);

        VCP = libvirt.virConnectOpenAuth(uri, vAuth, flags);
        // Check for an error
        processError(VCP);
        ErrorHandler.processError(Libvirt.INSTANCE);
    }

    /**
     * Computes the most feature-rich CPU which is compatible with all given
     * host CPUs.
     *
     * @param xmlCPUs
     *            array of XML descriptions of host CPUs
     * @return XML description of the computed CPU or NULL on error.
     * @throws LibvirtException
     */
    public String baselineCPU(String[] xmlCPUs) throws LibvirtException {
        return processError(
                libvirt.virConnectBaselineCPU(VCP, xmlCPUs, xmlCPUs.length, 0));
    }

    /**
     * Closes the connection to the hypervisor/driver. Calling any methods on
     * the object after close() will result in an exception.
     *
     * @throws LibvirtException
     * @return number of remaining references (>= 0)
     */
    public int close() throws LibvirtException {
        int success = 0;
        if (VCP != null) {
            success = libvirt.virConnectClose(VCP);
            // If leave an invalid pointer dangling around JVM crashes and burns
            // if someone tries to call a method on us
            // We rely on the underlying libvirt error handling to detect that
            // it's called with a null virConnectPointer
            VCP = null;
        }
        return processError(success);
    }

    /**
     * Compares the given CPU description with the host CPU
     *
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virConnectCompareCPU">
     *      Libvirt Documentation</a>
     * @param xmlDesc
     * @return comparison result according to enum CPUCompareResult
     * @throws LibvirtException
     */
    public CPUCompareResult compareCPU(String xmlDesc) throws LibvirtException {
        int rawResult = libvirt.virConnectCompareCPU(VCP, xmlDesc, 0);
        processError();
        return CPUCompareResult.get(rawResult);
    }

    /**
     * Create a new device on the VM host machine, for example, virtual HBAs
     * created using vport_create.
     *
     * @param xmlDesc
     *            the device to create
     * @return the Device object
     * @throws LibvirtException
     */
    public Device deviceCreateXML(String xmlDesc) throws LibvirtException {
        DevicePointer ptr = processError(
                libvirt.virNodeDeviceCreateXML(VCP, xmlDesc, 0));
        return new Device(this, ptr);
    }

    /**
     * Fetch a device based on its unique name
     *
     * @param name
     *            name of device to fetch
     * @return Device object
     * @throws LibvirtException
     */
    public Device deviceLookupByName(String name) throws LibvirtException {
        DevicePointer ptr = processError(
                libvirt.virNodeDeviceLookupByName(VCP, name));
        return new Device(this, ptr);
    }

    /**
     * Launches a new Linux guest domain. The domain is based on an XML
     * description similar to the one returned by virDomainGetXMLDesc(). This
     * function may require priviledged access to the hypervisor.
     *
     * @param xmlDesc
     *            the Domain description in XML
     * @param flags
     *            an optional set of flags (unused)
     * @return the Domain object
     * @throws LibvirtException
     * @see <a href="http://libvirt.org/format.html#Normal1" > The XML format
     *      description </a>
     */
    public Domain domainCreateLinux(String xmlDesc, int flags)
            throws LibvirtException {
        DomainPointer ptr = processError(
                libvirt.virDomainCreateLinux(VCP, xmlDesc, flags));

        return new Domain(this, ptr);
    }

    /**
     * Launch a new guest domain, based on an XML description
     *
     * @param xmlDesc
     * @return the Domain object
     * @throws LibvirtException
     * @see <a href="http://libvirt.org/format.html#Normal1" > The XML format
     *      description </a>
     */
    public Domain domainCreateXML(String xmlDesc, int flags)
            throws LibvirtException {
        DomainPointer ptr = processError(
                libvirt.virDomainCreateXML(VCP, xmlDesc, flags));
        return new Domain(this, ptr);
    }

    /**
     * Defines a domain, but does not start it
     *
     * @param xmlDesc
     * @return the Domain object
     * @throws LibvirtException
     * @see <a href="http://libvirt.org/format.html#Normal1" > The XML format
     *      description </a>
     */
    public Domain domainDefineXML(String xmlDesc) throws LibvirtException {
        DomainPointer ptr = processError(
                libvirt.virDomainDefineXML(VCP, xmlDesc));
        return new Domain(this, ptr);
    }

    /**
     * Removes an event callback.
     *
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventDeregisterAny">
     *      Libvirt Documentation</a>
     * @param callbackID
     *            the callback identifier
     * @throws LibvirtException
     */
    public void domainEventDeregister(int callbackID) throws LibvirtException {
        processError(
                libvirt.virConnectDomainEventDeregisterAny(VCP, callbackID));
    }

    /**
     * Registers a default event implementation based on the poll() system call.
     * <p>
     * Once registered, the application has to invoke {@link #processEvent} in a
     * loop to process events.
     * <p>
     * Note: You must call this function before connecting to the hypervisor.
     *
     * @see #processEvent
     */
    public static void initEventLoop() throws LibvirtException {
        if (libvirt.virEventRegisterDefaultImpl() == -1) {
            System.out.println("IT ACTUALLY FAILED");
            ErrorHandler.processError(libvirt);
        }

        // System.out.println("YES IT WORKED");
    }

    /**
     * Run one iteration of the event loop.
     * <p>
     * Applications will generally want to have a thread which invokes this
     * method in an infinite loop:
     * 
     * <pre>
     * {@code while (true) connection.processEvent(); }
     * </pre>
     * <p>
     * Failure to do so may result in connections being closed unexpectedly as a
     * result of keepalive timeout.
     *
     * @see #initEventLoop()
     */
    public void processEvent() throws LibvirtException {
        if (libvirt.virEventRunDefaultImpl() == -1)
            ErrorHandler.processError(Libvirt.INSTANCE);
    }

    int domainEventRegister(Domain domain, int eventID,
            Libvirt.VirDomainEventCallback cb) throws LibvirtException {
        DomainPointer ptr = domain == null ? null : domain.VDP;

        return processError(libvirt.virConnectDomainEventRegisterAny(VCP, ptr,
                eventID, cb, null, null));
    }

    int domainEventRegister(Domain domain, final DomainEvent.IOErrorCallback cb)
            throws LibvirtException {
        if (cb == null)
            throw new IllegalArgumentException(
                    "IOError callback cannot be null");

        Libvirt.VirConnectDomainEventIOErrorCallback virCB = new Libvirt.VirConnectDomainEventIOErrorCallback() {
            @Override
            public void eventCallback(ConnectionPointer virConnectPtr,
                    DomainPointer virDomainPointer, String srcPath,
                    String devAlias, int action, com.sun.jna.Pointer opaque) {
                assert (VCP.equals(virConnectPtr));

                Domain d = new Domain(Connect.this, virDomainPointer);
                cb.onIOError(Connect.this, d, srcPath, devAlias, action);
            }
        };

        return domainEventRegister(domain, DomainEvent.IOErrorCallback.eventID,
                virCB);
    }

    /**
     * Adds a callback to receive notifications of IOError domain events
     * occurring on a domain.
     *
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny">
     *      Libvirt Documentation</a>
     * @param cb
     *            the IOErrorCallback instance
     * @return The return value from this method is a positive integer
     *         identifier for the callback.
     * @throws LibvirtException
     *             on failure
     */
    public int domainEventRegister(final DomainEvent.IOErrorCallback cb)
            throws LibvirtException {
        return domainEventRegister(null, cb);
    }

    int domainEventRegister(Domain domain, final DomainEvent.RebootCallback cb)
            throws LibvirtException {
        if (cb == null)
            throw new IllegalArgumentException("RebootCallback cannot be null");

        Libvirt.VirConnectDomainEventGenericCallback virCB = new Libvirt.VirConnectDomainEventGenericCallback() {
            @Override
            public void eventCallback(ConnectionPointer virConnectPtr,
                    DomainPointer virDomainPointer,
                    com.sun.jna.Pointer opaque) {
                assert (VCP.equals(virConnectPtr));
                Domain d = new Domain(Connect.this, virDomainPointer);
                cb.onReboot(Connect.this, d);
            }
        };

        return domainEventRegister(domain, DomainEvent.IOErrorCallback.eventID,
                virCB);
    }

    int domainEventRegister(Domain domain,
            final DomainEvent.LifecycleCallback cb) throws LibvirtException {
        if (cb == null)
            throw new IllegalArgumentException(
                    "LifecycleCallback cannot be null");

        final DomainEvent.LifecycleCallback.Event events[] = DomainEvent.LifecycleCallback.Event
                .values();

        System.out.println("INSIDE DOMAINEVENTREGISTER");

        Libvirt.VirConnectDomainEventCallback virCB = new Libvirt.VirConnectDomainEventCallback() {
            @Override
            public int eventCallback(ConnectionPointer virConnectPtr,
                    DomainPointer virDomainPointer, int event, int detail,
                    com.sun.jna.Pointer opaque) {
                assert (VCP.equals(virConnectPtr));

                System.out.println("INSIDE EVENTCALLBACK");
                System.out.println("event: " + event + " detail:" + detail);

                if (0 <= event && event < events.length) {
                    Domain d = new Domain(Connect.this, virDomainPointer);
                    cb.onLifecycleChange(Connect.this, d, events[event],
                            detail);
                } else {
                    // TODO: throw an exception?
                }
                return 0;
            }
        };

        return domainEventRegister(domain, DomainEvent.IOErrorCallback.eventID,
                virCB);
    }

    /**
     * Adds a callback to receive notifications of domain lifecycle events
     * occurring on some domain.
     *
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny">
     *      Libvirt Documentation</a>
     * @param cb
     *            the LifecycleCallback instance
     * @return The return value from this method is a positive integer
     *         identifier for the callback.
     * @throws LibvirtException
     *             on failure
     */
    public int domainEventRegister(final DomainEvent.LifecycleCallback cb)
            throws LibvirtException {
        return domainEventRegister(null, cb);
    }

    int domainEventRegister(Domain domain,
            final DomainEvent.PMWakeupCallback cb) throws LibvirtException {
        if (cb == null)
            throw new IllegalArgumentException(
                    "PMWakeupCallback cannot be null");

        Libvirt.VirDomainEventCallback virCB = new Libvirt.VirConnectDomainEventPMChangeCallback() {
            @Override
            public void eventCallback(ConnectionPointer virConnectPtr,
                    DomainPointer virDomainPointer, int reason,
                    com.sun.jna.Pointer opaque) {
                assert (VCP.equals(virConnectPtr));
                Domain d = new Domain(Connect.this, virDomainPointer);
                cb.onPMWakeup(Connect.this, d, reason);
            }
        };

        return domainEventRegister(domain, DomainEvent.IOErrorCallback.eventID,
                virCB);
    }

    int domainEventRegister(Domain domain,
            final DomainEvent.PMSuspendCallback cb) throws LibvirtException {
        if (cb == null)
            throw new IllegalArgumentException(
                    "PMSuspendCallback cannot be null");

        Libvirt.VirDomainEventCallback virCB = new Libvirt.VirConnectDomainEventPMChangeCallback() {
            @Override
            public void eventCallback(ConnectionPointer virConnectPtr,
                    DomainPointer virDomainPointer, int reason,
                    com.sun.jna.Pointer opaque) {
                assert (VCP.equals(virConnectPtr));
                Domain d = new Domain(Connect.this, virDomainPointer);
                cb.onPMSuspend(Connect.this, d, reason);
            }
        };

        return domainEventRegister(domain, DomainEvent.IOErrorCallback.eventID,
                virCB);
    }

    /**
     * Adds a callback to receive notifications of PMSuspend events occurring on
     * some domain.
     *
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny">
     *      Libvirt Documentation</a>
     * @param cb
     *            the PMSuspendCallback instance
     * @return The return value from this method is a positive integer
     *         identifier for the callback.
     * @throws LibvirtException
     *             on failure
     */
    public int domainEventRegister(final DomainEvent.PMSuspendCallback cb)
            throws LibvirtException {
        return domainEventRegister(null, cb);
    }

    /**
     * Adds a callback to receive notifications of PMWakeup events occurring on
     * some domain.
     *
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny">
     *      Libvirt Documentation</a>
     * @param cb
     *            the PMWakeupCallback instance
     * @return The return value from this method is a positive integer
     *         identifier for the callback.
     * @throws LibvirtException
     *             on failure
     */
    public int domainEventRegister(final DomainEvent.PMWakeupCallback cb)
            throws LibvirtException {
        return domainEventRegister(null, cb);
    }

    /**
     * Adds a callback to receive notifications of Reboot domain events
     * occurring on an arbitrary domain.
     *
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny">
     *      Libvirt Documentation</a>
     * @param cb
     *            the RebootCallback instance
     * @return The return value from this method is a positive integer
     *         identifier for the callback.
     * @throws LibvirtException
     *             on failure
     */
    public int domainEventRegister(final DomainEvent.RebootCallback cb)
            throws LibvirtException {
        return domainEventRegister(null, cb);
    }

    /**
     * Finds a domain based on the hypervisor ID number.
     *
     * @param id
     *            the hypervisor id
     * @return the Domain object
     * @throws LibvirtException
     */
    public Domain domainLookupByID(int id) throws LibvirtException {
        DomainPointer ptr = processError(libvirt.virDomainLookupByID(VCP, id));
        return new Domain(this, ptr);
    }

    /**
     * Looks up a domain based on its name.
     *
     * @param name
     *            the name of the domain
     * @return the Domain object
     * @throws LibvirtException
     */
    public Domain domainLookupByName(String name) throws LibvirtException {
        DomainPointer ptr = processError(
                libvirt.virDomainLookupByName(VCP, name));
        return new Domain(this, ptr);
    }

    /**
     * Looks up a domain based on its UUID in array form. The UUID Array
     * contains an unpacked representation of the UUID, each int contains only
     * one byte.
     *
     * @param UUID
     *            the UUID as an unpacked int array
     * @return the Domain object
     * @throws LibvirtException
     */
    public Domain domainLookupByUUID(int[] UUID) throws LibvirtException {
        byte[] uuidBytes = Connect.createUUIDBytes(UUID);
        DomainPointer ptr = processError(
                libvirt.virDomainLookupByUUID(VCP, uuidBytes));
        return new Domain(this, ptr);
    }

    /**
     * Fetch a domain based on its globally unique id
     *
     * @param uuid
     *            a java UUID
     * @return a new domain object
     * @throws LibvirtException
     */
    public Domain domainLookupByUUID(UUID uuid) throws LibvirtException {
        return domainLookupByUUIDString(uuid.toString());
    }

    /**
     * Looks up a domain based on its UUID in String form.
     *
     * @param UUID
     *            the UUID in canonical String representation
     * @return the Domain object
     * @throws LibvirtException
     */
    public Domain domainLookupByUUIDString(String UUID)
            throws LibvirtException {
        DomainPointer ptr = processError(
                libvirt.virDomainLookupByUUIDString(VCP, UUID));
        return new Domain(this, ptr);
    }

    /**
     * Reads a native XML configuration document, and generates generates a
     * domain configuration file describing the domain. The format of the native
     * data is hypervisor dependant.
     *
     * @return domain XML as String, or {@code null} on error
     * @throws LibvirtException
     */
    public String domainXMLFromNative(String nativeFormat, String nativeConfig,
            int flags) throws LibvirtException {
        return processError(libvirt.virConnectDomainXMLFromNative(VCP,
                nativeFormat, nativeConfig, 0));
    }

    /**
     * Reads a domain XML configuration document, and generates generates a
     * native configuration file describing the domain. The format of the native
     * data is hypervisor dependant.
     *
     * @return domain XML as String, or {@code null} on error
     * @throws LibvirtException
     */
    public String domainXMLToNative(String nativeFormat, String domainXML,
            int flags) throws LibvirtException {
        String returnValue = libvirt.virConnectDomainXMLToNative(VCP,
                nativeFormat, domainXML, 0);
        return processError(returnValue);
    }

    @Override
    protected void finalize() throws LibvirtException {
        close();
    }

    /**
     * Talks to a storage backend and attempts to auto-discover the set of
     * available storage pool sources. e.g. For iSCSI this would be a set of
     * iSCSI targets. For NFS this would be a list of exported paths. The
     * srcSpec (optional for some storage pool types, e.g. local ones) is an
     * instance of the storage pool&apos;s source element specifying where to
     * look for the pools. srcSpec is not required for some types (e.g., those
     * querying local storage resources only)
     *
     * @param type
     *            type of storage pool to discover
     * @param srcSpecs
     *            XML document specifying discovery sourc
     * @param flags
     *            unused
     * @return an xml document consisting of a SourceList element containing a
     *         source document appropriate to the given pool type for each
     *         discovered source.
     * @throws LibvirtException
     */
    public String findStoragePoolSources(String type, String srcSpecs,
            int flags) throws LibvirtException {
        String returnValue = libvirt.virConnectFindStoragePoolSources(VCP, type,
                srcSpecs, flags);
        return processError(returnValue);
    }

    /**
     * Provides capabilities of the hypervisor / driver.
     *
     * @return an XML String describing the capabilities.
     * @throws LibvirtException
     * @see <a href="http://libvirt.org/format.html#Capa1" >The XML format
     *      description</a>
     */
    public String getCapabilities() throws LibvirtException {
        Pointer ptr = processError(libvirt.virConnectGetCapabilities(VCP));
        try {
            return Library.getString(ptr);
        } finally {
            Library.free(ptr);
        }
    }

    /**
     * NUMA Support
     */
    public long getCellsFreeMemory(int startCells, int maxCells)
            throws LibvirtException {
        LongByReference returnValue = new LongByReference();
        processError(libvirt.virNodeGetCellsFreeMemory(VCP, returnValue,
                startCells, maxCells));
        return returnValue.getValue();
    }

    /**
     * Returns the free memory for the connection
     */
    public long getFreeMemory() throws LibvirtException {
        long returnValue = 0;
        returnValue = libvirt.virNodeGetFreeMemory(VCP);
        if (returnValue == 0)
            processError();
        return returnValue;
    }

    /**
     * Returns the system hostname on which the hypervisor is running. (the
     * result of the gethostname(2) system call) If we are connected to a remote
     * system, then this returns the hostname of the remote system.
     *
     * @return the hostname
     * @throws LibvirtException
     */
    public String getHostName() throws LibvirtException {
        Pointer ptr = processError(libvirt.virConnectGetHostname(VCP));
        try {
            return Library.getString(ptr);
        } finally {
            Library.free(ptr);
        }
    }

    /**
     * Returns the version of the hypervisor against which the library was
     * compiled. The type parameter specified which hypervisor's version is
     * returned
     *
     * @param type
     * @return major * 1,000,000 + minor * 1,000 + release
     * @throws LibvirtException
     */
    public long getHypervisorVersion(String type) throws LibvirtException {
        LongByReference libVer = new LongByReference();
        LongByReference typeVer = new LongByReference();
        processError(libvirt.virGetVersion(libVer, type, typeVer));
        return libVer.getValue();
    }

    /**
     * Gets the version of the native libvirt library that the JNI part is
     * linked to.
     *
     * @return major * 1,000,000 + minor * 1,000 + release
     * @throws LibvirtException
     */
    public long getLibVirVersion() throws LibvirtException {
        LongByReference libVer = new LongByReference();
        LongByReference typeVer = new LongByReference();
        processError(libvirt.virGetVersion(libVer, null, typeVer));
        return libVer.getValue();
    }

    /**
     * Provides the maximum number of virtual CPUs supported for a guest VM of a
     * specific type. The 'type' parameter here corresponds to the 'type'
     * attribute in the <domain> element of the XML.
     *
     * @param type
     * @return the number of CPUs
     * @throws LibvirtException
     */
    public int getMaxVcpus(String type) throws LibvirtException {
        return processError(libvirt.virConnectGetMaxVcpus(VCP, type));
    }

    /**
     * Gets the name of the Hypervisor software used.
     *
     * @return the name
     * @throws LibvirtException
     */
    public String getType() throws LibvirtException {
        return processError(libvirt.virConnectGetType(VCP));
    }

    /**
     * Returns the URI (name) of the hypervisor connection. Normally this is the
     * same as or similar to the string passed to the
     * virConnectOpen/virConnectOpenReadOnly call, but the driver may make the
     * URI canonical.
     *
     * @return the URI
     * @throws LibvirtException
     */
    public String getURI() throws LibvirtException {
        return processError(libvirt.virConnectGetURI(VCP));
    }

    /**
     * Gets the version level of the Hypervisor running. This may work only with
     * hypervisor call, i.e. with priviledged access to the hypervisor, not with
     * a Read-Only connection. If the version can't be extracted by lack of
     * capacities returns 0.
     *
     * @return major * 1,000,000 + minor * 1,000 + release
     * @throws LibvirtException
     */
    public long getVersion() throws LibvirtException {
        LongByReference hvVer = new LongByReference();
        processError(libvirt.virConnectGetVersion(VCP, hvVer));
        return hvVer.getValue();
    }

    /**
     * Define an interface (or modify existing interface configuration)
     *
     * @param xmlDesc
     *            the interface to create
     * @return the Interface object
     * @throws LibvirtException
     */
    public Interface interfaceDefineXML(String xmlDesc)
            throws LibvirtException {
        InterfacePointer ptr = processError(
                libvirt.virInterfaceDefineXML(VCP, xmlDesc, 0));
        return new Interface(this, ptr);
    }

    /**
     * Try to lookup an interface on the given hypervisor based on its MAC.
     *
     * @throws LibvirtException
     */
    public Interface interfaceLookupByMACString(String mac)
            throws LibvirtException {
        InterfacePointer ptr = processError(
                libvirt.virInterfaceLookupByMACString(VCP, mac));
        return new Interface(this, ptr);
    }

    /**
     * Try to lookup an interface on the given hypervisor based on its name.
     *
     * @throws LibvirtException
     */
    public Interface interfaceLookupByName(String name)
            throws LibvirtException {
        InterfacePointer ptr = processError(
                libvirt.virInterfaceLookupByName(VCP, name));
        return new Interface(this, ptr);
    }

    /**
     * Determine if the connection is encrypted
     *
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virConnectIsEncrypted">
     *      Libvirt Documentation</a>
     * @return 1 if encrypted, 0 if not encrypted, -1 on error
     * @throws LibvirtException
     */
    public int isEncrypted() throws LibvirtException {
        int returnValue = libvirt.virConnectIsEncrypted(VCP);
        processError();
        return returnValue;
    }

    /**
     * Determine if the connection is secure
     *
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virConnectIsSecure">
     *      Libvirt Documentation</a>
     * @return 1 if secure, 0 if not secure, -1 on error
     * @throws LibvirtException
     */
    public int isSecure() throws LibvirtException {
        int returnValue = libvirt.virConnectIsSecure(VCP);
        processError();
        return returnValue;
    }

    /**
     * Lists the names of the defined but inactive domains
     *
     * @return an Array of Strings that contains the names of the defined
     *         domains currently inactive
     * @throws LibvirtException
     */
    public String[] listDefinedDomains() throws LibvirtException {
        int maxnames = numOfDefinedDomains();
        if (maxnames > 0) {
            final Pointer[] names = new Pointer[maxnames];
            final int n = processError(
                    libvirt.virConnectListDefinedDomains(VCP, names, maxnames));
            return Library.toStringArray(names, n);
        } else {
            return Library.NO_STRINGS;
        }
    }

    /**
     * Provides the list of names of defined interfaces on this host
     *
     * @return an Array of Strings that contains the names of the interfaces on
     *         this host
     * @throws LibvirtException
     */
    public String[] listDefinedInterfaces() throws LibvirtException {
        final int max = numOfDefinedInterfaces();
        if (max > 0) {
            final Pointer[] ifs = new Pointer[max];
            final int n = processError(
                    libvirt.virConnectListDefinedInterfaces(VCP, ifs, max));
            return Library.toStringArray(ifs, n);
        } else {
            return Library.NO_STRINGS;
        }
    }

    /**
     * Lists the inactive networks
     *
     * @return an Array of Strings that contains the names of the inactive
     *         networks
     * @throws LibvirtException
     */
    public String[] listDefinedNetworks() throws LibvirtException {
        int maxnames = numOfDefinedNetworks();
        if (maxnames > 0) {
            final Pointer[] names = new Pointer[maxnames];
            final int n = processError(libvirt
                    .virConnectListDefinedNetworks(VCP, names, maxnames));
            return Library.toStringArray(names, n);
        } else {
            return Library.NO_STRINGS;
        }
    }

    /**
     * Provides the list of names of inactive storage pools.
     *
     * @return an Array of Strings that contains the names of the defined
     *         storage pools
     * @throws LibvirtException
     */
    public String[] listDefinedStoragePools() throws LibvirtException {
        int num = numOfDefinedStoragePools();
        if (num > 0) {
            Pointer[] pools = new Pointer[num];
            final int n = processError(
                    libvirt.virConnectListDefinedStoragePools(VCP, pools, num));
            return Library.toStringArray(pools, n);
        } else {
            return Library.NO_STRINGS;
        }
    }

    /**
     * List the names of the devices on this node
     *
     * @param capabilityName
     *            optional capability name
     */
    public String[] listDevices(String capabilityName) throws LibvirtException {
        int maxDevices = numOfDevices(capabilityName);
        if (maxDevices > 0) {
            Pointer[] names = new Pointer[maxDevices];
            final int n = processError(libvirt.virNodeListDevices(VCP,
                    capabilityName, names, maxDevices, 0));
            return Library.toStringArray(names, n);
        } else {
            return Library.NO_STRINGS;
        }
    }

    /**
     * Lists the active domains.
     *
     * @return and array of the IDs of the active domains
     * @throws LibvirtException
     */
    public int[] listDomains() throws LibvirtException {
        int maxids = numOfDomains();
        int[] ids = new int[maxids];

        if (maxids > 0) {
            processError(libvirt.virConnectListDomains(VCP, ids, maxids));
        }
        return ids;
    }

    /**
     * Provides the list of names of interfaces on this host
     *
     * @return an Array of Strings that contains the names of the interfaces on
     *         this host
     * @throws LibvirtException
     */
    public String[] listInterfaces() throws LibvirtException {
        int num = numOfInterfaces();
        if (num > 0) {
            Pointer[] ifs = new Pointer[num];
            final int n = processError(
                    libvirt.virConnectListInterfaces(VCP, ifs, num));
            return Library.toStringArray(ifs, n);
        } else {
            return Library.NO_STRINGS;
        }
    }

    /**
     * Lists the names of the network filters
     *
     * @return an Array of Strings that contains the names network filters
     * @throws LibvirtException
     */
    public String[] listNetworkFilters() throws LibvirtException {
        int maxnames = numOfNetworkFilters();
        if (maxnames > 0) {
            Pointer[] names = new Pointer[maxnames];
            final int n = processError(
                    libvirt.virConnectListNWFilters(VCP, names, maxnames));
            return Library.toStringArray(names, n);
        } else {
            return Library.NO_STRINGS;
        }
    }

    /**
     * Lists the active networks.
     *
     * @return an Array of Strings that contains the names of the active
     *         networks
     * @throws LibvirtException
     */
    public String[] listNetworks() throws LibvirtException {
        int maxnames = numOfNetworks();
        if (maxnames > 0) {
            Pointer[] names = new Pointer[maxnames];
            final int n = processError(
                    libvirt.virConnectListNetworks(VCP, names, maxnames));
            return Library.toStringArray(names, n);
        } else {
            return Library.NO_STRINGS;
        }
    }

    /**
     * Retrieve the List UUIDs of defined secrets
     *
     * @return an Array of Strings that contains the uuids of the defined
     *         secrets
     */
    public String[] listSecrets() throws LibvirtException {
        int num = numOfSecrets();
        if (num > 0) {
            Pointer[] returnValue = new Pointer[num];
            final int n = processError(
                    libvirt.virConnectListSecrets(VCP, returnValue, num));
            return Library.toStringArray(returnValue, n);
        } else {
            return Library.NO_STRINGS;
        }
    }

    /**
     * Provides the list of names of active storage pools.
     *
     * @return an Array of Strings that contains the names of the defined
     *         storage pools
     * @throws LibvirtException
     */
    public String[] listStoragePools() throws LibvirtException {
        int num = numOfStoragePools();
        if (num > 0) {
            Pointer[] returnValue = new Pointer[num];
            final int n = processError(
                    libvirt.virConnectListStoragePools(VCP, returnValue, num));
            return Library.toStringArray(returnValue, n);
        } else {
            return Library.NO_STRINGS;
        }
    }

    /**
     * Creates and starts a new virtual network. The properties of the network
     * are based on an XML description similar to the one returned by
     * virNetworkGetXMLDesc()
     *
     * @param xmlDesc
     *            the Network Description
     * @return the Network object representing the created network
     * @throws LibvirtException
     * @see <a href="http://libvirt.org/format.html#Net1" >The XML format
     *      description</a>
     */
    public Network networkCreateXML(String xmlDesc) throws LibvirtException {
        NetworkPointer ptr = processError(
                libvirt.virNetworkCreateXML(VCP, xmlDesc));
        return new Network(this, ptr);
    }

    /**
     * Defines a network, but does not create it. The properties of the network
     * are based on an XML description similar to the one returned by
     * virNetworkGetXMLDesc()
     *
     * @param xmlDesc
     * @return the resulting Network object
     * @throws LibvirtException
     * @see <a href="http://libvirt.org/format.html#Net1" >The XML format
     *      description</a>
     */
    public Network networkDefineXML(String xmlDesc) throws LibvirtException {
        NetworkPointer ptr = processError(
                libvirt.virNetworkDefineXML(VCP, xmlDesc));
        return new Network(this, ptr);
    }

    /**
     * Defines a networkFilter
     *
     * @param xmlDesc
     *            the descirption of the filter
     * @return the new filer
     * @throws LibvirtException
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virNWFilterDefineXML"
     *      > Libvirt Documentation </a>
     */
    public NetworkFilter networkFilterDefineXML(String xmlDesc)
            throws LibvirtException {
        NetworkFilterPointer ptr = processError(
                libvirt.virNWFilterDefineXML(VCP, xmlDesc));
        return new NetworkFilter(this, ptr);
    }

    /**
     * Fetch a network filter based on its unique name
     *
     * @param name
     *            name of network filter to fetch
     * @return network filter object
     * @throws LibvirtException
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virNWFilterLookupByName"
     *      > Libvirt Documentation </a>
     */
    public NetworkFilter networkFilterLookupByName(String name)
            throws LibvirtException {
        NetworkFilterPointer ptr = processError(
                libvirt.virNWFilterLookupByName(VCP, name));
        return new NetworkFilter(this, ptr);
    }

    /**
     * Looks up a network filter based on its UUID in array form. The UUID Array
     * contains an unpacked representation of the UUID, each int contains only
     * one byte.
     *
     * @param UUID
     *            the UUID as an unpacked int array
     * @return the network filter object
     * @throws LibvirtException
     */
    public NetworkFilter networkFilterLookupByUUID(int[] UUID)
            throws LibvirtException {
        byte[] uuidBytes = Connect.createUUIDBytes(UUID);
        NetworkFilterPointer ptr = processError(
                libvirt.virNWFilterLookupByUUID(VCP, uuidBytes));
        return new NetworkFilter(this, ptr);
    }

    /**
     * Fetch a network filter based on its globally unique id
     *
     * @param uuid
     *            a java UUID
     * @return a new network filter object
     * @throws LibvirtException
     */
    public NetworkFilter networkFilterLookupByUUID(UUID uuid)
            throws LibvirtException {
        return networkFilterLookupByUUIDString(uuid.toString());
    }

    /**
     * Looks up a network filter based on its UUID in String form.
     *
     * @param UUID
     *            the UUID in canonical String representation
     * @return the Network Filter object
     * @throws LibvirtException
     */
    public NetworkFilter networkFilterLookupByUUIDString(String UUID)
            throws LibvirtException {
        NetworkFilterPointer ptr = processError(
                libvirt.virNWFilterLookupByUUIDString(VCP, UUID));
        return new NetworkFilter(this, ptr);
    }

    /**
     * Looks up a network on the based on its name.
     *
     * @param name
     *            name of the network
     * @return The Network object found
     * @throws LibvirtException
     */
    public Network networkLookupByName(String name) throws LibvirtException {
        NetworkPointer ptr = processError(
                libvirt.virNetworkLookupByName(VCP, name));
        return new Network(this, ptr);
    }

    /**
     * Looks up a network based on its UUID represented as an int array. The
     * UUID Array contains an unpacked representation of the UUID, each int
     * contains only one byte.
     *
     * @param UUID
     *            the UUID as an unpacked int array
     * @return The Network object found
     * @throws LibvirtException
     * @deprecated use the UUIDString or UUID API.
     */
    @Deprecated
    public Network networkLookupByUUID(int[] UUID) throws LibvirtException {
        byte[] uuidBytes = Connect.createUUIDBytes(UUID);
        NetworkPointer ptr = processError(
                libvirt.virNetworkLookupByUUID(VCP, uuidBytes));
        return new Network(this, ptr);
    }

    /**
     * Fetch a network based on its globally unique id
     *
     * @param uuid
     *            a java UUID
     * @return a new network object
     * @throws LibvirtException
     */
    public Network networkLookupByUUID(UUID uuid) throws LibvirtException {
        return networkLookupByUUIDString(uuid.toString());
    }

    /**
     * Looks up a network based on its UUID represented as a String.
     *
     * @param UUID
     *            the UUID in canonical String representation
     * @return The Network object found
     * @throws LibvirtException
     */
    public Network networkLookupByUUIDString(String UUID)
            throws LibvirtException {
        NetworkPointer ptr = processError(
                libvirt.virNetworkLookupByUUIDString(VCP, UUID));
        return new Network(this, ptr);
    }

    /**
     * Returns a NodeInfo object describing the hardware configuration of the
     * node.
     *
     * @return a NodeInfo object
     * @throws LibvirtException
     */
    public NodeInfo nodeInfo() throws LibvirtException {
        virNodeInfo vInfo = new virNodeInfo();
        processError(libvirt.virNodeGetInfo(VCP, vInfo));
        return new NodeInfo(vInfo);
    }

    /**
     * Provides the number of inactive domains.
     *
     * @return the number of inactive domains
     * @throws LibvirtException
     */
    public int numOfDefinedDomains() throws LibvirtException {
        return processError(libvirt.virConnectNumOfDefinedDomains(VCP));
    }

    /**
     * Provides the number of defined interfaces.
     *
     * @return the number of interfaces
     * @throws LibvirtException
     */
    public int numOfDefinedInterfaces() throws LibvirtException {
        return processError(libvirt.virConnectNumOfDefinedInterfaces(VCP));
    }

    /**
     * Provides the number of inactive networks.
     *
     * @return the number of inactive networks
     * @throws LibvirtException
     */
    public int numOfDefinedNetworks() throws LibvirtException {
        return processError(libvirt.virConnectNumOfDefinedNetworks(VCP));
    }

    /**
     * Provides the number of inactive storage pools
     *
     * @return the number of pools found
     * @throws LibvirtException
     */
    public int numOfDefinedStoragePools() throws LibvirtException {
        return processError(libvirt.virConnectNumOfDefinedStoragePools(VCP));
    }

    /**
     * Provides the number of node devices.
     *
     * @return the number of inactive domains
     * @throws LibvirtException
     */
    public int numOfDevices(String capabilityName) throws LibvirtException {
        return processError(
                libvirt.virNodeNumOfDevices(VCP, capabilityName, 0));
    }

    /**
     * Provides the number of active domains.
     *
     * @return the number of active domains
     * @throws LibvirtException
     */
    public int numOfDomains() throws LibvirtException {
        return processError(libvirt.virConnectNumOfDomains(VCP));
    }

    /**
     * Provides the number of interfaces.
     *
     * @return the number of interfaces
     * @throws LibvirtException
     */
    public int numOfInterfaces() throws LibvirtException {
        return processError(libvirt.virConnectNumOfInterfaces(VCP));
    }

    /**
     * Provides the number of network filters
     *
     * @return the number of network filters
     * @throws LibvirtException
     */
    public int numOfNetworkFilters() throws LibvirtException {
        return processError(libvirt.virConnectNumOfNWFilters(VCP));
    }

    /**
     * Provides the number of active networks.
     *
     * @return the number of active networks
     * @throws LibvirtException
     */
    public int numOfNetworks() throws LibvirtException {
        return processError(libvirt.virConnectNumOfNetworks(VCP));
    }

    /**
     * Fetch number of currently defined secrets.
     *
     * @return the number of secrets
     */
    public int numOfSecrets() throws LibvirtException {
        return processError(libvirt.virConnectNumOfSecrets(VCP));
    }

    /**
     * Provides the number of active storage pools
     *
     * @return the number of pools found
     * @throws LibvirtException
     */
    public int numOfStoragePools() throws LibvirtException {
        return processError(libvirt.virConnectNumOfStoragePools(VCP));
    }

    /**
     * call the error handling logic. Should be called after every libvirt call
     *
     * @throws LibvirtException
     */
    protected void processError() throws LibvirtException {
        ErrorHandler.processError(libvirt);
    }

    /**
     * Calls {@link #processError()} when the given libvirt return code
     * indicates an error.
     *
     * @param ret
     *            libvirt return code, indicating error if negative.
     * @return {@code ret}
     * @throws LibvirtException
     */
    protected final int processError(int ret) throws LibvirtException {
        if (ret < 0)
            processError();
        return ret;
    }

    /**
     * Calls {@link #processError()} if {@code arg} is null.
     *
     * @param arg
     *            An arbitrary object returned by libvirt.
     * @return {@code arg}
     * @throws LibvirtException
     */
    protected final <T> T processError(T arg) throws LibvirtException {
        if (arg == null)
            processError();
        return arg;
    }

    /**
     * Restores a domain saved to disk by Domain.save().
     *
     * @param from
     *            the path of the saved file on the remote host
     * @throws LibvirtException
     */
    public void restore(String from) throws LibvirtException {
        processError(libvirt.virDomainRestore(VCP, from));
    }

    /**
     * If XML specifies a UUID, locates the specified secret and replaces all
     * attributes of the secret specified by UUID by attributes specified in xml
     * (any attributes not specified in xml are discarded). Otherwise, creates a
     * new secret with an automatically chosen UUID, and initializes its
     * attributes from xml.
     *
     * @param xmlDesc
     *            the secret to create
     * @return the Secret object
     * @throws LibvirtException
     */
    public Secret secretDefineXML(String xmlDesc) throws LibvirtException {
        SecretPointer ptr = processError(
                libvirt.virSecretDefineXML(VCP, xmlDesc, 0));
        return new Secret(this, ptr);
    }

    /**
     * Looks up a secret based on its UUID in array form. The UUID Array
     * contains an unpacked representation of the UUID, each int contains only
     * one byte.
     *
     * @param UUID
     *            the UUID as an unpacked int array
     * @return the Secret object
     * @throws LibvirtException
     */
    public Secret secretLookupByUUID(int[] UUID) throws LibvirtException {
        byte[] uuidBytes = Connect.createUUIDBytes(UUID);
        SecretPointer ptr = processError(
                libvirt.virSecretLookupByUUID(VCP, uuidBytes));
        return new Secret(this, ptr);
    }

    /**
     * Fetch a secret based on its globally unique id
     *
     * @param uuid
     *            a java UUID
     * @return a new domain object
     * @throws LibvirtException
     */
    public Secret secretLookupByUUID(UUID uuid) throws LibvirtException {
        return secretLookupByUUIDString(uuid.toString());
    }

    /**
     * Looks up a secret based on its UUID in String form.
     *
     * @param UUID
     *            the UUID in canonical String representation
     * @return the Domain object
     * @throws LibvirtException
     */
    public Secret secretLookupByUUIDString(String UUID)
            throws LibvirtException {
        SecretPointer ptr = processError(
                libvirt.virSecretLookupByUUIDString(VCP, UUID));
        return new Secret(this, ptr);
    }

    public void setConnectionErrorCallback(Libvirt.VirErrorCallback callback)
            throws LibvirtException {
        libvirt.virConnSetErrorFunc(VCP, null, callback);
        processError();
    }

    /**
     * change the amount of memory reserved to Domain0. Domain0 is the domain
     * where the application runs. This function may requires priviledged access
     * to the hypervisor.
     *
     * @param memory
     *            in kilobytes
     * @throws LibvirtException
     */
    public void setDom0Memory(long memory) throws LibvirtException {
        processError(libvirt.virDomainSetMemory(null, new NativeLong(memory)));
    }

    /**
     * Create a new storage based on its XML description. The pool is not
     * persistent, so its definition will disappear when it is destroyed, or if
     * the host is restarted
     *
     * @param xmlDesc
     *            XML description for new pool
     * @param flags
     *            future flags, use 0 for now
     * @return StoragePool object
     * @throws LibvirtException
     */
    public StoragePool storagePoolCreateXML(String xmlDesc, int flags)
            throws LibvirtException {
        StoragePoolPointer ptr = processError(
                libvirt.virStoragePoolCreateXML(VCP, xmlDesc, flags));
        return new StoragePool(this, ptr);
    }

    /**
     * Define a new inactive storage pool based on its XML description. The pool
     * is persistent, until explicitly undefined.
     *
     * @param xml
     *            XML description for new pool
     * @param flags
     *            flags future flags, use 0 for now
     * @return StoragePool object
     * @throws LibvirtException
     */
    public StoragePool storagePoolDefineXML(String xml, int flags)
            throws LibvirtException {
        StoragePoolPointer ptr = processError(
                libvirt.virStoragePoolDefineXML(VCP, xml, flags));
        return new StoragePool(this, ptr);
    }

    /**
     * Fetch a storage pool based on its unique name
     *
     * @param name
     *            name of pool to fetch
     * @return StoragePool object
     * @throws LibvirtException
     */
    public StoragePool storagePoolLookupByName(String name)
            throws LibvirtException {
        StoragePoolPointer ptr = processError(
                libvirt.virStoragePoolLookupByName(VCP, name));
        return new StoragePool(this, ptr);
    }

    /**
     * Fetch a storage pool based on its globally unique id
     *
     * @param UUID
     *            globally unique id of pool to fetch
     * @return a new network object
     * @throws LibvirtException
     * @deprecated Use the UUIDString or UUID APIs.
     */
    @Deprecated
    public StoragePool storagePoolLookupByUUID(int[] UUID)
            throws LibvirtException {
        byte[] uuidBytes = Connect.createUUIDBytes(UUID);
        StoragePoolPointer ptr = processError(
                libvirt.virStoragePoolLookupByUUID(VCP, uuidBytes));
        return new StoragePool(this, ptr);
    }

    /**
     * Fetch a storage pool based on its globally unique id
     *
     * @param uuid
     *            a java UUID
     * @return a new network object
     * @throws LibvirtException
     */
    public StoragePool storagePoolLookupByUUID(UUID uuid)
            throws LibvirtException {
        return storagePoolLookupByUUIDString(uuid.toString());
    }

    /**
     * Fetch a storage pool based on its globally unique id
     *
     * @param UUID
     *            globally unique id of pool to fetch
     * @return VirStoragePool object
     * @throws LibvirtException
     */
    public StoragePool storagePoolLookupByUUIDString(String UUID)
            throws LibvirtException {
        StoragePoolPointer ptr = processError(
                libvirt.virStoragePoolLookupByUUIDString(VCP, UUID));
        return new StoragePool(this, ptr);
    }

    /**
     * Fetch a a storage volume based on its globally unique key
     *
     * @param key
     *            globally unique key
     * @return a storage volume
     */
    public StorageVol storageVolLookupByKey(String key)
            throws LibvirtException {
        StorageVolPointer sPtr = processError(
                libvirt.virStorageVolLookupByKey(VCP, key));
        return new StorageVol(this, sPtr);
    }

    /**
     * Fetch a storage volume based on its locally (host) unique path
     *
     * @param path
     *            locally unique path
     * @return a storage volume
     */
    public StorageVol storageVolLookupByPath(String path)
            throws LibvirtException {
        StorageVolPointer sPtr = processError(
                libvirt.virStorageVolLookupByPath(VCP, path));
        return new StorageVol(this, sPtr);
    }

    /**
     * Creates a new stream object which can be used to perform streamed I/O
     * with other public API function.
     *
     * @param flags
     *            use Stream.VIR_STREAM_NONBLOCK if non-blocking is required
     * @return the new object
     */
    public Stream streamNew(int flags) throws LibvirtException {
        StreamPointer sPtr = processError(libvirt.virStreamNew(VCP, flags));
        return new Stream(this, sPtr);
    }

    /**
     * Verify the connect is active.
     *
     * @return boolean The true connected, or false not.
     * @throws LibvirtException
     */
    public boolean isConnected() throws LibvirtException {
        return ((VCP != null) ? true : false);
    }

    /**
     * Determine if the connection to the hypervisor is still alive.
     * <p>
     * A connection will be classed as alive if it is either local, or running
     * over a channel (TCP or UNIX socket) which is not closed.
     *
     * @return {@code true} if alive, {@code false} otherwise.
     */
    public boolean isAlive() throws LibvirtException {
        return (1 == processError(libvirt.virConnectIsAlive(VCP)));
    }

    /**
     * Start sending keepalive messages after interval second of inactivity and
     * consider the connection to be broken when no response is received after
     * count keepalive messages sent in a row.
     * <p>
     * In other words, sending count + 1 keepalive message results in closing
     * the connection.
     * <p>
     * When interval is <= 0, no keepalive messages will be sent.
     * <p>
     * When count is 0, the connection will be automatically closed after
     * interval seconds of inactivity without sending any keepalive messages.
     * <p>
     * <em>Note</em>: client has to implement and run event loop to be able to
     * use keepalive messages. Failure to do so may result in connections being
     * closed unexpectedly.
     * <p>
     * <em>Note</em>: This API function controls only keepalive messages sent by
     * the client. If the server is configured to use keepalive you still need
     * to run the event loop to respond to them, even if you disable keepalives
     * by this function.
     *
     * @param interval
     *            number of seconds of inactivity before a keepalive message is
     *            sent
     * @param count
     *            number of messages that can be sent in a row
     * @return {@code true} when successful, {@code false} otherwise.
     */
    public boolean setKeepAlive(int interval, int count)
            throws LibvirtException {
        return (0 == processError(
                libvirt.virConnectSetKeepAlive(VCP, interval, count)));
    }
}
