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

package eu.ascetic.architecture.iaas.slamanager.poc.exceptions;

public class RequestNotCorrectException extends Exception {
	/**
	 * The <code>RequestNotCorrectException</code> exception class represents
	 * the incoming request is either null or incorrect format.
	 * 
	 * @author Kuan Lu
	 */
	private static final long serialVersionUID = 1L;

	public RequestNotCorrectException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
}
