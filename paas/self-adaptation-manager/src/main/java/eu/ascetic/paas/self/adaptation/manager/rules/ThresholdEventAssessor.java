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

import eu.ascetic.ioutils.ResultsStore;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.EventData;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.FiringCriteria;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This event assessor examines events and as soon as a breach occurs, n times
 * it reacts.
 *
 * @author Richard Kavanagh
 */
public class ThresholdEventAssessor extends AbstractEventAssessor {

    private int threshold = 2;
    private ArrayList<FiringCriteria> rules = new ArrayList<>();
    private static final String CONFIG_FILE = "paas-self-adaptation-manager-threshold.properties";
    private static final String RULES_FILE = "rules.csv";
    private String workingDir;
    private ResultsStore rulesFile;

    /**
     * This creates a new threshold event assessor. Configuration settings are
     * taken from file.
     */
    public ThresholdEventAssessor() {
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            threshold = config.getInt("paas.self.adaptation.manager.threshold", threshold);
            config.setProperty("paas.self.adaptation.manager.threshold", threshold);
            workingDir = config.getString("paas.self.adaptation.manager.working.directory", ".");
        } catch (ConfigurationException ex) {
            Logger.getLogger(AbstractEventAssessor.class.getName()).log(Level.INFO, "Error loading the configuration of the PaaS Self adaptation manager", ex);
        }
        loadRules();
    }

    /**
     * This performs a check to see if the settings file is empty or not. It
     * will write out a blank file if the file is not present.
     *
     * @param rulesFile The list of rules on disk to load
     * @return If the defaults settings have been written out to disk or not.
     */
    public boolean writeOutDefaults(ResultsStore rulesFile) {
        boolean answer = false;
        //Agreement Term, Guarantee Direction and Response Type
        if (!workingDir.endsWith("/")) {
            workingDir = workingDir + "/";
        }
        if (!new File(workingDir + RULES_FILE).exists()) {
            rulesFile.add("Agreement Term");
            rulesFile.append("Direction");
            rulesFile.append("Response Type");
            rulesFile.add("energy_usage_per_app");
            rulesFile.append("GT");
            rulesFile.append("REMOVE_VM");
            rulesFile.add("power_usage_per_app");
            rulesFile.append("GT");
            rulesFile.append("REMOVE_VM");
            rulesFile.add("energy_usage_per_app");
            rulesFile.append("GTE");
            rulesFile.append("REMOVE_VM");
            rulesFile.add("power_usage_per_app");
            rulesFile.append("GTE");
            rulesFile.append("REMOVE_VM");
            rulesFile.save();
            answer = true;
        }
        return answer;
    }

    /**
     * This loads the rules used by this event assessor in from disk.
     */
    private void loadRules() {
        writeOutDefaults(rulesFile);
        /**
         * Load in from file the following: Agreement Term, Guarantee Direction
         * and Response Type
         */
        rulesFile = new ResultsStore(workingDir + RULES_FILE);
        rulesFile.load();
        //ignore the header of the file
        for (int i = 1; i < rulesFile.size(); i++) {
            ArrayList<String> current = rulesFile.getRow(i);
            FiringCriteria rule = new FiringCriteria(current.get(0), current.get(1), current.get(2));
            rules.add(rule);
        }
    }

    @Override
    public Response assessEvent(EventData event, List<EventData> sequence, List<Response> recentAdaptation) {
        Response answer = null;
        List<EventData> previousData = EventDataAggregator.filterEventData(sequence, event.getSlaUuid(), event.getAgreementTerm());
        if (previousData.size() >= threshold) {
            /**
             * The rule should determine the type of response, i.e. scale up
             * down in or out. This will be read in from file.
             *
             * The decision engine should decide where to perform the adaptation
             * and to what scale.
             */
            FiringCriteria rule = getFirstMatchingFiringCriteria(event);
            int previousActionCount = ResponseHistoryAggregator.filterResponseHistory(recentAdaptation, event.getSlaUuid(), event.getAgreementTerm()).size();
            if (rule != null && previousActionCount == 0) {
                answer = new Response(getActuator(), event, rule.getResponseType());
            }
            return answer;
        }
        return answer;
    }

    /**
     * This tests an event to see if it matches any of the rules for firing off
     * a response.
     *
     * @param event The event to test
     * @return The first firing criteria that indicated that it has fired, due
     * to the specified event.
     */
    private FiringCriteria getFirstMatchingFiringCriteria(EventData event) {
        for (FiringCriteria rule : rules) {
            if (rule.shouldFire(event)) {
                return rule;
            }
        }
        return null;
    }

    /**
     * This gets the amount of events of a given type that need to occur before
     * the event fires.
     *
     * @return the threshold
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * This sets the amount of events of a given type that need to occur before
     * the event fires.
     *
     * @param threshold the threshold to set
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

}
