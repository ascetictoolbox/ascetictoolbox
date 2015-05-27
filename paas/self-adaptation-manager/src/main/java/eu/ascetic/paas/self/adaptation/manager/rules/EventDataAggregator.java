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
     * @param eventList The list of events in which to perform the analysis on
     * @param slaUuid The SLA identifier to perform analysis against
     * @param guaranteeid The guarantee id to perform analysis against
     * @return The direction and magnitude of event data.
     */
    public static double analyseEventData(List<EventData> eventList, String slaUuid, int guaranteeid) {
        List<EventData> eventData = filterEventData(eventList, slaUuid, guaranteeid);
        if (eventData.isEmpty()) {
            //This is a error case!
            return Double.NaN;
        } else if (eventData.size() == 1) {
            //Too few datapoints for any trend analysis
            return Double.NaN;
        } else if (eventData.size() > 2) {
            //Further analysis
            EventData earliest = eventData.get(0);
            EventData latest = eventData.get(eventData.size() -1);
            return EventData.getChangeInSlack(earliest, latest);
        }
        return Double.NaN;
    }
    
    /**
     * This takes a list of event data and filters out the irrelevant other
     * event data.
     * @param eventList The list of events to filter
     * @param slaUuid The SLA identifier to filter against
     * @param guaranteeid The guarantee id to filter against.
     * @return The list of events associated with a given guarantee of an SLA, in
     * ascending chronological order. i.e. earliest first.
     */
    public static List<EventData> filterEventData(List<EventData> eventList, String slaUuid, int guaranteeid) {
        ArrayList<EventData> answer = new ArrayList<>();
        for (EventData eventData : eventList) {
            if (eventData.getSlaUuid().equals(slaUuid) && eventData.getGuaranteeid() == guaranteeid) {
                answer.add(eventData);
            }
        }
        Collections.sort(answer);
        return answer;
    }
    
    
}
