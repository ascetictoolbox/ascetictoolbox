package eu.ascetic.vmc.libvirt;

import eu.ascetic.vmc.libvirt.jna.Libvirt;
import eu.ascetic.vmc.libvirt.jna.virError;

/**
 * Utility class which processes the last error from the libvirt library. It
 * turns errors into Libvirt Exceptions.
 *
 * @author bkearney
 */
public class ErrorHandler {

    /**
     * Look for the latest error from libvirt not tied to a connection
     *
     * @param libvirt
     *            the active connection
     * @throws LibvirtException
     */
    public static void processError(Libvirt libvirt) throws LibvirtException {
        virError vError = libvirt.virGetLastError();
        if (vError != null) {
            Error error = new Error(vError);
            /*
             * Don't throw exceptions for VIR_ERR_WARNING level errors
             */
            if (error.getLevel() == Error.ErrorLevel.VIR_ERR_ERROR) {
                throw new LibvirtException(error);
            }
        }
    }
}
