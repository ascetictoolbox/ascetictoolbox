/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.ws.rest;

import com.google.gson.Gson;
import es.bsc.demiurge.core.manager.VmManager;
import es.bsc.demiurge.core.selfadaptation.options.SelfAdaptationOptions;

/**
 * This class implements the REST calls that are related with self adaptation.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class SelfAdaptationCallsManager {

    private final Gson gson = new Gson();
    private final VmManager vmManager;

    /**
     * Class constructor.
     *
     * @param vmManager the VM manager
     */
    public SelfAdaptationCallsManager(VmManager vmManager) {
        this.vmManager = vmManager;
    }

    /**
     * Returns the self-adaptation options for the self-adaptation capabilities of the VMM.
     *
     * @return JSON with the options
     */
    public String getSelfAdaptationOptions() {
        return gson.toJson(vmManager.getSelfAdaptationOptions(), SelfAdaptationOptions.class);
    }

    /**
     * This function updates the configuration options for the self-adaptation capabilities of the VMM.
     *
     * @param options JSON with the options
     */
    public void saveSelfAdaptationOptions(String options) {
        vmManager.saveSelfAdaptationOptions(gson.fromJson(options, SelfAdaptationOptions.class));
    }

}
