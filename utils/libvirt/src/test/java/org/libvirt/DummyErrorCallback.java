package org.libvirt;


import com.sun.jna.Pointer;

import eu.optimis.vc.libvirt.ErrorCallback;
import eu.optimis.vc.libvirt.jna.virError;

public class DummyErrorCallback extends ErrorCallback {
    public boolean error = false;

    @Override
    public void errorCallback(Pointer userData, virError error) {
        this.error = true;
    }

}
