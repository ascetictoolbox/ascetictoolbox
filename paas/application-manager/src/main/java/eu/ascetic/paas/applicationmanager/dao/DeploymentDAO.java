package eu.ascetic.paas.applicationmanager.dao;

import java.util.List;

import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;

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
 * DAO interface to perform database actions over an Deployment object
 *
 */

public interface DeploymentDAO extends DAO<Deployment> {
	/**
	 * Returns the Deployment from the database by its Id
	 * 
	 * @param id of the Deployment
	 * @return the corresponding Deployment from the database
	 */
	public Deployment getById(int id);

	/**
	 * Returns all the Deployment stored in the database
	 * 
	 * @return all the Deployment stored in the database
	 */
	public List<Deployment> getAll();

	/**
	 * Stores a Deployment into the database
	 * 
	 * @param Deployment to be saved.
	 * @return <code>true</code> if the Deployment was saved correctly
	 */
	public boolean save(Deployment deployment);

	/**
	 * Updates a Deployment in the database
	 * 
	 * @param Deployment to be updated
	 * @return <code>true</code> if the Deployment was updated correctly
	 */
	public boolean update(Deployment deployment);

	/**
	 * Deletes a Deployment from the database
	 * 
	 * @param Deployment to be deleted
	 * @return <code>true</code> if the Deployment was deleted correctly
	 */
	public boolean delete(Deployment deployment);
	
	/**
	 * Returns all deployments for a given application with an specific status
	 * @param application Application from which we want to know the status...	
	 * @param status that we want to know...
	 * @return the list of deployments
	 */
	public List<Deployment> getDeploymentsForApplicationWithStatus(Application application, String status);
	
	/**
	 * Returns all deployments with an specific status
	 * @param status we are interested of.
	 * @return a List object with all the deploymetns that have that status in the DB
	 */
	public List<Deployment> getDeploymentsWithStatus(String status);
	
	/**
	 * Returns a deployment in the database with an specific deploymentName (that it is unique)
	 * @param deploymentName
	 * @return the deployment, if does not exists, it returns null
	 */
	public Deployment getDeployment(String deploymentName);
}
