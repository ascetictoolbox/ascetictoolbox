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

package eu.ascetic.architecture.iaas.slamanager.pac.action;

import org.apache.log4j.Logger;
import org.slasoi.slamodel.sla.Invocation;

import eu.ascetic.architecture.iaas.slamanager.pac.ProvisioningAdjustmentImpl;

public class ViolationAction extends Invocation{

	/**
	 * 
	 */
	private static Logger logger = Logger.getLogger(ViolationAction.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	public ViolationAction(){
	}

	public String provaMetodo(String msg){
		return msg+" Modificato";
	}
		
	public void manageADD(String appID, String virtualSystem, int number){
		logger.debug("Call manage to PM");
		logger.debug("AppId:"+appID+" , virtualSystem:"+virtualSystem+" , action:"+"ADD"+" , number:"+number);
		//String result=ProvisioningAdjustmentImpl.getPacToPm().manageADD(appID, virtualSystem,number);
		logger.debug("============  Action Invoked!!! ============");
		//logger.debug("Result:"+result);
	}	

}
