package eu.ascetic.vmc.libvirt;


import com.sun.jna.Pointer;

import eu.ascetic.vmc.libvirt.jna.Libvirt;
import eu.ascetic.vmc.libvirt.jna.virError;

/**
 * Callback class to allow users of the API to handle the Error messages in a
 * more robust fashion.
 *
 * @author bkearney
 *
 */
public class ErrorCallback implements Libvirt.VirErrorCallback {
    public void errorCallback(Pointer userData, virError error) {
        // By default, do nothing. This will silence the default
        // logging done by the C code. Other users can override this
        // and do more interesting things.
    }
}
