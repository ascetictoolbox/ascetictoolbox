/**
 * Copyright 2016 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package eu.ascetic.paas.self.adaptation.manager.ovf;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;
import eu.ascetic.utils.ovf.api.utils.OvfRuntimeException;
import org.apache.log4j.Logger;


public class OVFUtils {

    /**
     * The logger.
     */
    private static Logger logger = Logger.getLogger(OVFUtils.class);

    /**
     * Gets the ovf definition object.
     *
     * @param ovf the ovf
     * @return the ovf definition
     */
    public static OvfDefinition getOvfDefinition(String ovf) {
        if (ovf == null || ovf.equals("")) {
            return null;
        }
        try {
            OvfDefinition ovfDocument = OvfDefinition.Factory.newInstance(ovf);
            return ovfDocument;
        } catch (OvfRuntimeException ex) {
            Logger.getLogger(OVFUtils.class.getName()).info("Error parsing OVF file: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }    

    /**
     * Extracts the field ovf:Name from VirtualSystemCollection to differenciate
     * between applications.
     *
     * @param ovf String representing the OVF definition of an Application
     * @return the application name
     */
    public static String getApplicationName(OvfDefinition ovf) {

        try {
            return ovf.getVirtualSystemCollection().getId();
        } catch (OvfRuntimeException ex) {
            Logger.getLogger(OVFUtils.class.getName()).info("Error parsing OVF file: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }    

    /**
     * This gets the VirtualSystem object representation for an specific ovf VM 
     * type.
     *
     * @param ovf The ovf definition to be checked
     * @param ovfId The ovf id/ VM type to extract from the OVF
     * @return The Virtual System for the given VM type, otherwise null if it does
     * not exist.
     */
    public static VirtualSystem getVMFromOvfType(OvfDefinition ovf, String ovfId) {

        if (ovfId == null) {
            return null;
        }

        try {
            VirtualSystemCollection vsc = ovf.getVirtualSystemCollection();
            for (int i = 0; i < vsc.getVirtualSystemArray().length; i++) {
                VirtualSystem virtualSystem = vsc.getVirtualSystemAtIndex(i);
                String ovfVirtualSystemID = virtualSystem.getId();

                if (ovfId.equals(ovfVirtualSystemID)) {
                    return virtualSystem;
                }
            }

        } catch (OvfRuntimeException ex) {
            Logger.getLogger(OVFUtils.class.getName()).info("Error parsing OVF file: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }  
    
    /**
     * Returns the ProductSection for an specific ovfID
     *
     * @param ovf String representing the ovf file where to look
     * @param ovfId Ovf ID of the wanted product section
     * @return Returns a ProductSection object if the sections exits or null
     * otherwise
     */
    public static ProductSection getProductionSectionFromOvfType(OvfDefinition ovf, String ovfId) {

        VirtualSystem virtualSystem = getVMFromOvfType(ovf, ovfId);

        if (virtualSystem != null) {
            return virtualSystem.getProductSectionAtIndex(0);
        }

        return null;
    }


}
