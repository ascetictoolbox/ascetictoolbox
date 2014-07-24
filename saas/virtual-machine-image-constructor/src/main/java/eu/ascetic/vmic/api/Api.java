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
import eu.ascetic.vmic.api.datamodel.AbstractProgressData;
import eu.ascetic.vmic.api.datamodel.ProgressDataFile;
import eu.ascetic.vmic.api.datamodel.ProgressDataImage;

/**
 * Interface to VMIC capabilities
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public interface Api {

    /**
     * Generates suitable image(s) from an OVF definition
     * 
     * @param ovfDefinition
     *            The OVF definition to generate image(s) from
     */
    public void generateImage(OvfDefinition ovfDefinition);

    /**
     * Functionality to enable uploading of images to the SaaS local repository
     * 
     * @param ovfDefinitionId
     *            The OVF ID this file is associated with
     * @param file
     *            The file to upload
     */
    public void uploadFile(String ovfDefinitionId, File file);

    /**
     * Given a ovfDefinitionId this function returns the progress status and
     * percentage completion of a previous call to generateImage() or
     * uploadFile() (if the optional file argument is passed) as a
     * {@link AbstractProgressData} object that should be cast to either
     * {@link ProgressDataFile} (which also provides access to the files
     * {@link ProgressDataFile#remotePath}) or {@link ProgressDataImage} (that
     * provides access to the altered {@link ProgressDataImage#ovfDefinition})
     * depending on the operation.
     * 
     * @param ovfDefinitionId
     *            The OVF ID to get progress details on
     * @param file
     *            The OVF associated file to get progress details on (optional
     *            can be null)
     * @return An object containing details of progress
     * @throws ProgressException
     *             Thrown if an error occurred during the generation of an image
     */
    public AbstractProgressData progressCallback(String ovfDefinitionId,
            File file) throws ProgressException;
}
