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


package eu.ascetic.architecture.iaas.pac.events.converter;


import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import eu.ascetic.architecture.iaas.pac.events.Value;


public class ValueConverter implements Converter {

	@SuppressWarnings("unchecked")
	public boolean canConvert(final Class clazz) {
		return clazz.equals(Value.class);
	}

	
	@Override
	public void marshal(final Object valore, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final NodeWithAttribute elem = (NodeWithAttribute) valore;
		writer.addAttribute(elem.getAttributeName(), elem.getAttributeValue()); 
		writer.setValue(elem.getTextValue());
	}


	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {

        return  new Value(reader.getAttribute("id"), reader.getValue());		
		
	}
	
}
