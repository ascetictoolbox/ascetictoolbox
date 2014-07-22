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

package eu.ascetic.paas.slam.main.beans;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.slasoi.gslam.core.builder.PlanningOptimizationBuilder;
import org.slasoi.gslam.core.builder.ProvisioningAdjustmentBuilder;
import org.slasoi.gslam.core.context.GenericSLAManagerServices;
import org.slasoi.gslam.core.context.GenericSLAManagerServices.SLAMConfiguration;
import org.slasoi.gslam.core.context.GenericSLAManagerUtils;
import org.slasoi.gslam.core.context.SLAMContextAware;
import org.slasoi.gslam.core.context.SLAManagerContext;
import org.slasoi.gslam.core.pac.ProvisioningAdjustment;
import org.slasoi.gslam.core.poc.PlanningOptimization;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.extensions.annotation.ServiceReference;

public class SkeletonSLAMBean implements BundleContextAware {
    public void start() {
        try {
            // the slam configuration file should be located under SLASOI_HOME directory. (see details in integration
            // approach)
            SLAMConfiguration sklConfig = gslamServices.loadConfigurationFrom("ascetic.paas.instance1.cfg");

            slamContext =
                    gslamServices.createContext(osgiContext, sklConfig.name, sklConfig.epr, sklConfig.group,
                            sklConfig.wsPrefix);

            // Inject the POC into the slamContext
            slamContext.setPlanningOptimization(nnPOC);
            injectIntoContext(nnPOC, slamContext);

            // Inject the PAC into the slamContext
            slamContext.setProvisioningAdjustment(nnPAC);
            injectIntoContext(nnPAC, slamContext);

            LOGGER.info("\n\n \t*** :: start :: gslamServices    >> \n" + gslamServices);
            LOGGER.info("\n\n \t*** :: start :: SkeletonSLAMBean >> \n" + slamContext);
        }
        catch (Exception e) {
            LOGGER.debug(e);
            e.printStackTrace();
        }
    }

    public void stop() {
    }

    public void setBundleContext(BundleContext osgiContext) {
        this.osgiContext = osgiContext;
    }

    protected void injectIntoContext(Object obj, SLAManagerContext context) {
        if (obj instanceof SLAMContextAware) {
            ((SLAMContextAware) obj).setSLAManagerContext(context);
        }
    }

    @ServiceReference
    public void setGslamServices(GenericSLAManagerServices gslamServices) {
        LOGGER.info("generic-slam injected successfully into slam-slam");
        this.gslamServices = gslamServices;
    }

    @ServiceReference
    public void setGslamUtils( GenericSLAManagerUtils utils )
    {
        LOGGER.info( "generic-slam-utils injected successfully into slam-slam" );
        this.gslamUtils = utils;
    }
    
    @ServiceReference(filter = "(proxy=slam-poc)")
    public void setPOC(PlanningOptimizationBuilder builder) {
        LOGGER.info("slam-POC created and injected successfully into slam-slam");

        this.nnPOC = builder.create();
    }

    @ServiceReference(filter = "(proxy=slam-pac)")
    public void setPAC(ProvisioningAdjustmentBuilder builder) {
        LOGGER.info("slam-PAC created and injected successfully into slam-slam");
        this.nnPAC = builder.create();
    }

    protected SLAManagerContext         slamContext;
    protected GenericSLAManagerServices gslamServices;
    protected GenericSLAManagerUtils    gslamUtils;

    protected PlanningOptimization nnPOC;
    protected ProvisioningAdjustment nnPAC;

    protected BundleContext osgiContext;

    private static final Logger LOGGER = Logger.getLogger(SkeletonSLAMBean.class);
}
