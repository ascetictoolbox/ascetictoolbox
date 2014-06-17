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

package eu.ascetic.architecture.iaas.slamanager.pac.events;

import org.apache.log4j.Logger;
import org.slasoi.gslam.pac.EventTranslator;
import org.slasoi.gslam.pac.events.Message;

import com.thoughtworks.xstream.XStream;

import eu.ascetic.architecture.iaas.slamanager.pac.events.converter.NodeWithAttributeConverter;
import eu.ascetic.architecture.iaas.slamanager.pac.events.converter.ValueConverter;


public class ViolationMessageTranslator extends EventTranslator {

	private static final Logger logger = Logger.getLogger(ViolationMessageTranslator.class.getName());

    public ViolationMessageTranslator() {
    }

    
    public Message fromXML(String messageStr) {
        logger.debug("ViolationAgentMessage from XML");
        logger.debug(messageStr);

        XStream xstream = new XStream();
        
        //xstream.processAnnotations(ViolationMessage.class);
        xstream.registerConverter(new eu.ascetic.architecture.iaas.slamanager.pac.events.converter.DateTimeConverter());
        
        xstream.registerConverter(new ValueConverter());
        
        xstream.alias(ViolationMessage.class.getSimpleName(), ViolationMessage.class);
        xstream.useAttributeFor(ViolationMessage.class, "sid");
        xstream.useAttributeFor(ViolationMessage.class, "ceeId");
        xstream.useAttributeFor(ViolationMessage.class, "vsName");
        xstream.useAttributeFor(ViolationMessage.class, "vmName");

        return  (Message) xstream.fromXML(messageStr);
    }

    
    public String toXML(Message message) {
        logger.debug("ViolationAgentMessage to XML");
        logger.debug(message);

        XStream xstream = new XStream();
        //xstream.processAnnotations(ViolationMessage.class);
        
        xstream.registerConverter(new eu.ascetic.architecture.iaas.slamanager.pac.events.converter.DateTimeConverter());
        
        xstream.registerConverter(new ValueConverter());

        xstream.useAttributeFor(ViolationMessage.class, "sid");
        xstream.useAttributeFor(ViolationMessage.class, "ceeId");
        xstream.useAttributeFor(ViolationMessage.class, "vsName");
        xstream.useAttributeFor(ViolationMessage.class, "vmName");

        xstream.alias(ViolationMessage.class.getSimpleName(), ViolationMessage.class);

        String messageStr = xstream.toXML(message);
        logger.debug(messageStr);

        return messageStr;
    }

}
