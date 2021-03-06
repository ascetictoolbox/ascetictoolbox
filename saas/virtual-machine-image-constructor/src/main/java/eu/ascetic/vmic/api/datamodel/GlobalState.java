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

import eu.ascetic.utils.ovf.api.OvfDefinition;

/**
 * Class for storing the state of the VMIC.
 * 
 * TODO: Add cleanup mechanism to purge completed applications after X time?
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.1
 */
public class GlobalState {

    private GlobalConfiguration globalConfiguration;
    private Map<String, AbstractProgressData> progressDataHashMap;
    private boolean running = false;

    /**
     * Default constructor that creates a {@link ProgressDataImage} {@link Map}
     * for storing the state of all applications for which we are generating
     * images.
     * 
     * @param globalConfiguration
     *            The global configuration object storing environmental
     *            properties to setup the VMIC.
     */
    public GlobalState(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
        progressDataHashMap = new HashMap<String, AbstractProgressData>();
    }

    /**
     * @return the configuration
     */
    public GlobalConfiguration getConfiguration() {
        return globalConfiguration;
    }

    /**
     * Adds a new instance of a {@link ProgressDataImage} object to the
     * {@link GlobalState} of the VMIC
     * 
     * @param ovfDefinitionId
     *            The OVF ID to create a {@link ProgressDataImage} object for.
     */
    public void addImageProgress(String ovfDefinitionId) {
        progressDataHashMap.put(ovfDefinitionId, new ProgressDataImage());
    }

    /**
     * Adds a new instance of a {@link ProgressDataImage} object to the
     * {@link GlobalState} of the VMIC
     * 
     * @param id
     *            The id of the {@link ProgressDataFile} object.
     */
    public void addFileProgress(String id) {
        progressDataHashMap.put(id, new ProgressDataFile());
    }

    /**
     * Sets the current progress phase for a given application.
     * 
     * @param id
     *            The id of the {@link AbstractProgressData} object.
     * @param currentPhaseId
     *            The current phase ID to set (see {@link AbstractProgressData})
     */
    public void setProgressPhase(String id, int currentPhaseId) {
        AbstractProgressData abstractProgressData = progressDataHashMap
                .get(id);
        abstractProgressData.setCurrentPhaseId(currentPhaseId);
        progressDataHashMap.put(id, abstractProgressData);
    }

    /**
     * Sets the current progress percentage for the phase of a given
     * application.
     * 
     * @param id
     *            The id of the {@link AbstractProgressData} object.
     * @param currentPercentageCompletion
     *            The current completion % of a phase to set (see
     *            {@link AbstractProgressData}).
     */
    public void setProgressPercentage(String id,
            Double currentPercentageCompletion) {
        AbstractProgressData abstractProgressData = progressDataHashMap
                .get(id);
        abstractProgressData
                .setCurrentPercentageCompletion(currentPercentageCompletion);
        progressDataHashMap.put(id, abstractProgressData);
    }

    /**
     * Get the AbstractProgressData object with a given ID.
     * 
     * @param id
     *            The id of the {@link AbstractProgressData} object.
     * @return The AbstractProgressData object to return.
     */
    public AbstractProgressData getProgressData(String id) {
        return progressDataHashMap.get(id);
    }

    /**
     * Sets additional progress details for an uploading file via
     * an applications associated {@link ProgressDataFile} object.
     * 
     * @param remotePath
     *            The remote path to return
     */
    public void setFileProgressDetails(String id, String transfered, String speed, String eta) {
        ((ProgressDataFile) progressDataHashMap.get(id))
                .setTransfered(transfered);
        ((ProgressDataFile) progressDataHashMap.get(id))
        .setSpeed(speed);
        ((ProgressDataFile) progressDataHashMap.get(id))
        .setEta(eta);
    }
    
    
    /**
     * Set the ovfDefinition to be returned through the API via an applications
     * associated {@link ProgressDataImage} object.
     * 
     * @param ovfDefinition
     *            The OvfDefinition to return.
     */
    public void setOvfDefinition(OvfDefinition ovfDefinition) {
        ((ProgressDataImage) progressDataHashMap.get(ovfDefinition
                .getVirtualSystemCollection().getId()))
                .setOvfDefinition(ovfDefinition);
    }

    /**
     * Set the remote path of a File uploaded to be returned through the API via
     * an applications associated {@link ProgressDataFile} object.
     * 
     * @param remotePath
     *            The remote path to return
     */
    public void setRemotePath(String id, String remotePath) {
        ((ProgressDataFile) progressDataHashMap.get(id))
                .setRemotePath(remotePath);
    }

    /**
     * Specify that an operation on an application has finished.
     * 
     * @param id
     *            The id of the {@link AbstractProgressData} object to set as complete.
     */
    public void setComplete(String id) {
        progressDataHashMap.get(id).setComplete(true);
    }

    /**
     * Returns a string representing the progress of a application operation for
     * logging purposes.
     * 
     * @param id
     *            The id of the {@link AbstractProgressData} object to get a progress string for.
     * @return The string representation of the progress.
     */
    public String getProgressLogString(String id) {
        return progressDataHashMap.get(id).getProgressString()
                + " [AbstractProgressData ID: " + id + "]";
    }

    /**
     * Returns the status of thread.
     * 
     * @return the running status
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Sets the status of thread.
     * 
     * @param running
     *            the running status to set
     */
    public void setRunning(boolean running) {
        this.running = running;
    }
}
