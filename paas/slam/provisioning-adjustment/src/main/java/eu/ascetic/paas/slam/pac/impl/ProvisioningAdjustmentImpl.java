/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


/**
 * Copyright (c) 2008-2010, SLASOI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of SLASOI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SLASOI BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author         Beatriz Fuentes - fuentes@tid.es
 * @version        $Rev$
 * @lastrevision   $Date$
 * @filesource     $URL$
 */

package eu.ascetic.paas.slam.pac.impl;

import org.apache.log4j.Logger;
import org.slasoi.gslam.core.control.Policy;
import org.slasoi.gslam.pac.ProvisioningAndAdjustment;

/**
 * DOMAIN SPECIFIC PAC.
 * 
 * @author Miguel Rojas (UDO)
 * 
 */
public class ProvisioningAdjustmentImpl extends ProvisioningAndAdjustment {
    /**
     * logger.
     */
    private static Logger logger = Logger.getLogger(ProvisioningAdjustmentImpl.class.getName());

    /**
     * Constructor.
     * 
     * @param configFile
     *            configuration file to be used by the PAC
     */
    public ProvisioningAdjustmentImpl(final String configFile) {
        super(configFile);
        logger.info("Creating Skeleton PAC...");
    }

    /**
     * Default constructor.
     */
    public ProvisioningAdjustmentImpl() {
        logger.info("Creating Skeleton PAC...");
    }

    /**
     * Triggers the execution of a provisioning plan.
     * 
     * @param plan
     *            the plan to be executed
     * @throws PlanFoundException
     *             if the plan has been already sent for execution
     * @throws PlanFormatException
     *             if the plan is not corrected built
     */
    public void executePlan(Plan plan) throws PlanFoundException, PlanFormatException {
        logger.info("Execute plan " + plan.getPlanId());
    }

    /**
     * Cancels the execution of a plan.
     * 
     * @param planId
     *            the identifier of the plan to be cancelled
     * @throws PlanNotFoundException
     *             if the plan is not being executed
     */
    public void cancelExecution(String planId) throws PlanNotFoundException {
        logger.info("Cancel plan " + planId);
    }

    /**
     * POC informs the PAC that a given action is being executed.
     * 
     * @param planId
     *            identifier of the plan affected by the action
     * @param action
     *            action being executed
     * @param estimatedTime
     *            estimation of time for the action to finish
     * @throws PlanNotFoundException
     *             if the plan is not under PAC's control
     */
    public void ongoingAction(String planId, Task action, long estimatedTime) throws PlanNotFoundException {
        logger.info("Ongoing action " + action.getActionName() + " affecting plan " + planId);
    }

    /**
     * Method to inform about the status of a given plan.
     * 
     * @param planId
     *            the identifier of the plan
     * @return the plan status.
     * @throws PlanNotFoundException
     */
    public Status getPlanStatus(String planId) throws PlanNotFoundException {
        logger.info("get status of plan " + planId);
        return Status.PROVISIONING;
    }

    /**
     * Method to query the LLMS database.
     * 
     * @param ServiceManagerId
     *            ID of the ServiceManager to forward the query
     * @param query
     *            database query
     * @return the result of the query
     */
    // To be changed once the ServiceManagers define the query interface
    public String queryMonitoringDatabase(String ServiceManagerId, String query) {
        logger.info("Executing query " + query + " in Service Manager " + ServiceManagerId);
        return "hello";
    }

    /**
     * Set policies to the PAC. To be used from the business layer.
     * 
     * @param policyClassType
     *            type of policy (Adjustment/Negotiation)
     * @param policies
     *            new policies
     * @return result of the action
     */
    public int setPolicies(String policyClassType, Policy[] policies) {
        logger.info("Setting policies ");
        for (Policy policy : policies) {
            logger.info(policy.toString());
        }

        return 0;
    }

    /**
     * Get the policies used by the PAC. To be used from the business layer.
     * 
     * @param policyClassType
     *            type of policy (Adjustment/Negotiation)
     * @return policies the PAC policies
     */
    public Policy[] getPolicies(String policyClassType) {
        logger.info("Getting policies");
        return null;
    }
}
