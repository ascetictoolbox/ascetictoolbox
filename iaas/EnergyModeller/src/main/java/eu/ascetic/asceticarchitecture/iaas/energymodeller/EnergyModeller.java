/**
 * Copyright 2014 University of Leeds
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
package eu.ascetic.asceticarchitecture.iaas.energymodeller;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.DefaultEnergyPredictor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.EnergyPredictorInterface;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.CandidateVMHostMapping;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.input.VMWorkloadProfile;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.CurrentUsageRecord;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HistoricUsageRecord;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the main entry point for the energy modeller. It is going to have the
 * following core functionality:
 *
 * The ability to provide current energy usage values for VMs. The main aim of
 * this will be to aid the placement of VMs on machines. The ability to provide
 * current energy usage for the infrastructure the VMs run upon.
 *
 * Predicted values for the future, energy usage. Including: projecting current
 * deployments energy usage and candidate deployments energy usage i.e. a VM
 * with a workload mapping to a machine.
 *
 * A historic record of energy usage for billing purposes.
 *
 * @author Richard
 */
public class EnergyModeller {

    public EnergyModeller() {
    }
    private static final String DEFAULT_PREDICTOR_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor";
    EnergyPredictorInterface predictor = new DefaultEnergyPredictor();

    /**
     * This allows the energy predictor to be set
     *
     * @param energyPredictor The name of the algorithm to set
     */
    public void setEnergyPredictor(String energyPredictor) {
        try {
            if (!energyPredictor.startsWith(DEFAULT_PREDICTOR_PACKAGE)) {
                energyPredictor = DEFAULT_PREDICTOR_PACKAGE + "." + energyPredictor;
            }
            predictor = (EnergyPredictorInterface) (Class.forName(energyPredictor).newInstance());
        } catch (ClassNotFoundException ex) {
            if (predictor == null) {
                predictor = new DefaultEnergyPredictor();
            }
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.WARNING, "The scheduling algorithm specified was not found");
        } catch (InstantiationException ex) {
            if (predictor == null) {
                predictor = new DefaultEnergyPredictor();
            }
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.WARNING, "The scheduling algorithm did not work");
        } catch (IllegalAccessException ex) {
            if (predictor == null) {
                predictor = new DefaultEnergyPredictor();
            }
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.WARNING, "The scheduling algorithm did not work");
        }
    }

    /**
     * This provides for a collection of VMs the amount of energy that has 
     * historically been used.
     *
     * @param vms The set of virtual machines.
     * @param timePeriod The time period for which the query applies.
     * @return The value returned should be separated on per VM basis. This
     * allows for a set of values to be queried at the same time and also
     * aggregated correctly.
     *
     * Envisaged purpose: Determining the amount of energy being used/having
     * been used by an SLA.
     *
     * Historic Values: Avg Watts over time Avg Current (useful??) Avg Voltage
     * (useful??) kWh of energy used since instantiation
     */
    public HashSet<HistoricUsageRecord> getEnergyRecordForVM(Collection<VM> vms, TimePeriod timePeriod) {
        HashSet<HistoricUsageRecord> answer = new HashSet<>();
        for (VM vm : vms) {
            answer.add(getEnergyRecordForVM(vm, timePeriod));
        }
        return answer;
    }    
    
    /**
     * This provides for a collection of VMs the amount of energy currently in
     * use.
     *
     * @param vms The set of virtual machines.
     * @return The value returned should be separated on per VM basis. This
     * allows for a set of values to be queried at the same time and also
     * aggregated correctly.
     *
     * Envisaged purpose: Determining the amount of energy being used/having
     * been used by an SLA.
     *
     * Current Values: Watts, current and voltage
     * 
     */
    public HashSet<CurrentUsageRecord> getCurrentEnergyForVM(Collection<VM> vms) {
        HashSet<CurrentUsageRecord> answer = new HashSet<>();
        for (VM vm : vms) {
            answer.add(getCurrentEnergyForVM(vm));
        }
        return answer;
    }

    /**
     * This provides for a collection of physical machines the amount of energy
     * that has historically been used.
     *
     * @param hosts The set of physical machines.
     * @param timePeriod The time period for which the query applies.
     * @return The value returned should be separated on per machine basis. This
     * allows for a set of values to be queried at the same time and also
     * aggregated correctly.
     *
     * Historic Values: Avg Watts over time (+ Max/Min??) Avg Current (useful??)
     * Avg Voltage (useful??) kWh of energy used since boot/time immemorial
     * (i.e. long as recorded data)
     *
     */
    public HashSet<HistoricUsageRecord> getEnergyRecordForHost(Collection<Host> hosts, TimePeriod timePeriod) {
        HashSet<HistoricUsageRecord> answer = new HashSet<>();
        for (Host host : hosts) {
            answer.add(getEnergyRecordForHost(host, timePeriod));
        }
        return answer;
    }
    
    /**
     * This provides for a collection of physical machines the amount of energy
     * currently in use.
     *
     * @param hosts The set of physical machines.
     * @return The value returned should be separated on per machine basis. This
     * allows for a set of values to be queried at the same time and also
     * aggregated correctly.
     *
     * Current Values: Watts, current and voltage
     *
     */
    public HashSet<CurrentUsageRecord> getCurrentEnergyForHost(Collection<Host> hosts) {
        HashSet<CurrentUsageRecord> answer = new HashSet<>();
        for (Host host : hosts) {
            answer.add(getCurrentEnergyForHost(host));
        }
        return answer;
    }    

    /**
     * This returns the energy usage for a named virtual machine.
     *
     * @param virtualMachine A reference to the VM
     * @return
     *
     * Current Values: Watts, current and voltage
     * 
     */
    public CurrentUsageRecord getCurrentEnergyForVM(VM virtualMachine) {
        CurrentUsageRecord answer = new CurrentUsageRecord(virtualMachine); //TODO Replace with method call
        return answer;
    }

    /**
     * This returns the energy usage for a named virtual machine.
     *
     * @param virtualMachine A reference to the VM
     * @param timePeriod The time period for which the query applies.
     * @return
     *
     * Historic Values: Avg Watts over time Avg Current (useful??) Avg Voltage
     * (useful??) kWh of energy used since instantiation
     */
    public HistoricUsageRecord getEnergyRecordForVM(VM virtualMachine, TimePeriod timePeriod) {
        HistoricUsageRecord answer = new HistoricUsageRecord(virtualMachine); //TODO Replace with method call
        answer.setDuration(timePeriod);
        return answer;
    }

    /**
     * This returns the energy usage for a named physical machine.
     *
     * @param host A reference to physical machine
     * @param timePeriod The time period for which the query applies.
     * @return
     *
     * Historic Values: Avg Watts over time (+ Max/Min??) Avg Current (useful??)
     * Avg Voltage (useful??) kWh of energy used since boot/time immemorial
     * (i.e. long as recorded data)
     *
     */
    public HistoricUsageRecord getEnergyRecordForHost(Host host, TimePeriod timePeriod) {
        HistoricUsageRecord answer = new HistoricUsageRecord(host); //TODO Replace with method call
        answer.setDuration(timePeriod);
        return answer;
    }

    /**
     * This returns the energy usage for a named physical machine.
     *
     * @param host A reference to physical machine
     * @return
     *
     * Current Values for: Power (Watts), current and voltage
     */
    public CurrentUsageRecord getCurrentEnergyForHost(Host host) {
        CurrentUsageRecord answer = new CurrentUsageRecord(host); //TODO Replace with method call
        return answer;
    }

    /**
     * This provides the amount of energy predicted to be used by the placement
     * of a VM. This allows for a set of possible placement positions to be
     * tested all at once.
     *
     * @param vmImage A reference to the VM image to be deployed
     * @param workload A description of the workload
     * @param hosts The set of machines to provide energy estimates for
     * @return
     *
     * Avg Watts that is expected to use over time by the VM Predicted energy
     * used (kWh) during life of VM
     *
     */
    public HashSet<EnergyUsagePrediction> getPredictedEnergyForVM(VM vmImage, VMWorkloadProfile workload, Collection<Host> hosts) {
        HashSet<EnergyUsagePrediction> answer = new HashSet<>();

        for (Host host : hosts) {
            answer.add(getPredictedEnergyForVM(vmImage, workload, host));
        }
        return answer;
    }

    /**
     * This provides the amount of energy predicted to be used by the placement
     * of a VM.
     *
     * @param vmImage A reference to the VM image to be deployed
     * @param workload A description of the workload
     * @param host The machine to provide an energy estimate for
     * @return
     *
     * Avg Watts that is expected to use over time by the VM Predicted energy
     * used (kWh) during life of VM
     */
    public EnergyUsagePrediction getPredictedEnergyForVM(VM vmImage, VMWorkloadProfile workload, Host host) {
        return new EnergyUsagePrediction(new CandidateVMHostMapping(vmImage, host));
    }

    /**
     * This provides the amount of energy predicted to be used by a VM that has
     * already been placed on the infrastructure.
     *
     * @param virtualMachine A reference to the VM, that future energy usage is
     * to be predicted for.
     * @param workload The workload for the VM
     * @return
     *
     * Avg Watts that is expected to use over time by the VM Predicted energy
     * used (kWh) during life of VM
     */
    public EnergyUsagePrediction getPredictedEnergyForVM(VM virtualMachine, VMWorkloadProfile workload) {
        return new EnergyUsagePrediction(virtualMachine);
    }

    /**
     * This provides the amount of energy predicted to be used by a set of VMs
     * (i.e. a whole SLA) that has already been placed on the infrastructure.
     *
     * @param virtualMachines A reference to the VMs, that future energy usage
     * is to be predicted for.
     * @param workload The workload for the VMs
     * @return
     *
     * Avg Watts that is expected to use over time by the VM Predicted energy
     * used (kWh) during life of VM
     */
    public HashSet<EnergyUsagePrediction> getPredictedEnergyForVM(Collection<VM> virtualMachines, Collection<VMWorkloadProfile> workload) {
        HashSet<EnergyUsagePrediction> answer = new HashSet<>();
        for (VM vmImage : virtualMachines) {
            for (VMWorkloadProfile vMWorkloadProfile : workload) {
                answer.add(getPredictedEnergyForVM(vmImage, vMWorkloadProfile));
            }
        }
        return answer;
    }

    /**
     * This method is not considered part of the scope of 1st Year work. The
     * workload would be difficult to describe and has no forseen usage.
     *
     * @param host The physical machine to predict future energy usage for
     * @param workload The workload for the VMs
     * @return
     * @deprecated Not needed as the workload is hard to describe! Such
     * functionality would be difficult to implement and the outcome would not
     * be clear regarding its value.
     */
    public EnergyUsagePrediction getPredictedEnergyForMachine(Host host, Collection<VMWorkloadProfile> workload) {
        return null;
    }
}
