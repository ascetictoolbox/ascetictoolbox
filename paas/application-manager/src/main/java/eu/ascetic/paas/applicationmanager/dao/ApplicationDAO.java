package eu.ascetic.paas.applicationmanager.dao;

import java.util.List;

import eu.ascetic.paas.applicationmanager.model.Application;


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
 * DAO interface to perform database actions over an Application object
 * 
 */

public interface ApplicationDAO extends DAO<Application> {

	/**
	 * Returns the Application from the database by its Id with deployment information
	 * 
	 * @param id of the Application
	 * @return the corresponding Application from the database
	 */
	public Application getById(int id);
	
	/**
	 * Returns the Application from the database by its Id with an empty list of deployments
	 * 
	 * @param id of the Application
	 * @return the corresponding Application from the database
	 */
	public Application getByIdWithoutDeployments(int id);

	/**
	 * Returns all the Application stored in the database with Deployments information
	 * 
	 * @return all the Application stored in the database
	 */
	public List<Application> getAll();
	
	/**
	 * Returns all the Application stored in the database without Deployments information
	 * 
	 * @return all the Application stored in the database
	 */
	public List<Application> getAllWithOutDeployments();

	/**
	 * Stores a Application into the database
	 * 
	 * @param Application to be saved.
	 * @return <code>true</code> if the Application was saved correctly
	 */
	public boolean save(Application application);

	/**
	 * Updates a Application in the database
	 * 
	 * @param Application to be updated
	 * @return <code>true</code> if the Application was updated correctly
	 */
	public boolean update(Application application);

	/**
	 * Deletes a Application from the database
	 * 
	 * @param Provider to be deleted
	 * @return <code>true</code> if the Application was deleted correctly
	 */
	public boolean delete(Application application);
	
	/**
	 * Returns an application from the database using its name as query paremeter with deployment information
	 * @param name of the application
	 * @return The Application object or <code>null</code> if there is no application by that name in the Database
	 */
	public Application getByName(String name);
	
	/**
	 * Returns an application from the database using its name as query paremeter with deployment information without deployment information
	 * @param name of the application
	 * @return The Application object or <code>null</code> if there is no application by that name in the Database
	 */
	public Application getByNameWithoutDeployments(String name);
}