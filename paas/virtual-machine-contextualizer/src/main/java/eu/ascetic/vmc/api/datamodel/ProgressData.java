/**
 *  Copyright 2013 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.vmc.api.datamodel;

import java.util.HashMap;
import java.util.Map;

import eu.ascetic.utils.ovf.api.OvfDefinition;

/**
 * A class to store the progress data of a service. Contains statics for ID and
 * string representations of each phase in the VMC's contextualization life
 * cycle.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.5
 */
public class ProgressData {

    public static final double COMPLETED_PERCENTAGE = 100.0;

    // Index of contextualization PHASES.
    /**
     * 0
     */
    public static final int INITIALIZE_PHASE_ID = 0;
    /**
     * 1
     */
    public static final int RETRIEVE_DATA_PHASE_ID = 1;
    /**
     * 2
     */
    public static final int CONVERT_IMAGE_PHASE_ID = 2;
    /**
     * 3
     */
    public static final int CREATE_ISOS_PHASE_ID = 3;
    /**
     * 4
     */
    public static final int FINALISE_PHASE_ID = 4;

    // Textual representation of PHASES of contextualization in order.
    /**
     * "Starting Contextualization Process"
     */
    private static final String INITIALISE_PHASE_TEXT = "Starting Contextualization Process";
    /**
     * "Retrieving Contextualization Data"
     */
    private static final String RETRIEVE_DATA_PHASE_TEXT = "Retrieving Contextualization Data";
    /**
     * "Converting Image(s)"
     */
    private static final String COVNERT_IMAGES_PHASE_TEXT = "Converting Image(s)";
    /**
     * "Creating ISO Image(s)"
     */
    private static final String CREATE_ISOS_PHASE_TEXT = "Creating ISO Image(s)";
    /**
     * "Contextualization Complete!"
     */
    private static final String FINALISE_PHASE_TEXT = "Contextualization Complete!";

    /**
     * Phase text in order of execution.
     */
    private static final String[] PHASES = { INITIALISE_PHASE_TEXT,
            RETRIEVE_DATA_PHASE_TEXT, COVNERT_IMAGES_PHASE_TEXT,
            CREATE_ISOS_PHASE_TEXT, FINALISE_PHASE_TEXT };

    /**
     * The current phase being executed.
     */
    private int currentPhaseId;

    /**
     * The current percentage completion of the currentPhaseId.
     */
    private Double currentPercentageCompletion;

    /**
     * Used to store an altered version of the ovfDefinition for passing image
     * URI's back through the API.
     */
    private OvfDefinition ovfDefinition = null;

    /**
     * Defines when the VMC has completed contextualizing a service.
     */
    private boolean complete = false;

    /**
     * Defines if the VMC has encountered an unrecoverable exception.
     */
    private boolean error = false;

    /**
     * Exception causing VMC to error out.
     */
    private Exception exception = null;

    /**
     * Stores the history of progress
     */
    private Map<Integer, Double> history;

    /**
     * Constructor with default progress values.
     */
    public ProgressData() {
        currentPhaseId = 0;
        currentPercentageCompletion = COMPLETED_PERCENTAGE;
        this.history = new HashMap<Integer, Double>(1);
        history.put(currentPhaseId, currentPercentageCompletion);
    }

    /**
     * Gets the current progress as text.
     * 
     * @return the current progress in textual form.
     */
    public String getProgressString() {
        return getCurrentPhaseName() + " " + getCurrentPercentageCompletion()
                + "%";
    }

    /**
     * Gets the current phase name.
     * 
     * @return the current phase name as a string.
     */
    public String getCurrentPhaseName() {
        return PHASES[currentPhaseId];
    }

    /**
     * Gets a phase name.
     * 
     * @return the a phase name as a string.
     */
    public String getPhaseName(int phaseId) {
        return PHASES[phaseId];
    }

    /**
     * Gets the current phase ID.
     * 
     * @return the currentPhaseId.
     */
    public int getCurrentPhaseId() {
        return currentPhaseId;
    }

    /**
     * Sets the current phase ID.
     * 
     * @param currentPhaseId
     *            the currentPhaseId to set.
     */
    public void setCurrentPhaseId(int currentPhaseId) {
        this.currentPhaseId = currentPhaseId;
        this.currentPercentageCompletion = 0.0;
        this.history.put(currentPhaseId, currentPercentageCompletion);
    }

    /**
     * Gets the current percentage completion of an operation.
     * 
     * @return the currentPercentageCompletion for the current phase.
     */
    public Double getCurrentPercentageCompletion() {
        return currentPercentageCompletion;
    }

    /**
     * Sets the current percentage completion of an operation.
     * 
     * @param currentPercentageCompletion
     *            the currentPercentageCompletion to set for the current phase.
     */
    public void setCurrentPercentageCompletion(
            Double currentPercentageCompletion) {
        this.currentPercentageCompletion = currentPercentageCompletion;
        this.history.put(currentPhaseId, currentPercentageCompletion);
    }

    /**
     * Defines if the VMC has completed contextualization.
     * 
     * @return the complete
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Sets completion state.
     * 
     * @param complete
     *            the complete to set
     */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    /**
     * Tests if an error occurred.
     * 
     * @return the error
     */
    public boolean isError() {
        return error;
    }

    /**
     * Sets error state.
     * 
     * @param error
     *            the error to set
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * Gets the {@link Exception} that caused the error.
     * 
     * @return the exception
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Sets the {@link Exception} that caused the error.
     * 
     * @param exception
     *            the exception to set
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * Gets a Map&ltPhaseId, PhasePercentageCompletion&gt history of progress,
     * use getPhaseName() to get text name of the phase.
     * 
     * @return the history of progress
     */
    public Map<Integer, Double> getHistory() {
        return history;
    }

    /**
     * The total progress of the VMC.
     * 
     * @return the totalProgress
     */
    public Double getTotalProgress() {
        if (currentPhaseId == 0) {
            return 0.0;
        } else {
            return COMPLETED_PERCENTAGE / (PHASES.length / (currentPhaseId));
        }
    }

    /**
     * The number of PHASES in the VMC.
     * 
     * @return the totalProgress
     */
    public int getNumberOfPhases() {
        return PHASES.length;
    }

    /**
     * Gets the ovfDefinition altered by the VMC during the contextualization
     * process.
     * 
     * @return the ovfDefinition.
     */
    public OvfDefinition getOvfDefinition() {
        return ovfDefinition;
    }

    /**
     * Sets the OvfDefinition.
     * 
     * @param ovfDefinition
     *            the ovfDefinition to set.
     */
    public void setOvfDefinition(OvfDefinition ovfDefinition) {
        this.ovfDefinition = ovfDefinition;
    }
}
