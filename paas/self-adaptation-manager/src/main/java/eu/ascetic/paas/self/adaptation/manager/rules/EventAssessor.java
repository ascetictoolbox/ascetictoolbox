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

import java.util.List;

/**
 * This is the standard interface for all event assessors.
 * @author Richard Kavanagh
 */
public interface EventAssessor {

    /**
     * This assesses an event and decides if a response is required. If no
     * response is required then null is returned.
     * @param event The SLA event to assess
     * @param sequence The sequence of similar events that have been assessed.
     * @return A response object in cases where an adaptive response is required.
     */
    public Response assessEvent(EventData event, List<EventData> sequence);
    
}
