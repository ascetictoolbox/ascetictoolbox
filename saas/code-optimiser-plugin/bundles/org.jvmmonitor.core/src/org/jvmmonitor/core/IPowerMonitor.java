package org.jvmmonitor.core;

public interface IPowerMonitor {

    /**
     * Sets the tracking state.
     * 
     * @param tracking
     *            <tt>true</tt> to enable tracking
     * @throws JvmCoreException
     */
    void setTracking(boolean tracking) throws JvmCoreException;

    /**
     * Gets the tracking state.
     * 
     * @return <tt>true</tt> if tracking is enabled
     * @throws JvmCoreException
     */
    boolean isTracking() throws JvmCoreException;

    /**
     * Refreshes the resources cache.
     * 
     * @throws JvmCoreException
     */
    void refreshResourcesCache() throws JvmCoreException;

    /**
     * Clears the tracked resources.
     * 
     * @throws JvmCoreException
     */
    void clear() throws JvmCoreException;

    /**
     * Gets the state indicating if SWT resource monitor is supported.
     * 
     * @return <tt>true</tt> if SWT resource monitor is supported
     */
    boolean isSupported();	
	
}
