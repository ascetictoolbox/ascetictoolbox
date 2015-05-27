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

import java.io.FileNotFoundException;
import java.util.List;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Rule;

/**
 * This event assessor when faced with deciding if corrective action should be
 * taken will use fuzzy logic.
 *
 * @author Richard Kavanagh
 */
public class FuzzyEventAssessor extends AbstractEventAssessor {

    private static String ruleFile = "adapt.fcl";
    private FIS fis = null;

    /**
     * This creates a new event assessor that utilises fuzzy logic.
     * @throws java.io.FileNotFoundException In cases where the rules file is not
     * found.
     */
    public FuzzyEventAssessor() throws FileNotFoundException {
        fis = FIS.load(ruleFile, true);
        if (fis == null) { // Error while loading?
            throw new FileNotFoundException("Can't load file: '" + ruleFile + "'");
        }        
    }

    /**
     * Material online that is useful may be found at: Code:
     * http://jfuzzylogic.sourceforge.net/html/example_java.html
     * http://jfuzzylogic.sourceforge.net/html/java.html
     *
     * Fuzzy Language: http://jfuzzylogic.sourceforge.net/html/example_fcl.html
     * http://jfuzzylogic.sourceforge.net/html/fcl.html
     * http://jfuzzylogic.sourceforge.net/html/example_fcl.html
     */
    
    /**
     * This prints out the current fuzzy data for analysis.
     */
    public void printFuzzyDataEvaluation() {
        System.out.println("----------------- Membership -----------------");
        System.out.println("Low: " + fis.getVariable("urgency").getMembership("low"));
        System.out.println("Medium: " + fis.getVariable("urgency").getMembership("medium"));
        System.out.println("High: " + fis.getVariable("urgency").getMembership("immediate"));
        System.out.println("----------------- Urgency Output variable -----------------");
        double answer = fis.getVariable("urgency").getLatestDefuzzifiedValue();
        System.out.println("Answer: " + answer);
        System.out.println("----------------- FIS -----------------");
        System.out.println(fis);
        System.out.println("----------------- Rule Block -----------------");
        //http://jfuzzylogic.sourceforge.net/html/faq.html
        for (Rule rule : fis.getFunctionBlock("adaptor").getFuzzyRuleBlock("No1").getRules()) {
            System.out.println(rule);
        }
    }
    
    @Override
    public Response assessEvent(EventData event, List<EventData> sequence) {
        Response answer = null;
        List<EventData> eventData = EventDataAggregator.filterEventData(sequence, event.getSlaUuid(), event.getGuaranteeid());
        eventData = EventDataAggregator.filterEventDataByTime(eventData, 120); //2mins
        double trendValue = EventDataAggregator.analyseEventData(eventData);
        fis.setVariable("currentDifference", event.getDeviationBetweenRawAndGuarantee());
        fis.setVariable("trendDifference", trendValue);
        fis.evaluate();
        for (Rule rule : fis.getFunctionBlock("adaptor").getFuzzyRuleBlock("No1").getRules()) {
            System.out.println(rule);
            if (rule.getDegreeOfSupport() == 1.0) {
                //This creates the new response
                answer = new Response(null, null); 
                return answer;
            }
        }
        return answer;
    }    

    /**
     * This indicates which file to load in order to get the fuzzy logic rules
     * that are required for adaptation.
     *
     * @return The path and name of the rules file.
     */
    public static String getRuleFile() {
        return ruleFile;
    }

    /**
     * This sets the file to load in order to get the fuzzy logic rules that are
     * required for adaptation.
     *
     * @param ruleFile The path and name of the rules file to load.
     */
    public static void setRuleFile(String ruleFile) {
        FuzzyEventAssessor.ruleFile = ruleFile;
    }

}
