package org.jvmmonitor.internal.agent;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

public class Power implements PowerMXBean {

    /** The MXBean name. */
    public final static String POWER_MXBEAN_NAME = "org.jvmmonitor:type=Power";
    private OperatingSystemMXBean operatingSystemMXBean;

    private long nanoBefore = 0;
    private long cpuBefore = 0;

    /**
     * The constructor.
     */
    public Power() {
        operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        nanoBefore = System.nanoTime();
        cpuBefore = getProcessCpuTime();
    }

    /**
     * Gets the state indicating if monitoring SWT resources is supported.
     * 
     * @return <tt>true</tt> if monitoring SWT resources is supported
     */
    public boolean isSupported() {
        return true;
    }

    /**
     * Gets the value for the attribute power
     * 
     * @return the value for power
     */
    public double getPower() {

        long nanoAfter = System.nanoTime();
        long cpuAfter = getProcessCpuTime();
        System.err.println("nanoAfter = " + nanoAfter);
        System.err.println("cpuAfter = " + cpuAfter);

        long cpuPercentage;
        if (nanoAfter > nanoBefore) {
            cpuPercentage = ((cpuAfter - cpuBefore) * 100L)
                    / (nanoAfter - nanoBefore);
        } else {
            cpuPercentage = 0;
        }
        System.err.println("cpuPercentage = " + cpuPercentage);

        nanoBefore = nanoAfter;
        cpuBefore = cpuAfter;
        System.err.println("nanoBefore = " + nanoBefore);
        System.err.println("cpuBefore = " + cpuBefore);

        // FIXME Eventually this cpuPercentage will be passed to an Energy Model
        // along with some calibration data
        long power = 0L;
        if (cpuPercentage != 0) {
            long maxPower = 18L;
            power = (maxPower / 100L) * cpuPercentage;
        }

        System.err.println("power = " + power);
        return power;
    }

    private long getProcessCpuTime() {
        try {
            if (Class.forName("com.sun.management.OperatingSystemMXBean")
                    .isInstance(operatingSystemMXBean)) {
                Method processCpuTime = operatingSystemMXBean.getClass()
                        .getDeclaredMethod("getProcessCpuTime");
                processCpuTime.setAccessible(true);
                long time = (Long) processCpuTime.invoke(operatingSystemMXBean);
                System.err.println("processCpuTime() returned: " + time);
                return time; 
            } else {
                // FIXME Add alternative method if sun packages is not available
                System.err.println("Reflection using com.sun.management.OperatingSystemMXBean failed");
                return 0;
            }
        } catch (Exception e) {
            System.err.println(
                    "Error invoking getProcessCpuTime() by reflection: "
                            + e.getMessage());
            return 0;
        }
    }
}
