/**
 *  Copyright 2014 University of Leeds
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
package eu.ascetic.vmic.api.datamodel;

import java.util.Map;

import eu.ascetic.utils.ovf.api.OvfDefinition;

/**
 * A class to store the progress data of a application. Contains statics for ID.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.1
 */
public class ProgressData {

    // Index of VMIC PHASES.
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
     * 4
     */
    public static final int FINALISE_PHASE_ID = 4;

    // Textual representation of PHASES of VMIC in order.
    /**
     * "Starting Image Generation Process"
     */
    private static final String INITIALISE_PHASE_TEXT = "Starting Image Generation Process";
    /**
     * "Retrieving Image Generation Data"
     */
    private static final String RETRIEVE_DATA_PHASE_TEXT = "Retrieving Image Generation Data";
    /**
     * "Generating Image(s)"
     */
    private static final String GENERATE_IMAGES_PHASE_TEXT = "Generating Image(s)";
    /**
     * "Image Generation Complete!"
     */
    private static final String FINALISE_PHASE_TEXT = "Image Generation Complete!";

    /**
     * Phase text in order of execution.
     */
    private static final String[] PHASES = { INITIALISE_PHASE_TEXT,
            RETRIEVE_DATA_PHASE_TEXT, GENERATE_IMAGES_PHASE_TEXT,
            FINALISE_PHASE_TEXT };

    /**
     * The current phase being executed.
     */
    private int currentPhaseId;

    public static final double COMPLETED_PERCENTAGE = 100.0;

    /**
     * The current percentage completion
     */
    private Double currentPercentageCompletion;

    /**
     * Used to store an altered version of the ovfDefinition for passing image
     * URI's back through the API.
     */
    private OvfDefinition ovfDefinition = null;

    /**
     * Defines when the VMIC has completed constructing an image.
     */
    private boolean complete = false;

    /**
     * Defines if the VMIC has encountered an unrecoverable exception.
     */
    private boolean error = false;

    /**
     * Exception causing VMIC to error out.
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
        currentPercentageCompletion = 0.0;
    }

    /**
     * @return the current progress in textual form.
     */
    public String getProgressString() {
        return getCurrentPhaseName() + " " + getCurrentPercentageCompletion()
                + "%";
    }

    /**
     * @return the current phase name as a string.
     */
    public String getCurrentPhaseName() {
        return PHASES[currentPhaseId];
    }

    /**
     * @return the current phase name as a string.
     */
    public String getPhaseName(int phaseId) {
        return PHASES[phaseId];
    }

    /**
     * @return the currentPhaseId.
     */
    public int getCurrentPhaseId() {
        return currentPhaseId;
    }

    /**
     * @param currentPhaseId
     *            the currentPhaseId to set.
     */
    public void setCurrentPhaseId(int currentPhaseId) {
        this.currentPhaseId = currentPhaseId;
        this.currentPercentageCompletion = 0.0;
        this.history.put(currentPhaseId, currentPercentageCompletion);
    }

    /**
     * @return the currentPercentageCompletion
     */
    public Double getCurrentPercentageCompletion() {
        return currentPercentageCompletion;
    }

    /**
     * @param currentPercentageCompletion
     *            the currentPercentageCompletion to set
     */
    public void setCurrentPercentageCompletion(
            Double currentPercentageCompletion) {
        this.currentPercentageCompletion = currentPercentageCompletion;
    }

    /**
     * The ovfDefinition altered by the VMIC during the generation process.
     * 
     * @return the ovfDefinition.
     */
    public OvfDefinition getOvfDefinition() {
        return ovfDefinition;
    }

    /**
     * @param ovfDefinition
     *            the ovfDefinition to set.
     */
    public void setOvfDefinition(OvfDefinition ovfDefinition) {
        this.ovfDefinition = ovfDefinition;
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
     * @param complete
     *            the complete to set
     */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    /**
     * @return the error
     */
    public boolean isError() {
        return error;
    }

    /**
     * @param error
     *            the error to set
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * @return the exception
     */
    public Exception getException() {
        return exception;
    }

    /**
     * @param exception
     *            the exception to set
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * HashMap<PhaseId, PhasePercentageCompletion>, use getPhaseName() to get
     * text name of the phase.
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
}
