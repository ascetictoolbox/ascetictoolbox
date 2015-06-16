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

import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.EventData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This takes event data and performs aggregation functions on it, with the aim
 * of determining trends in the data. i.e. if events raw values are getting
 * worse, better or staying the same.
 * @author Richard Kavanagh
 */
public class EventDataAggregator {

    /**
     * This is a basic trend analysis that looks at the first and last data point
     * values in order to determine the direction in which the trend is going.
     * @param eventData The list of events in which to perform the analysis on
     * @return The direction and magnitude of event data.
     */
    public static double analyseEventData(List<EventData> eventData) {
        if (eventData.isEmpty()) {
            //This is a error case!
            return Double.NaN;
        } else if (eventData.size() == 1) {
            //Too few datapoints for any trend analysis
            return 0.0;
        } else if (eventData.size() > 2) {
            //Further analysis
            EventData earliest = eventData.get(0);
            EventData latest = eventData.get(eventData.size() -1);
            return EventData.getChangeInSlack(earliest, latest);
        }
        return Double.NaN; //This is a error case!
    }
    
    /**
     * This takes a list of events and provides a list of events for a 
     * single guarantee of a named SLA.
     * @param events The list of events to filter
     * @param slaUuid The SLA identifier to filter against
     * @param guaranteeid The guarantee id to filter against.
     * @return The list of events associated with a given guarantee of an SLA, in
     * ascending chronological order. i.e. earliest first.
     */
    public static List<EventData> filterEventData(List<EventData> events, String slaUuid, String guaranteeid) {
        ArrayList<EventData> answer = new ArrayList<>();
        for (EventData eventData : events) {
            if (eventData.getSlaUuid().equals(slaUuid) && eventData.getGuaranteeid().equals(guaranteeid)) {
                answer.add(eventData);
            }
        }
        Collections.sort(answer);
        return answer;
    }
    
    /**
     * This takes a list of events and provides a list of events for a 
     * single named SLA.
     *
     * @param events The list of event records to filter
     * @param slaUuid The SLA identifier to filter against
     * @return The list of responses associated with a given SLA,in ascending
     * chronological order. i.e. earliest first.
     */
    public static List<EventData> filterResponseHistory(List<EventData> events,
            String slaUuid) {
        ArrayList<EventData> answer = new ArrayList<>();
        for (EventData event : events) {
            if (event.getSlaUuid().equals(slaUuid)) {
                answer.add(event);
            }
        }
        Collections.sort(answer);
        return answer;
    }    
    
    /**
     * This takes a list of event data and filters out old data.
     * @param eventList The list of events to filter
     * @param ageSeconds The time in seconds to allow data entry points for
     * @return The list of events in ascending chronological order. 
     * i.e. earliest first, that meet the time criteria.
     */
    public static List<EventData> filterEventDataByTime(List<EventData> eventList, int ageSeconds) {
        ArrayList<EventData> answer = new ArrayList<>();
        long now = System.currentTimeMillis();
        now = now / 1000;
        long filterTime = now - ageSeconds;
        for (EventData eventData : eventList) {
            if (eventData.getTime() >= filterTime) {
                answer.add(eventData);
            }
        }
        Collections.sort(answer);
        return answer;        
    }
    
}
