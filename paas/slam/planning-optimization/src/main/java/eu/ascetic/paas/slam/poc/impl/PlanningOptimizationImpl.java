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
 * @author         Miguel Rojas - miguel.rojas@uni-dortmund.de
 * @version        $Rev$
 * @lastrevision   $Date$
 * @filesource     $URL$
 */

package eu.ascetic.paas.slam.poc.impl;

import java.util.List;
import org.apache.log4j.Logger;
import org.slasoi.gslam.core.context.SLAMContextAware;
import org.slasoi.gslam.core.context.SLAManagerContext;
import org.slasoi.gslam.core.context.SLAManagerContext.SLAManagerContextException;
import org.slasoi.gslam.core.negotiation.SLARegistry;
import org.slasoi.gslam.core.negotiation.INegotiation.TerminationReason;
import org.slasoi.gslam.core.negotiation.SLARegistry.IRegister;
import org.slasoi.gslam.core.negotiation.SLARegistry.InvalidUUIDException;
import org.slasoi.gslam.core.negotiation.SLARegistry.SLAState;
import org.slasoi.gslam.core.negotiation.SLARegistry.UpdateFailureException;
import org.slasoi.gslam.core.pac.ProvisioningAdjustment;
import org.slasoi.gslam.core.pac.ProvisioningAdjustment.Plan;
import org.slasoi.gslam.core.pac.ProvisioningAdjustment.PlanFormatException;
import org.slasoi.gslam.core.pac.ProvisioningAdjustment.PlanFoundException;
import org.slasoi.gslam.core.pac.ProvisioningAdjustment.PlanNotFoundException;
import org.slasoi.gslam.core.pac.ProvisioningAdjustment.Status;
import org.slasoi.gslam.core.poc.PlanningOptimization;
import org.slasoi.slamodel.primitives.UUID;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;
import eu.ascetic.paas.slam.poc.negotiation.SLAT2SLAImpl;
import eu.ascetic.paas.slam.poc.provision.PlanHandlerImpl;
import eu.ascetic.paas.slam.poc.provision.RunnerImpl;
import eu.ascetic.paas.slam.poc.replan.ReplanImpl;

/**
 * DOMAIN SPECIFIC POC
 * 
 * @author Miguel Rojas (UDO)
 * 
 */
public class PlanningOptimizationImpl implements PlanningOptimization, SLAMContextAware {

    /**
     * Initializes a newly created <code>PlanningOptimizationImpl</code> object.
     */
    public PlanningOptimizationImpl() {
        dsAssestmentAndCustomized = new DomainSpecAssessmentAndCustomize();
        dsINotification = new POCINotification();
        dsIPlanStatus = new POCIPlanStatus();
        dsIReplan = new POCIReplan();
    }

    /**
     * Gets the inner class <code>IAssessmentAndCustomize</code> instance.
     * 
     * @see #pocIAssessmentAndCustomize
     */
    public IAssessmentAndCustomize getIAssessmentAndCustomize() {
        return dsAssestmentAndCustomized;
    }

    /**
     * Gets the inner class <code>INotification</code> instance.
     * 
     * @see #pocINotification
     */
    public INotification getINotification() {
        return dsINotification;
    }

    /**
     * Gets the inner class <code>IPlanStatus</code> instance.
     * 
     * @see #pocIPlanStatus
     */
    public IPlanStatus getIPlanStatus() {
        return dsIPlanStatus;
    }

    /**
     * Gets the inner class <code>IReplan</code> instance.
     * 
     * @see #pocIReplan
     */
    public IReplan getIReplan() {
        return dsIReplan;
    }

    /**
     * Injects the infrastructure SLA manager instance into the planning and optimization context.
     * 
     * @param context
     *            The instance of infrastructure SLA manager
     */
    public void setSLAManagerContext(SLAManagerContext context) {
        this.context = context;
    }

    // ------------------------------------- INNER CLASSES ----------------------------------------
    /**
     * The <code>POCIAssessmentAndCustomize</code> class represents all the operations during SLA negotiation phase.
     */
    class DomainSpecAssessmentAndCustomize implements IAssessmentAndCustomize {
        /**
         * Customer invokes this method for creating a final agreement.
         * 
         * @param negotiationID
         *            the ID of a specific negotiation.
         * @param slaTemplate
         *            the SLA template based on which a final SLA will be created.
         */
        @SuppressWarnings("finally")
        public SLA createAgreement(String negotiationID, SLATemplate slaTemplate) {
            boolean renegotiationFlag = false;
            SLA[] infrastructureSLAs = null;
            SLA infrastructureSLA = null;
            SLA sla = null;
            try {
                SLARegistry slaRegistry = context.getSLARegistry();
                infrastructureSLAs = slaRegistry.getIQuery().getSLA(new UUID[] { new UUID(negotiationID) });
                if (infrastructureSLAs != null && infrastructureSLAs.length > 0) {
                    renegotiationFlag = true;
                    infrastructureSLA = infrastructureSLAs[0];
                    LOGGER.info("*** Infrastructure SLA found with ID = " + infrastructureSLA.getUuid().getValue());
                }

                LOGGER.info("Start to create SLA with incoming SLA template...");
                SLAT2SLAImpl trnasfer = new SLAT2SLAImpl(slaTemplate);
                sla = trnasfer.transfer();

                LOGGER.info("SLA is created successfully!");
                LOGGER.info("The SLA can be fully/partially monitored by monitoring manager.");
                LOGGER.info("Start to register SLA into SLA registry...");
                SLARegistry registry = context.getSLARegistry();
                IRegister register = registry.getIRegister();
                if (renegotiationFlag == false) {
                    register.register(sla, null, SLAState.OBSERVED);
                    LOGGER.info("SLA is registered successfully into SLA registry!");
                }
                else {
                    LOGGER.info("Before updating own SLA in SLA registry.");
                    register.update(infrastructureSLA.getUuid(), sla, null, SLAState.OBSERVED);
                    LOGGER.info("Own SLA updated successfully in SLA registry.");
                }

            }
            catch (Exception e) {
                LOGGER.debug(e);
            }
            finally {
                return sla;
            }
        }

        /**
         * Customer invokes this method for starting a negotiation
         * 
         * @param negotiationID
         *            the ID of a specific negotiation.
         * @param slaTemplate
         *            the counter-offer from customer side that service provider has to analyze.
         */
        public SLATemplate[] negotiate(String negotiationID, SLATemplate slaTemplate) {
            assert (negotiationID != null && !negotiationID.equals("") && slaTemplate != null) : "negotiate method requires an negotiationID != null or not empty and an slaTemplate != null.";
            return new SLATemplate[] { (new RunnerImpl(slaTemplate, context)).run() };
        }

        /**
         * Customer invokes this method for starting provisioning
         * 
         * @param slaID
         *            the UUID of a specific SLA.
         */
        public SLA provision(UUID slaID) {
            assert (slaID != null && slaID.getValue() != null && !slaID.getValue().equals("")) : "provision method requires an slaID != null or not empty";
            return new SLA();
        }

        /**
         * Customer invokes this method for canceling provisioning
         * 
         * @param arg0
         *            the UUID of a specific SLA.
         * @param arg1
         *            the reason for canceling the SLA.
         */
        public boolean terminate(UUID arg0, List<TerminationReason> arg1) {
            assert (arg0 != null && arg0.getValue() != null && !arg0.getValue().equals("") && arg1 != null && arg1
                    .size() >= 1) : "it requires a UUID != null or not empty and termination reason not null and the size of arg1 list is greater or equal to 1.";
            SLARegistry registry = null;
            SLA infSLAToTerminate = null;
            try {
                registry = context.getSLARegistry();
                infSLAToTerminate = registry.getIQuery().getSLA(new UUID[] { arg0 })[0];
                // no dependencies
                registry.getIRegister().update(arg0, infSLAToTerminate, null, SLAState.EXPIRED);
            }
            catch (SLAManagerContextException e) {
                e.printStackTrace();
            }
            catch (UpdateFailureException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (InvalidUUIDException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            ProvisioningAdjustment pac;
            try {
                pac = context.getProvisioningAdjustment();
                pac.cancelExecution(infSLAToTerminate.getPropertyValue(PlanHandlerImpl.PLAN_ID_SLA));
                return true;
            }
            catch (SLAManagerContextException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
            catch (PlanNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * The <code>POCINotification</code> class represents all the operations during SLA provisioning phase.
     * 
     */
    class POCINotification implements INotification {
        /**
         * Starts provisioning
         * 
         * @param newSLA
         *            the object of a specific SLA.
         */
        public void activate(SLA newSLA) {
            assert (newSLA != null) : "it requires an SLA != null.";
            try {
                PlanHandlerImpl planHandler= new PlanHandlerImpl(newSLA);
                Plan plan = planHandler.planMaker();
                if (plan != null) {
                    ProvisioningAdjustment pac = context.getProvisioningAdjustment();
                    pac.executePlan(plan);
                }
                else {
                    LOGGER
                            .error("Infrastructure does not have enough resources while querying, plan can not be executed.");
                }
            }
            catch (SLAManagerContextException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (PlanFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (PlanFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * The <code>POCIPlanStatus</code> class represents the operations related to plan status during SLA provisioning
     * phase.
     */
    class POCIPlanStatus implements IPlanStatus {

        /**
         * Accepts the current status of a plan given its ID
         * 
         * @param planId
         *            the ID of a specific plan.
         * @param status
         *            the status of a specific plan.
         */
        public void planStatus(String planId, Status status) {
            assert (planId != null && !planId.equals("") && status != null) : "it requires a planId != null or not empty and plan status not null.";
            // TODO Auto-generated method stub
        }
    }

    /**
     * The <code>POCIReplan</code> class represents the re-plan operations during SLA provisioning phase.
     * 
     */
    class POCIReplan implements IReplan {

        /**
         * Accepts the analysis of a specific plan
         * 
         * @param planId
         *            the ID of a specific plan.
         * @param analysis
         *            the reason for re-plan.
         */
        public void rePlan(String uuid, String analysis) {
            assert (uuid != null && !uuid.equals("") && analysis != null && !analysis.equals("")) : "it requires a planId != null or not empty and analysis not null or not empty.";
            ReplanImpl replan = new ReplanImpl();
            UUID id = new UUID("");
            replan.rePlan(id);
            // TODO Auto-generated method stub
        }
    }

    protected IAssessmentAndCustomize dsAssestmentAndCustomized;
    protected INotification dsINotification;
    protected IPlanStatus dsIPlanStatus;
    protected IReplan dsIReplan;

    protected SLAManagerContext context;
    private static final Logger LOGGER = Logger.getLogger(PlanningOptimizationImpl.class);
}
