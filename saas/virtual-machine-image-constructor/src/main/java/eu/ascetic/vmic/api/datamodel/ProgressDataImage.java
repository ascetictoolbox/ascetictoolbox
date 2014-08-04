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

import eu.ascetic.utils.ovf.api.OvfDefinition;

/**
 * A class to store the image operation progress data. Contains statics for ID.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.1
 */
public class ProgressDataImage extends AbstractProgressData {

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
    public static final int GENERATE_IMAGES_PHASE_ID = 2;
    /**
     * 3
     */
    public static final int FINALISE_PHASE_ID = 3;

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
    static final String[] PHASES = { INITIALISE_PHASE_TEXT,
            RETRIEVE_DATA_PHASE_TEXT, GENERATE_IMAGES_PHASE_TEXT,
            FINALISE_PHASE_TEXT };

    /**
     * Used to store an altered version of the original ovfDefinition for
     * passing image URI's back through the API.
     */
    private OvfDefinition ovfDefinition = null;

    /**
     * Constructor with default progress values.
     */
    public ProgressDataImage() {
        super(PHASES); // Initialise super constructor
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
}
