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
package eu.ascetic.vmic.api.core;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmic.api.VmicApi;

/**
 * @author Django Armstrong (ULeeds)
 *
 */
public class OvfDefinitionParser {

    private OvfDefinition ovfDefinition;
    
    private String ovfDefinitionId;

    private String applicationRepositoryPath;

    /**
     * @param ovfDefinition
     * 
     */
    public OvfDefinitionParser(OvfDefinition ovfDefinition, VmicApi vmicApi) {
        this.ovfDefinition = ovfDefinition;
        this.ovfDefinitionId = ovfDefinition.getVirtualSystemCollection().getId();
        this.applicationRepositoryPath = vmicApi.getGlobalState()
                .getConfiguration().getRepositoryPath() + "/" + ovfDefinitionId;
    }

    /**
     * 
     */
    public int parse() {

        // TODO: Parse data need for image generation from the ProductSection
        ovfDefinition.getVirtualSystemCollection().getProductSectionArray();

        // TODO: Store the image generation somewhere...

        return 0;
    }

    /**
     * @return
     */
    public int getMode() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @return
     */
    public int getImageNumber() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @param i
     * @return
     */
    public String getImagePath(int i) {
        // TODO
        return applicationRepositoryPath + "/" + getImageName(i);
    }
    
    /**
     * @param i
     * @return
     */
    private String getImageName(int i) {
        // TODO
        return "";
    }

    
    /**
     * @param i
     * @return
     */
    public String getImageMountPointPath(int i) {
        return applicationRepositoryPath + "/mnt/" + getImageName(i);
    }

    /**
     * @param i
     * @return
     */
    public String getScript(int i) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return
     */
    public String getBaseImagePath() {
        // FIXME: Using hard coded image URI, for Y2 select using OVF operating
        // system section
        return "/DFS/ascetic/vm-images/Ubuntu.qcow2";
    }
}
