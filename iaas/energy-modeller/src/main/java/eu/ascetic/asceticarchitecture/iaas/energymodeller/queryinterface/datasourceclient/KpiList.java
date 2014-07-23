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
    public static final String IDLE_KPI_NAME = "system.cpu.util[,idle]";
    public static final String INTERUPT_KPI_NAME = "system.cpu.util[,interrupt]";
    public static final String IO_WAIT_KPI_NAME = "system.cpu.util[,iowait]";
    public static final String NICE_KPI_NAME = "system.cpu.util[,nice]";
    public static final String SOFT_IRQ_KPI_NAME = "system.cpu.util[,softirq]";
    public static final String STEAL_KPI_NAME = "system.cpu.util[,steal]";
    public static final String SYSTEM_KPI_NAME = "system.cpu.util[,system]";
    public static final String USER_KPI_NAME = "system.cpu.util[,user]";    

}
