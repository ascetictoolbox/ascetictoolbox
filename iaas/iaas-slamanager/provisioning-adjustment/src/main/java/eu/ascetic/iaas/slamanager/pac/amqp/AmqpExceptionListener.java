package eu.ascetic.iaas.slamanager.pac.amqp;



import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.apache.log4j.Logger;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net
 * 
 * Basic Exception handling for Amqp client exceptions. 
 */
public class AmqpExceptionListener implements ExceptionListener {
	private static Logger logger = Logger.getLogger(AmqpExceptionListener.class);
	
	@Override
	public void onException(JMSException exception) {
		logger.info("Connection ExceptionListener fired, exiting.");
		logger.info(exception.getMessage());
		exception.printStackTrace(System.out);
	}
}