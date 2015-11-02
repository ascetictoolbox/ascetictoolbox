package eu.ascetic.providerregistry.service;

import java.util.List;

import eu.ascetic.providerregistry.model.Provider;

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
 * @email david.garciaperez@atos.net 
 * 
 * DAO interface to perform database actions over a Provider object
 */
public interface ProviderDAO extends DAO<Provider> {

	/**
	 * Returns the Provider from the database by its Id
	 * 
	 * @param id of the Provider
	 * @return the corresponding Provider from the database
	 */
	public Provider getById(int id);

	/**
	 * Returns all the Provider stored in the database
	 * 
	 * @return all the Provider stored in the database
	 */
	public List<Provider> getAll();

	/**
	 * Stores a Provider into the database
	 * 
	 * @param Provider to be saved.
	 * @return <code>true</code> if the Provider was saved correctly
	 */
	public boolean save(Provider provider);

	/**
	 * Updates a Provider in the database
	 * 
	 * @param Provider to be updated
	 * @return <code>true</code> if the Provider was updated correctly
	 */
	public boolean update(Provider provider);

	/**
	 * Deletes a Provider from the database
	 * 
	 * @param Provider to be deleted
	 * @return <code>true</code> if the Provider was deleted correctly
	 */
	public boolean delete(Provider provider);
}