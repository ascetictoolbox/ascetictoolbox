/*
 *  Copyright 2002-2012 Barcelona Supercomputing Center (www.bsc.es)
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

package integratedtoolkit.util;

import integratedtoolkit.types.CloudImageDescription;
import integratedtoolkit.types.ResourceDescription;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * The CloudImageManager is an utility to manage the different images that can 
 * be used for a certain Cloud Provider
 */
public class CloudImageManager {
   
    /** Relation between the name of an image and its features */
    private HashMap<String, CloudImageDescription> images;

    /**
     * Constructs a new CloudImageManager
     */
    public CloudImageManager() {
        images = new HashMap();
    }

    /**
     * Adds a new image which can be used by the Cloud Provider
     * @param cid Description of the image
     */
    public void add(CloudImageDescription cid) {
        images.put(cid.getName(), cid);
    }

    /**
     * Finds all the images provided by the Cloud Provider which fulfill the 
     * resource description.
     * @param requested description of the features that the image must provide
     * @return The best image provided by the Cloud Provider which fulfills the 
     * resource description 
     */
    public LinkedList<CloudImageDescription> getCompatibleImages(ResourceDescription requested) {
        LinkedList<CloudImageDescription> compatiblesList =  new LinkedList<CloudImageDescription>();
        for (CloudImageDescription cid : images.values()) {
            // TODO CHECK ARCH, SOFTWARE AND OS constraints
            compatiblesList.add(cid);
        }
        return compatiblesList;
    }

    /**
     * Return all the image names offered by that Cloud Provider
     * @return set of image names offered by that Cloud Provider
     */
    public Set<String> getAllImageNames() {
        return images.keySet();
    }
}
