package org.jvmmonitor.internal.agent;

public interface PowerMXBean {

    /**
     * Gets the state indicating if monitoring SWT resources is supported.
     * 
     * @return <tt>true</tt> if monitoring SWT resources is supported
     */
    public boolean isSupported();
	
}
