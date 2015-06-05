package eu.ascetic.paas.applicationmanager.amqp;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

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
 * Simple Amqp list listner for helping in the tests
 */
public class AmqpListListener implements MessageListener {
	List<TextMessage> textMessages = new ArrayList<TextMessage>();

	@Override
	public void onMessage(Message message) {
		TextMessage textMessage = (TextMessage) message;
		textMessages.add(textMessage);
	}

	public List<TextMessage> getTextMessages() {
		return textMessages;
	}
}

