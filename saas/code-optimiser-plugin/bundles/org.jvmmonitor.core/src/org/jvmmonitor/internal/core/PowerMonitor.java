package org.jvmmonitor.internal.core;

import javax.management.Attribute;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.core.runtime.IStatus;
import org.jvmmonitor.core.IPowerMonitor;
import org.jvmmonitor.core.JvmCoreException;
import org.jvmmonitor.core.JvmModel;

public class PowerMonitor implements IPowerMonitor {

	/** The MXBean name. */
//    public final static String POWER_MXBEAN_NAME = "org.jvmmonitor.internal.core:type=PowerMonitor";
    public final static String POWER_MXBEAN_NAME = "org.jvmmonitor:type=Power";

    /** The Tracking attribute in SWTResourceMonitorMXBean. */
    private static final String TRACKING = "Tracking"; //$NON-NLS-1$	
	
    /** The JVM. */
    private ActiveJvm jvm;	
	
    /**
     * The constructor.
     * 
     * @param jvm
     *            The JVM
     */
    public PowerMonitor(ActiveJvm jvm) {
    	System.err.println("Power Monitor Seen");
        this.jvm = jvm;
//        Attribute  attr = new Attribute("Power", 25);
//        try {
//        	ObjectName power = new ObjectName("Power");
//			jvm.getMBeanServer().setAttribute(power, attr);
//		} catch (MalformedObjectNameException | JvmCoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    }	
	
    /*
     * @see IPowerMonitor#setTracking(boolean)
     */
    @Override
    public void setTracking(boolean tracking) throws JvmCoreException {
    	System.err.println("Power - Start Tracking");
    	ObjectName objectName = validateAgent();
        if (objectName != null) {
            jvm.getMBeanServer().setAttribute(objectName,
                    new Attribute(TRACKING, tracking));
        }
        System.err.println("Power - Start Tracking - END");
    }

    /*
     * @see IPowerMonitor#isTracking()
     */
    @Override
    public boolean isTracking() throws JvmCoreException {
//    	return true;
    	System.err.println("Power - Is Tracking");
        ObjectName objectName = validateAgent();
        if (objectName != null) {
            Object attribute = jvm.getMBeanServer().getAttribute(objectName,
                    TRACKING);
            if (attribute instanceof Boolean) {
            	System.out.println("Power - Is Tracking (OK) - " + ((Boolean) attribute).booleanValue());
            	return ((Boolean) attribute).booleanValue();
            }
        }
        System.err.println("Power - Is Tracking - False");
        return false;
    }

	@Override
	public void refreshResourcesCache() throws JvmCoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() throws JvmCoreException {
		// TODO Auto-generated method stub
		
	}
	
    /**
     * Validates the agent.
     * 
     * @return The object name for Power resource monitor MXBean
     * @throws JvmCoreException
     */
    private ObjectName validateAgent() throws JvmCoreException {
        System.err.println("Start Validate Agent");
    	if (!jvm.isRemote()
                && !JvmModel.getInstance().getAgentLoadHandler()
                        .isAgentLoaded()) {
            throw new JvmCoreException(IStatus.ERROR,
                    Messages.agentNotLoadedMsg, new Exception());
        }
    	System.err.println("End Validate Agent");
        return jvm.getMBeanServer().getObjectName(
        		POWER_MXBEAN_NAME);
    }	

	@Override
	public boolean isSupported() {
//		return true;
		System.err.println("Start Is Supported");
		try {
            ObjectName objectName = validateAgent();
            if (objectName == null) {
            	System.out.println("Is not Supported");
                return false;
            }
            Object attribute = jvm.getMBeanServer().getAttribute(objectName,
                    TRACKING);
            System.err.println("Is Supported - " + attribute != null);
            return attribute != null;
        } catch (JvmCoreException e) {
        	System.err.println("Is not Supported - Error");
        	e.printStackTrace();
            return false;
        }
	}

}
