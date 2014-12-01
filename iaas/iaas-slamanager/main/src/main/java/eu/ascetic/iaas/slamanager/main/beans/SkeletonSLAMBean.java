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
 * @author         Miguel Rojas - miguel.rojas@uni-dortmund.de
 * @version        $Rev$
 * @lastrevision   $Date$
 * @filesource     $URL$
 */

package eu.ascetic.iaas.slamanager.main.beans;

import java.util.Hashtable;

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

import eu.ascetic.iaas.slamanager.main.beans.client.NegotiationHelper;

public class SkeletonSLAMBean implements BundleContextAware {

	public SkeletonSLAMBean() {
	}

	public void start() {
		try {
			LOGGER.info("Loading configuration");

			INSTANCES++;
			SLAMConfiguration sklConfig = gslamServices.loadConfigurationFrom("ascetic.instance1.cfg");

			LOGGER.debug("Config: name=" + sklConfig.name);
			LOGGER.debug("Config: epr=" + sklConfig.epr);
			LOGGER.debug("Config: group=" + sklConfig.group);
			LOGGER.debug("Config: wsPrefix=" + sklConfig.wsPrefix);

			slamContext = gslamServices.createContext(osgiContext, sklConfig.name + "-" + INSTANCES, sklConfig.epr, sklConfig.group, sklConfig.wsPrefix);
			slamContext.setProperties(sklConfig.properties);

			// Inject the POC into the slamContext
			slamContext.setPlanningOptimization(nnPOC);

			injectIntoContext(nnPOC, slamContext);

			// Inject the PAC into the slamContext
			slamContext.setProvisioningAdjustment(nnPAC);
			injectIntoContext(nnPAC, slamContext);

			LOGGER.info("\n\n \t*** :: start :: gslamServices    >> \n" + gslamServices);
			LOGGER.info("\n\n \t*** :: start :: SkeletonSLAMBean >> \n" + slamContext);
		} catch (Exception e) {
			LOGGER.debug(e);
			e.printStackTrace();
		}
	}

	public void stop() {
	}

	public void setBundleContext(BundleContext osgiContext) {
		assert (osgiContext != null) : "The OSGi context of ascetic SLA manager != null.";
		this.osgiContext = osgiContext;

		AsceticSlamTracer tracer = new AsceticSlamTracer();
		osgiContext.registerService(tracer.getClass().getName(), tracer, null);
	}

	/**
	 * Injects the domain specific components into SLA manager context. e.g.,
	 * IPOC and IPAC.
	 */
	protected void injectIntoContext(Object obj, SLAManagerContext context) {
		assert (context != null && obj != null) : "The context of ascetic SLA manager != null and the object that is injecting into SLAM context !=null.";
		if (obj instanceof SLAMContextAware) {
			((SLAMContextAware) obj).setSLAManagerContext(context);
		}
	}

	/**
	 * Sets the generic SLA manager services.
	 */
	@ServiceReference
	public void setGslamServices(GenericSLAManagerServices gslamServices) {
		assert (gslamServices != null) : "Generic SLAM services != null.";
		LOGGER.info("generic-slam injected successfully into ascetic-slam");
		this.gslamServices = gslamServices;
	}

	@ServiceReference
	public void setGslamUtils(GenericSLAManagerUtils utils) {
		LOGGER.info("generic-slam-utils injected successfully into ascetic-slam");
		this.gslamUtils = utils;
	}

	/**
	 * Sets IPOC.
	 */
	@ServiceReference(filter = "(proxy=iaas-poc)")
	public void setPOC(PlanningOptimizationBuilder pocBuilder) {
		LOGGER.info("is-POC injected successfully into ascetic-slam");
		assert (pocBuilder != null) : "PlanningOptimizationBuilder instance != null.";
		this.nnPOC = pocBuilder.create();
	}

	/**
	 * Sets IPAC.
	 */
	@ServiceReference(filter = "(proxy=iaas-pac)")
	public void setPAC(ProvisioningAdjustmentBuilder pacBuilder) {
		LOGGER.info("ascetic-PAC injected successfully into ascetic-slam");
		assert (pacBuilder != null) : "ProvisioningAdjustmentBuilder instance != null.";
		this.nnPAC = pacBuilder.create();
	}

	protected SLAManagerContext slamContext;
	protected GenericSLAManagerServices gslamServices;
	protected GenericSLAManagerUtils gslamUtils;

	protected PlanningOptimization nnPOC;
	protected ProvisioningAdjustment nnPAC;

	protected BundleContext osgiContext;

	private static int INSTANCES = 0;
	private static final Logger LOGGER = Logger.getLogger(SkeletonSLAMBean.class);

	public class AsceticSlamTracer {
		/**
		 * method to be invoked from osgi-console via 'echo' command
		 */
		public void context() {
			System.out.println(slamContext);
		}

		/**
		 * method to be invoked from osgi-console via 'echo' command
		 */
		public void slamID() {
			try {
				System.out.println("\t\t SLAM-ID = " + slamContext.getSLAManagerID());
			} catch (Exception e) {
			}
		}

		/**
		 * method to be invoked from osgi-console via 'invoke' command
		 */
		public void values(Hashtable<String, String> params) {
			assert (params != null) : "SLAM-params != null.";
			System.out.println("\t\t SLAM-params = " + params);
		}

		/**
		 * method to test Negotiation
		 */
		public void test() {
			try {
				NegotiationHelper helper = new NegotiationHelper();
				helper.run(slamContext);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
