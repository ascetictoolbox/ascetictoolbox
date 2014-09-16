package eu.ascetic.paas.applicationmanager.model;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * @email david.garciaperez@atos.net 
 * 
 * Collection of variables to maintain equal through the entire application
 */

public class Dictionary {
	public static final String APPLICATION_MANAGER_NAMESPACE = "http://application_manager.ascetic.eu/doc/schemas/xml";
	
	public static String APPLICATION_STATUS_SUBMITTED = "SUBMITTED";
	public static String APPLICATION_STATUS_NEGOTIATION = "NEGOTIATION";
	public static String APPLICATION_STATUS_NEGOTIATING = "NEGOTIATING";		
	public static String APPLICATION_STATUS_NEGOTIATIED = "NEGOTIATED";
	public static String APPLICATION_STATUS_CONTEXTUALIZATION = "CONTEXTUALIZATION";
	public static String APPLICATION_STATUS_CONTEXTUALIZING = "CONTEXTUALIZING";
	public static String APPLICATION_STATUS_CONTEXTUALIZED = "CONTEXTUALIZED";
	public static String APPLICATION_STATUS_DEPLOYING = "DEPLOYING";
	public static String APPLICATION_STATUS_DEPLOYED = "DEPLOYED";
	public static String APPLICATION_STATUS_TERMINATED = "TERMINATED";
	public static String APPLICATION_STATUS_ERROR = "ERROR";
}
