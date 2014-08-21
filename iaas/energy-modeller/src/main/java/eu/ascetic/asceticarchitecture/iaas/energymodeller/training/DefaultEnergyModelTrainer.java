/**
 * Copyright 2014 Athens University of Economics and Business
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.asceticarchitecture.iaas.energymodeller.training;

/**
 * This is implements the basic trainer for the energy model for the ASCETiC
 * project.
 *
 * @author E. Agiatzidou
 */
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DefaultEnergyModelTrainer implements EnergyModelTrainerInterface {

    public DefaultEnergyModelTrainer() {
    }

    public static HashMap<Host, ArrayList<HostEnergyCalibrationData>> storeValues = new HashMap<>();

    /**
     * This method stores the appropriate values that are needed for training
     * the model. It should be called several times for a specific number of
     * values to be gathered.
     *
     * @param host The host resource that the model is to be trained for
     * @param usageCPU The CPU usage of the host
     * @param usageRAM The RAM usage of the host
     * @param wattsUsed The watts consumed under these levels of usage
     * @param numberOfValues The number of values that the trainer expects from
     * the user before it is ready for extracting the coefficients of the model.
     * @return True if the appropriate amount of values has been gathered, False
     * if not.
     */
    @Override
    public boolean trainModel(Host host, double usageCPU, double usageRAM, double wattsUsed, int numberOfValues) {
        HostEnergyCalibrationData usageHost = new HostEnergyCalibrationData(usageCPU, usageRAM, wattsUsed);
        ArrayList<HostEnergyCalibrationData> temp = new ArrayList<>();
        int num = 0;
        if (storeValues.containsKey(host)) {
            temp = storeValues.get(host);
            temp.add(usageHost);
            storeValues.put(host, temp);
            num = temp.size();
        } else {
            temp.add(usageHost);
            storeValues.put(host, temp);
        }
        return (num >= numberOfValues);
    }

    /**
     * This method stores the appropriate values that are needed for training
     * the model. It loads all the data in at once, if called a second time it
     * will append to the data already held.
     *
     * @param host The host resource that the model is to be trained for
     * @param data The set of data that is to be added to the training data.
     */
    @Override
    public void trainModel(Host host, ArrayList<HostEnergyCalibrationData> data) {
        if (storeValues.containsKey(host)) {
            storeValues.get(host).addAll(data);
        } else {
            storeValues.put(host, data);
        }
    }

    /**
     * This function prints the HashMap that stores the values needed for the
     * model training for a specific host.
     *
     * @param storeValues The HashMap storing the values
     * @param host The host (key) for which the values are going to be printed
     *
     */
    public void printValuesMap(HashMap<Host, ArrayList<HostEnergyCalibrationData>> storeValues, Host host) {

        ArrayList<HostEnergyCalibrationData> data = storeValues.get(host);
        System.out.print(host.getHostName() + ": ");
        HostEnergyCalibrationData next;
        for (Iterator< HostEnergyCalibrationData> it = data.iterator(); it.hasNext();) {
            next = it.next();
            System.out.println("CPU" + next.getCpuUsage());
            System.out.println(" RAM" + next.getMemoryUsage());
            System.out.println(" watts" + next.getWattsUsed());
        }

        System.out.println();
    }

    /**
     * This function should use the values stored to create the coefficients of
     * the model.
     *
     * @param host The host (key) for which the values are going to be printed.
     *
     * @return The coefficients and the intercept of the model.
     */
    @Override
    public EnergyModel retrieveModel(Host host) {
        EnergyModel temp = new EnergyModel();
        ArrayList<HostEnergyCalibrationData> valuesOfHost = storeValues.get(host);

        ArrayList<Double> ur = new ArrayList<>();
        ArrayList<Double> cpuEnergy = new ArrayList<>();
        ArrayList<Double> cpuRam = new ArrayList<>();
        ArrayList<Double> ramEnergy = new ArrayList<>();
        ArrayList<Double> uc = new ArrayList<>();

        HostEnergyCalibrationData currentItem;
        double energy = 0.0;
        double cpu = 0.0;
        double ram = 0.0;
        for (Iterator<HostEnergyCalibrationData> it = valuesOfHost.iterator(); it.hasNext();) {
            currentItem = it.next();

            ur.add(currentItem.getMemoryUsage() * currentItem.getMemoryUsage());
            cpuEnergy.add(currentItem.getCpuUsage() * currentItem.getWattsUsed());
            cpuRam.add(currentItem.getCpuUsage() * currentItem.getMemoryUsage());
            ramEnergy.add(currentItem.getMemoryUsage() * currentItem.getWattsUsed());
            uc.add(currentItem.getCpuUsage() * currentItem.getCpuUsage());
            energy = currentItem.getWattsUsed();
            cpu = currentItem.getCpuUsage();
            ram = currentItem.getMemoryUsage();

        }
        double sumUr = calculateSums(ur);
        double sumCpuEnergy = calculateSums(cpuEnergy);
        double sumCpuRam = calculateSums(cpuRam);
        double sumRamEnergy = calculateSums(ramEnergy);
        double sumUc = calculateSums(uc);

        double coefficientCpu = (sumUr * sumCpuEnergy - sumCpuRam * sumRamEnergy) / (sumUr * sumUc - (sumCpuRam * sumCpuRam));
        double coefficientRam = (sumUc * sumRamEnergy - sumCpuRam * sumCpuEnergy) / (sumUr * sumUc - (sumCpuRam * sumCpuRam));
        double intercept = energy - coefficientCpu * cpu - coefficientRam * ram;
        temp.setCoefCPU(coefficientCpu);
        temp.setCoefRAM(coefficientRam);
        temp.setIntercept(intercept);

        return temp;

    }

    /**
     * This function calculates the sums of the values in an Array List.
     *
     * @param valuesList The ArrayList to calculate the values of.
     *
     * @return The sum of the values.
     */
    private double calculateSums(ArrayList<Double> valuesList) {
        double sum = 0.0;
        for (Iterator<Double> it = valuesList.iterator(); it.hasNext();) {
            sum = sum + it.next();
        }
        return sum;
    }

}
