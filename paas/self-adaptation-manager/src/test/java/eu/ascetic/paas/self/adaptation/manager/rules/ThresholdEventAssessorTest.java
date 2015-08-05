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
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.util.List;
import java.util.ArrayList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Richard
 */
public class ThresholdEventAssessorTest {
    
    public ThresholdEventAssessorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of assessEvent method, of class ThresholdEventAssessor.
     */
    @Test
    public void testAssessEvent() {
        System.out.println("assessEvent");
        EventData event = new EventData(0, 10, 4, EventData.Type.SLA_BREACH, 
                EventData.Operator.GT, "SLAID", "AppID", "DeplyID", "GurID", "power_usage_per_app");
        List<EventData> sequence = new ArrayList<>();
        List<Response> recentAdaptation = new ArrayList<>();
        ThresholdEventAssessor instance = new ThresholdEventAssessor();
        instance.setThreshold(0);
        Response result = instance.assessEvent(event, sequence, recentAdaptation);
        assert(result != null);
        assert(result.getActionType().equals(Response.AdaptationType.REMOVE_VM));
    }
    
}
