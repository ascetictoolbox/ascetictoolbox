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

package es.bsc.vmmclient.vmm;

import es.bsc.vmmclient.models.*;

import java.util.List;

public interface VmManager {

    List<VmDeployed> getVms();

    List<String> deployVms(List<Vm> vms);

    VmDeployed getVm(String id);

    void performActionOnVm(String id, VmAction action);

    void destroyVm(String id);

    List<VmDeployed> getAppVms(String id);

    void destroyAppVms(String id);

    List<ImageUploaded> getImages();

    String uploadImage(ImageToUpload image);

    ImageUploaded getImage(String id);

    void destroyImage(String id);

    List<Node> getNodes();

    List<VmEstimate> getEstimates(List<VmToBeEstimated> vms);

	List<VmCost> getCosts(List<String> vmIds);

	void migrate(String vmId, String destinationHostName);
}
