package org.jvmmonitor.internal.core;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import javax.management.Attribute;
import javax.management.ObjectName;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.core.runtime.IStatus;
import org.jvmmonitor.core.IPowerMonitor;
import org.jvmmonitor.core.JvmCoreException;
import org.jvmmonitor.core.JvmModel;

import eu.ascetic.ioutils.ResultsStore;
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
    
    private static final String CALIBRATION_DATA_FILE = "calibration_data.csv";
    private ResultsStore calibrationData;
    
    /**
     * The constructor.
     * 
     * @param jvm
     *            The JVM
     */
    public PowerMonitor(ActiveJvm jvm) {
        this.jvm = jvm;
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
     * This loads the calibration data from file and sets the calibration data
     * if needed.
     */
    @Override
    public void loadCalibrationData() {
        try {
            setHostCalibrationData(readCalbrationDataFromFile());
        }
        catch (Exception ex) {
            
        }
        if (!host.isCalibrated()) {
            System.out.println("Using Defaults");
            writeDefaultCalibrartionDataToFile();
            useDefaultCalibrationData();
        }
    }
    
    /**
     * This sets the default calibration data in case no data is read from file.
     */
    private void useDefaultCalibrationData () {
        ArrayList<HostEnergyCalibrationData> calibrationData = new ArrayList<>();
        calibrationData.add(new HostEnergyCalibrationData(0, 0, 50));
        calibrationData.add(new HostEnergyCalibrationData(25, 0, 62.5));
        calibrationData.add(new HostEnergyCalibrationData(50, 0, 75));
        calibrationData.add(new HostEnergyCalibrationData(75, 0, 87.5));
        calibrationData.add(new HostEnergyCalibrationData(100, 0, 100));
        host.setHostName(jvm.getHost().getName());
        host.setAvailable(true);
        host.setDiskGb(20);
        host.setRamMb(2048);
        host.setCalibrationData(calibrationData);        
    }
    
    /**
     * This writes the default calibration data to file
     */
    private void writeDefaultCalibrartionDataToFile() {
        if (!calibrationData.getResultsFile().exists()) {
            calibrationData.add("Hostname");
            calibrationData.append("CPU Utilisation");
            calibrationData.append("Power");
            calibrationData.add(host.getHostName());
            calibrationData.append(0);
            calibrationData.append(50);
            calibrationData.add(host.getHostName());
            calibrationData.append(25);
            calibrationData.append(62.5);
            calibrationData.add(host.getHostName());
            calibrationData.append(50);
            calibrationData.append(75);
            calibrationData.add(host.getHostName());
            calibrationData.append(75);
            calibrationData.append(87.5);
            calibrationData.add(host.getHostName());
            calibrationData.append(100);
            calibrationData.append(100);
            calibrationData.save();
        }
    }
    
    private ArrayList<HostEnergyCalibrationData> readCalbrationDataFromFile() {
        ArrayList<HostEnergyCalibrationData> answer = new ArrayList<HostEnergyCalibrationData>();
        String path = PowerMonitor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (Exception ex) {
        }
        calibrationData = new ResultsStore(path + "//" + CALIBRATION_DATA_FILE);        
        if (!calibrationData.getResultsFile().exists()) {
            return answer;
        }
        calibrationData.load();
        //Ignore the header row
        if (calibrationData.size() <= 1) {
            return answer;
        }
        for (int rowNumber = 1; rowNumber < calibrationData.size(); rowNumber++) {
            ArrayList<String> row = calibrationData.getRow(rowNumber);
            if (row.get(0).equals(jvm.getHost().getName())) {
                try {
                    double cpuUsage = Double.parseDouble(row.get(1));
                    double wattsUsed = Double.parseDouble(row.get(2));
                    HostEnergyCalibrationData item = new HostEnergyCalibrationData(cpuUsage, 0, wattsUsed);
                    answer.add(item);
                } catch (Exception ex) {
                    //Catch any parse errors silently
                }
            }
        }
        return answer;
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
        if (!host.isCalibrated()) {
            loadCalibrationData();
        }
        if ("".equals(getHostCalibrationInputString())) {
            try {
                setAgentCalibrationData(validateAgent(), host.getCalibrationData());
            } catch (JvmCoreException e) {
            }
        }
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
    
    /**
     * This sets the agents host calibration data
     * @param objectName
     * @param calibrationData
     */
    private void setAgentCalibrationData(ObjectName objectName, ArrayList<HostEnergyCalibrationData> calibrationData) {
      //Set the server side calibration data
        String attribute = "";
        for (HostEnergyCalibrationData item : calibrationData) {
            attribute = attribute + (attribute.equals("") ? "" : ",") + item.getCpuUsage() + "," + item.getWattsUsed();
        }
        try {
            Attribute attrib = new Attribute("HostCalibrationInputString", attribute);
            jvm.getMBeanServer().setAttribute(objectName, attrib);
        } catch (Exception ex) {
        }
    }
    
    /**
     * 
     * @return
     */
    @Override
    public String getHostCalibrationInputString() {
        try {
            ObjectName objectName = validateAgent();
            return (String) jvm.getMBeanServer().getAttribute(objectName, "HostCalibrationInputString");
        } catch (JvmCoreException ex) {
            return "";
        }
    }
    
    @Override
    public void setHostCalibrationInputString(String calibrationData) {
        System.out.println(calibrationData);
        ArrayList<HostEnergyCalibrationData> data = new ArrayList<>();
        String[] splitString = calibrationData.split(",");
        for (int i = 0; i < splitString.length; i = i + 2) {
            double cpu = Double.parseDouble(splitString[i]);
            double power = Double.parseDouble(splitString[i+1]);
            HostEnergyCalibrationData item = new HostEnergyCalibrationData(cpu, 0, power);
            data.add(item);
        }
        host.setCalibrationData(data);
        try {
            ObjectName objectName = validateAgent();
            setAgentCalibrationData(objectName, host.getCalibrationData());
        } catch (JvmCoreException ex) {               
            }
    }      
    
    @Override
    public void setHostCalibrationData(List<HostEnergyCalibrationData> calibrationData) throws JvmCoreException {
        if (calibrationData.isEmpty()) {
            return;
        }
        ObjectName objectName = validateAgent();
        if (objectName != null) {
            if (calibrationData instanceof ArrayList) {
                host.setCalibrationData(((ArrayList) calibrationData));
            } else {
                ArrayList<HostEnergyCalibrationData> data = new ArrayList<>();
                data.addAll(calibrationData);
                host.setCalibrationData(data);
            }
            setAgentCalibrationData(objectName, host.getCalibrationData());
        }
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
        if (!jvm.isRemote() && !JvmModel.getInstance().getAgentLoadHandler()
                .isAgentLoaded()) {
            throw new JvmCoreException(IStatus.ERROR,
                    Messages.agentNotLoadedMsg, new Exception());
        }
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
