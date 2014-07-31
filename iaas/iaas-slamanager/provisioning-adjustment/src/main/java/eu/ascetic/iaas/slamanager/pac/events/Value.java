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


package eu.ascetic.iaas.slamanager.pac.events;

import eu.ascetic.iaas.slamanager.pac.events.converter.NodeWithAttribute;


public class Value implements NodeWithAttribute{

	private String id;

	private String text;
	
	public Value(String id, String text) {
		this.id = id;
		this.text = text;
	}

	@Override
	public String getAttributeValue() {
		return id;
	}

	@Override
	public String getAttributeName() {
		return "id";
	}

	@Override
	public String getTextValue() {
		return text;
	}
	
}