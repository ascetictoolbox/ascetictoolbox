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

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class for defining progress data.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public abstract class AbstractProgressData {

    public static final double COMPLETED_PERCENTAGE = 100.0;

    /**
     * The current phase being executed.
     */
    protected int currentPhaseId;

    /**
     * The current percentage completion
     */
    protected Double currentPercentageCompletion;

    /**
     * Defines when the VMIC has completed.
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
     * Stores the history of progress.
     */
    protected Map<Integer, Double> history;

    /**
     * Stores the phases.
     */
    private String[] phases = null;

    /**
     * Constructor that sets up the phases of an operation.
     * 
     */
    public AbstractProgressData(String[] phases) {
        super();
        this.phases = phases.clone();
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
        return phases[currentPhaseId];
    }

    /**
     * Gets a phase name.
     * 
     * @return the a phase name as a string.
     */
    public String getPhaseName(int phaseId) {
        return phases[phaseId];
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
     * @return the currentPercentageCompletion
     */
    public Double getCurrentPercentageCompletion() {
        return currentPercentageCompletion;
    }

    /**
     * Sets the current percentage completion of an operation.
     * 
     * @param currentPercentageCompletion
     *            the currentPercentageCompletion to set
     */
    public void setCurrentPercentageCompletion(
            Double currentPercentageCompletion) {
        this.currentPercentageCompletion = currentPercentageCompletion;
        this.history.put(currentPhaseId, currentPercentageCompletion);
    }

    /**
     * Defines if the VMIC has completed an operation.
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
     * The total progress of this VMIC operation.
     * 
     * @return the totalProgress
     */
    public Double getTotalProgress() {
        if (currentPhaseId == 0) {
            return 0.0;
        } else {
            return ((COMPLETED_PERCENTAGE / (double) (phases.length / (double) (currentPhaseId))) + (COMPLETED_PERCENTAGE / (double) phases.length)
                    * (currentPercentageCompletion / (double) COMPLETED_PERCENTAGE));
        }
    }

    /**
     * The number of phases in this VMIC operation.
     * 
     * @return the totalProgress
     */
    public int getNumberOfPhases() {
        return phases.length;
    }

}