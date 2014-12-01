/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package eu.ascetic.paas.slam.poc.negotiation;

import java.util.Collection;

import org.slasoi.infrastructure.servicemanager.types.ProvisionRequestType;
import org.slasoi.infrastructure.servicemanager.types.ReservationResponseType;
import org.slasoi.monitoring.common.configuration.MonitoringSystemConfiguration;
import org.slasoi.monitoring.common.features.ComponentMonitoringFeatures;

import datastructure.Request;

/**
 * The implementation of interface <code>ServiceManagerHandler</code>
 * 
 * @see org.slasoi.isslam.poc.servicesmanager.ServiceManagerHandler
 * @author Kuan Lu
 */
public class ServiceManagerHandlerImpl {
    public static ServiceManagerHandlerImpl instance;

    /**
     * Gets the instance of infrastructure service manager handler.
     */
    public static ServiceManagerHandlerImpl getInstance() {
        if (ServiceManagerHandlerImpl.instance == null) {
            ServiceManagerHandlerImpl.instance = new ServiceManagerHandlerImpl();
            return ServiceManagerHandlerImpl.instance;
        }
        else
            return ServiceManagerHandlerImpl.instance;
    }

    public ComponentMonitoringFeatures[] getMonitoringFeature() {
            return null;
    }

    public boolean prepare() {
        return false;
    }
    
    public ProvisionRequestType generateProvisionRequestType(Collection<Request> requestList, MonitoringSystemConfiguration monitoringConfig ){
        // generate ProvisionRequestType according to the requests and monitoring ability
        return new ProvisionRequestType();
    }

    /**
     * Queries the resource capability of service manager.
     */
    public ProvisionRequestType query(ProvisionRequestType temp) {
        return new ProvisionRequestType();
    }

    /**
     * Reserves the resources from infrastructure service manager.
     */
    public ReservationResponseType reserve(ProvisionRequestType temp) {
        return new ReservationResponseType();
    }

}
