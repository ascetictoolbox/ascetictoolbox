package eu.ascetic.paas.applicationmanager.dao;

import java.util.List;

import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Deployment;;


/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * DAO interface to perform database actions over an Agreement object
 * 
 */

public interface AgreementDAO extends DAO<Agreement> {

	/**
	 * Returns the Agreement from the database by its Id with deployment information
	 * 
	 * @param id of the Agreement
	 * @return the corresponding Application from the database
	 */
	public Agreement getById(int id);
	

	/**
	 * Returns all the Agreement stored in the database with Agreement information
	 * 
	 * @return all the Agreement stored in the database
	 */
	public List<Agreement> getAll();
	

	/**
	 * Stores a Agreement into the database
	 * 
	 * @param Agreement to be saved.
	 * @return <code>true</code> if the Agreement was saved correctly
	 */
	public boolean save(Agreement agreement);

	/**
	 * Updates a Agreement in the database
	 * 
	 * @param Agreement to be updated
	 * @return <code>true</code> if the Agreement was updated correctly
	 */
	public boolean update(Agreement agreement);

	/**
	 * Deletes a Agreement from the database
	 * 
	 * @param Agreement to be deleted
	 * @return <code>true</code> if the Agreement was deleted correctly
	 */
	public boolean delete(Agreement agreement);
	
	/**
	 * Returns the accepted agreement for a deployment
	 * @param deployment Deployment to search for agreements
	 * @return the accepted agreement or null if none.
	 */
	public Agreement getAcceptedAgreement(Deployment deployment);
}
