/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.paas.self.adaptation.manager.rules;

import eu.ascetic.paas.slam.pac.events.ViolationMessage;
import eu.ascetic.paas.slam.pac.events.ViolationMessageTranslator;
import org.slasoi.gslam.pac.events.Message;

/**
 * This class converts from the SLA manager basic types into the Self-adaptation
 * managers basic types.
 *
 * @author Richard Kavanagh
 */
public class EventDataConverter {

    /**
     * This converts from an XML representation of an event into the internal
     * representation of the event.
     * @param eventXml The event in XML format
     * @return The object representation of the event
     */
    public static EventData convertEventData(String eventXml) {
        Message message = new ViolationMessageTranslator().fromXML(eventXml);
        return convertEventData((ViolationMessage) message);
    }
    
    
    /**
     * @param event The incoming event to convert into the self-adaptation
     * managers internal representation
     * @return The converted internal representation of events
     * @see eu.ascetic.paas.slam.pac.events.ViolationMessage
     */
    public static EventData convertEventData(ViolationMessage event) {
        EventData answer = new EventData();
        answer.setTime(event.getTime().getTimeInMillis() / 1000);
        if (event.getAlert().getType().equals("violation")) {
            answer.setType(EventData.Type.SLA_BREACH);
        } else {
            answer.setType(EventData.Type.WARNING);
        }
        answer.setRawValue(Double.parseDouble(event.getValue().getTextValue()));
        answer.setGuranteedValue(event.getAlert().getSlaGuaranteedState().getGuaranteedValue());
        answer.setGuranteeOperator(getOperator(event));

        answer.setSlaUuid(event.getAlert().getSlaUUID());
        answer.setGuaranteeid(event.getAlert().getSlaGuaranteedState().getGuaranteedId());
        return answer;

    }

    /**
     * This converts a violation messages guarantee state's operator into an 
     * event type operator.
     * @param event The event type to convert.
     * @return The enumerated type for the operator
     */
    public static EventData.Operator getOperator(ViolationMessage event) {
        switch (event.getAlert().getSlaGuaranteedState().getOperator()) {
            case "less_than_or_equals":
                return EventData.Operator.LTE;
            case "less_than":
                return EventData.Operator.LT;
            case "equals":
                return EventData.Operator.EQ;
            case "greater_than_or_equals":
                return EventData.Operator.GTE;
            case "greater_than":
                return EventData.Operator.GT;
        }

        return EventData.Operator.LTE;
    }

}