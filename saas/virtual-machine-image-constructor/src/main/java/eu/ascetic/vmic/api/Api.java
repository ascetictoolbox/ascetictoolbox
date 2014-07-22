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
package eu.ascetic.vmic.api;

import java.io.File;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmic.api.core.ProgressException;
import eu.ascetic.vmic.api.datamodel.ProgressData;

/**
 * Interface to VMIC capabilities
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public interface Api {

    /**
     * Generates a suitable image from an OVF definition
     * 
     * @param ovfDefinition
     */
    void generateImage(OvfDefinition ovfDefinition);

    /**
     * Given a ovfDefinitionId this function returns the progress status and
     * percentage completion of a previous call to generateImage() as a
     * ProgressData object.
     * 
     * @param ovfDefinitionId
     *            The OVF ID to get progress details on
     * @return progressData An object containing details of progress
     * @throws ProgressException
     *             Thrown if an error occurred during the generation of an image
     */
    public ProgressData progressCallback(String ovfDefinitionId)
            throws ProgressException;

    /**
     * Functionality to enable uploading of images to the SaaS local repository
     * 
     * @param ovfDefinitionId
     *            The OVF ID this file is associated with
     * @param file
     *            The file to upload
     * @returns A URI reference to the uploaded file for use in the OVF
     *          Definition
     */
    public String uploadFile(String ovfDefinitionId, File file);
}
