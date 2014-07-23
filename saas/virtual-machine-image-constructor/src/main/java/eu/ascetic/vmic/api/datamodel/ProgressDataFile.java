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

/**
 * A class to store the file operation progress data. Contains statics for ID.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.1
 */
public class ProgressDataFile extends AbstractProgressData {

    // Index of VMIC PHASES.
    /**
     * 0
     */
    public static final int INITIALIZE_PHASE_ID = 0;
    /**
     * 2
     */
    public static final int UPLOADING_FILE_PHASE_ID = 1;
    /**
     * 4
     */
    public static final int FINALISE_PHASE_ID = 2;

    // Textual representation of PHASES of VMIC in order.
    /**
     * "Starting Image Generation Process"
     */
    private static final String INITIALISE_PHASE_TEXT = "Starting File Upload Process";
    /**
     * "Generating Image(s)"
     */
    private static final String UPLOADING_FILE_PHASE_TEXT = "Uploading File";
    /**
     * "Image Generation Complete!"
     */
    private static final String FINALISE_PHASE_TEXT = "File Upload Complete!";

    /**
     * Phase text in order of execution.
     */
    static final String[] PHASES = { INITIALISE_PHASE_TEXT,
            UPLOADING_FILE_PHASE_TEXT, FINALISE_PHASE_TEXT };

    /**
     * Used to store a files remote URI reference for passing back through the
     * API.
     */
    private String remotePath = null;

    /**
     * Constructor with default progress values.
     */
    public ProgressDataFile() {
        super(PHASES); // Initialise super constructor
    }

    /**
     * Gets the remote path.
     * 
     * @return the remotePath
     */
    public String getRemotePath() {
        return remotePath;
    }

    /**
     * Sets the remote path.
     * 
     * @param remotePath
     *            the remotePath to set
     */
    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }
}
