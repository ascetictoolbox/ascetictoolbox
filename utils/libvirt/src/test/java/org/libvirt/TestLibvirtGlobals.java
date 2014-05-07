package org.libvirt;

import eu.optimis.vc.libvirt.Connect;
import eu.optimis.vc.libvirt.LibvirtException;
import junit.framework.TestCase;

/**
 * libvirt tests not requiring an active connection
 */
public class TestLibvirtGlobals extends TestCase {
    public void testErrorCallback() throws Exception {
        DummyErrorCallback cb = new DummyErrorCallback();
        Connect.setErrorCallback(cb);
        try {
            @SuppressWarnings("unused")
			Connect conn = new Connect("xen://optimis1.leeds/", false);
        } catch (LibvirtException e) {
            // eat it
        }
        assertTrue("We should have caught an error", cb.error);
    }
}
