/**
 * Copyright 2012 Hewlett-Packard Development Company, L.P.                
 *                                                                          
 * Licensed under the Apache License, Version 2.0 (the "License");         
 * you may not use this file except in compliance with the License.        
 * You may obtain a copy of the License at                                 
 *                                                                          
 *     http://www.apache.org/licenses/LICENSE-2.0                          
 *                                                                          
 * Unless required by applicable law or agreed to in writing, software     
 * distributed under the License is distributed on an "AS IS" BASIS,       
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and     
 * limitations under the License.                                          
 */

package eu.ascetic.iaas.slamanager.pac.action;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.slasoi.slamodel.primitives.Expr;
import org.slasoi.slamodel.primitives.ID;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.service.Interface.Specification;
import org.slasoi.slamodel.sla.AgreementTerm;
import org.slasoi.slamodel.sla.Guaranteed;
import org.slasoi.slamodel.sla.Guaranteed.Action;
import org.slasoi.slamodel.sla.Guaranteed.Action.Defn;
import org.slasoi.slamodel.sla.InterfaceDeclr;
import org.slasoi.slamodel.sla.Invocation;
import org.slasoi.slamodel.sla.SLA;

import eu.ascetic.iaas.slamanager.pac.ProvisioningAdjustmentImpl;

public class ActionInvocation {

	private static Logger logger = Logger.getLogger(ActionInvocation.class.getName());

	private String appUUID;
	private String number;
	private String virtualSystem;
	private SLA sla;

	private String OVF_basePath;
	private Properties properties;
	private String ovfConfigurationFile;

	protected static String MANAGE_ADD_OPERATION_NAME = "manageADD";

	protected static String MANAGE_DEL_OPERATION_NAME = "manageDEL";

	protected static String OVF_REPO_PATH = "ovf_repo_path";

	public String getOvfConfigurationFile() {
		return ovfConfigurationFile;
	}

	public void setOvfConfigurationFile(String ovfConfigurationFile) {
		this.ovfConfigurationFile = ovfConfigurationFile;
	}

	public void init() {
		logger.debug("Initializing Action Invocation...");
		appUUID = null;
		number = null;
		properties = new Properties();
		ovfConfigurationFile = ProvisioningAdjustmentImpl.getConfigurationFileImpl();
		OVF_basePath = System.getenv("SLASOI_HOME") + System.getProperty("file.separator");
		try {
			properties.load(new FileInputStream(OVF_basePath + ovfConfigurationFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//ProvisioningAdjustmentImpl.getPacToPm().init();
	}

	public void invokeActionFromSLA(SLA sla, String agreementTerm, String guaranteedId) {
		logger.debug("Invoking action based on agreementTerm: " + agreementTerm + " and guaranteedId: " + guaranteedId);
		Class actionClass = null;
		this.sla = sla;
		appUUID=sla.getPropertyValue(new STND("AppUUID"));
		for (AgreementTerm at : sla.getAgreementTerms()) {
			if (at.getId().getValue().equalsIgnoreCase(agreementTerm)) {
				for (Guaranteed g : at.getGuarantees()) {
					if (g instanceof Action) {
						Action action = (Action) g;
						Expr[] precond = action.getPrecondition().getParameters();
						for (int l = 0; l < precond.length; l++) {
							if ((((ID) precond[l]).getValue()).equalsIgnoreCase(guaranteedId)) {
								Defn upperAction = action.getPostcondition();
								if (upperAction instanceof Invocation) {
									Invocation localAction = (Invocation) action.getPostcondition();
									String endpoint = getEndpoint(localAction.getEndpointId().getValue());
									logger.debug("Action to call: " + endpoint);
									// value is in the format:
									// "InterfaceId/Operation"
									String[] s = (localAction.getOperationId().getValue()).split("/");
									String operation = s[1];
									logger.debug("Operation to call: " + operation);
									// allowed methods: "manageADD","manageDEL"
									if (operation.equals(MANAGE_DEL_OPERATION_NAME) || operation.equals(MANAGE_ADD_OPERATION_NAME)) {
										ID[] ids = localAction.getParameterKeys();
										for (int k = 0; k < ids.length; k++) {
											if (ids[k].getValue().equals("Number")) {
												number = ((ID) localAction.getParameterValue(ids[k])).getValue();
											}
											if (ids[k].getValue().equals("VirtualSystem")) {
												virtualSystem = ((ID) localAction.getParameterValue(ids[k])).getValue();
											}
										}
										try {
											actionClass = Class.forName(endpoint);
											Object actionInstance = actionClass.newInstance();
											Method myMethod = actionClass.getMethod(operation, new Class[] { String.class, String.class, int.class });
											String returnValue = (String) myMethod.invoke(actionInstance, new Object[] { appUUID, virtualSystem, new Integer(number).intValue() });
										} catch (SecurityException e) {
											e.printStackTrace();
										} catch (IllegalArgumentException e) {
											e.printStackTrace();
										} catch (ClassNotFoundException e) {
											e.printStackTrace();
										} catch (InstantiationException e) {
											e.printStackTrace();
										} catch (IllegalAccessException e) {
											e.printStackTrace();
										} catch (NoSuchMethodException e) {
											e.printStackTrace();
										} catch (InvocationTargetException e) {
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
				}
				break;
			}
		}
	}

	private String getEndpoint(String value) {
		// value is in the format: "InterfaceId/EndpointId"
		String[] splitted = value.split("/");
		InterfaceDeclr interfDec = sla.getInterfaceDeclr(splitted[0]);
		return ((Specification) interfDec.getInterface()).getName();
	}

}
