package org.jvmmonitor.internal.core;

import java.util.ArrayList;

import javax.management.Attribute;
import javax.management.ObjectName;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.core.runtime.IStatus;
import org.jvmmonitor.core.IPowerMonitor;
import org.jvmmonitor.core.JvmCoreException;
import org.jvmmonitor.core.JvmModel;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.CpuOnlyBestFitEnergyPredictor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.EnergyPredictorInterface;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;

public class PowerMonitor implements IPowerMonitor {

    /** The Tracking attribute in SWTResourceMonitorMXBean. */
    private static final String TRACKING = "Tracking"; //$NON-NLS-1$

    /** The JVM. */
    private ActiveJvm jvm;

    private EnergyPredictorInterface predictor = null;
    
    private eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host host 
        = new eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host(0, "localhost");      
    
    /**
     * The constructor.
     * 
     * @param jvm
     *            The JVM
     */
    public PowerMonitor(ActiveJvm jvm) {
        this.jvm = jvm;
        //TODO Set this calibration data correctly
        ArrayList<HostEnergyCalibrationData> calibrationData = new ArrayList<>();
        calibrationData.add(new HostEnergyCalibrationData(0, 0, 50));
        calibrationData.add(new HostEnergyCalibrationData(25, 0, 62.5));
        calibrationData.add(new HostEnergyCalibrationData(50, 0, 75));
        calibrationData.add(new HostEnergyCalibrationData(75, 0, 87.5));
        calibrationData.add(new HostEnergyCalibrationData(100, 0, 100));        
        host.setAvailable(true);
        host.setDiskGb(20);
        host.setRamMb(2048);
        host.setCalibrationData(calibrationData);
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.setProperty("iaas.energy.modeller.cpu.energy.predictor.default_load", 0);
        config.setProperty("iaas.energy.modeller.cpu.energy.predictor.vm_share_rule", "DefaultEnergyShareRule");
        config.setProperty("iaas.energy.modeller.cpu.energy.predictor.consider_idle_energy", true);
        config.setProperty("iaas.energy.modeller.energy.predictor.overheadPerHostInWatts", 0);
        config.setProperty("iaas.energy.modeller.cpu.energy.predictor.datasource", "ZabbixDirectDbDataSourceAdaptor");
        config.setProperty("iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.min", 0);
        config.setProperty("iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.sec", 30);
        predictor = new CpuOnlyBestFitEnergyPredictor(config);        
    }    
    
    /**
     * This calculates the power consumption based upon cpu usage
     * 
     * @param cpuUsage
     *            The Utilisation of the CPU
     * @return The power consumption of a given thread.
     */
    @Override
    public double calculatePowerConsumption(double cpuUsage) {
        return predictor.predictPowerUsed(host, cpuUsage) - host.getIdlePowerConsumption();
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
        // return true;
        System.err.println("Power - Is Tracking");
        ObjectName objectName = validateAgent();
        if (objectName != null) {
            Object attribute = jvm.getMBeanServer().getAttribute(objectName,
                    TRACKING);
            if (attribute instanceof Boolean) {
                System.out.println("Power - Is Tracking (OK) - "
                        + ((Boolean) attribute).booleanValue());
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
        if (!jvm.isRemote() && !JvmModel.getInstance().getAgentLoadHandler()
                .isAgentLoaded()) {
            throw new JvmCoreException(IStatus.ERROR,
                    Messages.agentNotLoadedMsg, new Exception());
        }
        System.err.println("End Validate Agent");
        return jvm.getMBeanServer().getObjectName(POWER_MXBEAN_NAME);
    }

    @Override
    public boolean isSupported() {
        // return true;
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
