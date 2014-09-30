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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

/**
 * This lists all known KPI constants, it should therefore not be instantiated.
 *
 * @author Richard
 */
public abstract class KpiList {

    //Power and energy
    public static final String POWER_KPI_NAME = "power";
    public static final String ENERGY_KPI_NAME = "energy";
    //CPU based metrics
    public static final String CPU_COUNT_KPI_NAME = "system.cpu.num";
    public static final String CPU_IDLE_KPI_NAME=  "system.cpu.util[,idle]";
    public static final String CPU_INTERUPT_KPI_NAME = "system.cpu.util[,interrupt]";
    public static final String CPU_IO_WAIT_KPI_NAME = "system.cpu.util[,iowait]";
    public static final String CPU_NICE_KPI_NAME = "system.cpu.util[,nice]";
    public static final String CPU_SOFT_IRQ_KPI_NAME = "system.cpu.util[,softirq]";
    public static final String CPU_STEAL_KPI_NAME = "system.cpu.util[,steal]";
    public static final String CPU_SYSTEM_KPI_NAME = "system.cpu.util[,system]";
    public static final String CPU_USER_KPI_NAME = "system.cpu.util[,user]";
    public static final String CPU_LOAD_LAST_1_MIN_KPI_NAME = "system.cpu.load[percpu,avg1]";
    public static final String CPU_LOAD_LAST_5_MIN_KPI_NAME = "system.cpu.load[percpu,avg5]";
    public static final String CPU_LOAD_LAST_15_MIN_KPI_NAME = "system.cpu.load[percpu,avg15]";    
    //memory metrics
    public static final String MEMORY_AVAILABLE_KPI_NAME = "vm.memory.size[available]";     
    public static final String MEMORY_TOTAL_KPI_NAME = "vm.memory.size[total]";
    //swap space
    public static final String SWAP_SPACE_FREE_KPI_NAME = "system.swap.size[,free]";     
    public static final String SWAP_SPACE_FREE_PERC_KPI_NAME = "system.swap.size[,pfree]";     
    public static final String SWAP_SPACE_TOTAL_KPI_NAME = "system.swap.size[,total]";     
    //disk metrics
    public static final String DISK_FREE_KPI_NAME = "vfs.fs.size[/,free]"; 
    public static final String DISK_FREE_PERC_KPI_NAME = "vfs.fs.size[/,pfree]"; 
    public static final String DISK_USED_KPI_NAME = "vfs.fs.size[/,used]"; 
    public static final String DISK_TOTAL_KPI_NAME = "vfs.fs.size[/,total]";
    //Network
    public static final String NETWORK_OUT_STARTS_WITH_KPI_NAME = "net.if.in[eth";    
    public static final String NETWORK_IN_STARTS_WITH_KPI_NAME = "net.if.out[eth";    
    //boot time
    public static final String BOOT_TIME_KPI_NAME = "system.boottime";
    //physical host mapping
    public static final String VM_PHYSICAL_HOST_NAME = "physical_host";

}
