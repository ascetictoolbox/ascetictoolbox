package eu.ascetic.paas.applicationmanager.dao;

import java.util.List;

import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.VM;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * DAO interface to perform database actions over an VM object
 *
 */

public interface VMDAO extends DAO<VM> {
	/**
	 * Returns the VM from the database by its Id
	 * 
	 * @param vmId of the VM
	 * @return the corresponding VM from the database
	 */
	public VM getById(int vmId);

	/**
	 * Returns all the VM stored in the database
	 * 
	 * @return all the VM stored in the database
	 */
	public List<VM> getAll();

	/**
	 * Stores a VM into the database
	 * 
	 * @param VM to be saved.
	 * @return <code>true</code> if the VM was saved correctly
	 */
	public boolean save(VM vn);

	/**
	 * Updates a VM in the database
	 * 
	 * @param VM to be updated
	 * @return <code>true</code> if the Deployment was updated correctly
	 */
	public boolean update(VM vm);

	/**
	 * Deletes a VM from the database
	 * 
	 * @param VM to be deleted
	 * @return <code>true</code> if the VM was deleted correctly
	 */
	public boolean delete(VM vm);
	
	/**
	 * Returns all the VMs that are not deleted using the image
	 * @param image
	 * @return
	 */
	public List<VM> getNotDeletedVMsWithImage(Image image);
	
	/**
	 * Returns how many VMs are in not DELETED state in the DB for
	 * an specific ovfId and an specific deployment
	 * @param ovfId Of the VMs we are looking for
	 * @param deploymentId deploymentId to which the VMs belogn
	 * @return list of VMs registered in the DB
	 */
	public List<VM> getVMsWithOVfIdForDeploymentNotDeleted(String ovfId, int deploymentId);
	
	/**
	 * Returns the VM with this specific combination of providerID and providerVMID
	 * @param proivderVMId
	 * @param providerID
	 * @return the VM information or null if no VM with that information is avaiblable.
	 */
	public VM getVMWithProviderVMId(String proivderVMId, String providerID); 
	
	/**
	 * Returns the VMs with an specific OVF ID that are active in the deployment
	 * @param deploymentId
	 * @param ovfId
	 * @return 
	 */
	public List<VM> getVMsWithOvfIdAndActive(int deploymentId, String ovfId);
}
