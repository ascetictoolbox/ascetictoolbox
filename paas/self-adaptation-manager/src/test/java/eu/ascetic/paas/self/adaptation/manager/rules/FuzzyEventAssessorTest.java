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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Richard
 */
public class FuzzyEventAssessorTest {

    /**
     * Test of printFuzzyDataEvaluation method, of class FuzzyEventAssessor.
     */
    @Test
    public void testPrintFuzzyDataEvaluation() {
        try {
            System.out.println("printFuzzyDataEvaluation");
            FuzzyEventAssessor instance = new FuzzyEventAssessor();
            instance.printFuzzyDataEvaluation();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FuzzyEventAssessorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of assessEvent method, of class FuzzyEventAssessor.
     */
    @Test
    public void testAssessEvent() {
        try {
            System.out.println("assessEvent");
            EventData event = null;
            List<EventData> sequence = new ArrayList<>();
            List<Response> recentAdaptation = new ArrayList<>();
            FuzzyEventAssessor instance = new FuzzyEventAssessor();
            Response result = instance.assessEvent(event, sequence, recentAdaptation);
//            assert(result != null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FuzzyEventAssessorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getRuleFile method, of class FuzzyEventAssessor.
     */
    @Test
    public void testGetRuleFile() {
        System.out.println("getRuleFile");
        String expResult = "adapt.fcl";
        String result = FuzzyEventAssessor.getRuleFile();
        assertEquals(expResult, result);
    }

    /**
     * Test of setRuleFile method, of class FuzzyEventAssessor.
     */
    @Test
    public void testSetRuleFile() {
        System.out.println("setRuleFile");
        String ruleFile = "adapt.fcl";
        FuzzyEventAssessor.setRuleFile(ruleFile);
    }

}
