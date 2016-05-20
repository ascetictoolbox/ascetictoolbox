package org.jvmmonitor.internal.agent;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.CpuOnlyBestFitEnergyPredictor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.EnergyPredictorInterface;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Power implements PowerMXBean {

    /** The MXBean name. */
    public final static String POWER_MXBEAN_NAME = "org.jvmmonitor:type=Power";
    private OperatingSystemMXBean operatingSystemMXBean;
    private String predictorName = "CpuOnlyBestFitEnergyPredictor";
    private EnergyPredictorInterface predictor = null;  //getPredictor(predictorName);
    private static final String DEFAULT_PREDICTOR_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor";    
    private ArrayList<HostEnergyCalibrationData> calibrationData = new ArrayList<>();
    
    private long nanoBefore = 0;
    private long cpuBefore = 0;
    Host host = new Host(0, "localhost"); 

    /**
     * The constructor.
     */
    public Power() {
        operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        nanoBefore = System.nanoTime();
        cpuBefore = getProcessCpuTime();
        calibrationData.add(new HostEnergyCalibrationData(0, 0, 50));
        calibrationData.add(new HostEnergyCalibrationData(100, 0, 100));
        calibrationData.add(new HostEnergyCalibrationData(25, 0, 65));
        calibrationData.add(new HostEnergyCalibrationData(50, 0, 75));
        calibrationData.add(new HostEnergyCalibrationData(85, 0, 90));
        host.setAvailable(true);
        host.setDiskGb(20);
        host.setRamMb(2048);
        host.setCalibrationData(calibrationData);
        //TODO Ensure the predictor doesn't take settings from disk
        //predictor = new CpuOnlyBestFitEnergyPredictor();
    }

    /**
     * Gets the state indicating if monitoring SWT resources is supported.
     * 
     * @return <tt>true</tt> if monitoring SWT resources is supported
     */
    public boolean isSupported() {
        return true;
    }
    
    public void setHostCalibrationData(ArrayList<HostEnergyCalibrationData> calibrationData) {
    	this.calibrationData = calibrationData;
    }

    /**
     * Gets the value for the attribute power
     * 
     * @return the value for power
     */
    public double getPower() {

      if (predictor != null) {
        long nanoAfter = System.nanoTime();
        long cpuAfter = getProcessCpuTime();

        double cpuPercentage = 0.0;
        if (nanoAfter > nanoBefore) {
            cpuPercentage = ((cpuAfter - cpuBefore) * 100.0)
                    / (double) (nanoAfter - nanoBefore);
        }

        nanoBefore = nanoAfter;
        cpuBefore = cpuAfter;
      
        double power = 0.0;
        if (!host.isCalibrated()) {
        	host.setCalibrationData(calibrationData);  
        }
        power = predictor.predictPowerUsed(host, cpuPercentage);        
    	return power;
     } else {	 
    		return 10;
     }

    }
    
    /**
     * This allows the power estimator to be set
     *
     * @param powerUtilisationPredictor The name of the predictor to use
     * @return The predictor to use.
     */
    public EnergyPredictorInterface getPredictor(String powerUtilisationPredictor) {
        EnergyPredictorInterface answer = null;
        try {
            if (!powerUtilisationPredictor.startsWith(DEFAULT_PREDICTOR_PACKAGE)) {
                powerUtilisationPredictor = DEFAULT_PREDICTOR_PACKAGE + "." + powerUtilisationPredictor;
            }
            answer = (EnergyPredictorInterface) (Class.forName(powerUtilisationPredictor).newInstance());
        } catch (ClassNotFoundException ex) {
            if (answer == null) {
                answer = new CpuOnlyBestFitEnergyPredictor();
            }
            Logger.getLogger(Power.class.getName()).log(Level.WARNING, "The predictor specified was not found");
        } catch (InstantiationException | IllegalAccessException ex) {
            if (answer == null) {
                answer = new CpuOnlyBestFitEnergyPredictor();
            }
            Logger.getLogger(Power.class.getName()).log(Level.WARNING, "The predictor specified did not work", ex);
        }
        return answer;
    }       

    private long getProcessCpuTime() {
        try {
            if (Class.forName("com.sun.management.OperatingSystemMXBean")
                    .isInstance(operatingSystemMXBean)) {
                Method processCpuTime = operatingSystemMXBean.getClass()
                        .getDeclaredMethod("getProcessCpuTime");
                processCpuTime.setAccessible(true);
                long time = (Long) processCpuTime.invoke(operatingSystemMXBean);
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
