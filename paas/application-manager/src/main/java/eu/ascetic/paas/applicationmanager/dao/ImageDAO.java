package eu.ascetic.paas.applicationmanager.dao;

import java.util.List;

import eu.ascetic.paas.applicationmanager.model.Image;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * @email david.garciaperez@atos.net 
 * 
 * DAO interface to perform database actions over an Image object
 *
 */
public interface ImageDAO extends DAO<Image> {
	/**
	 * Returns the Image from the database by its Id
	 * 
	 * @param id of the Image
	 * @return the corresponding Image from the database
	 */
	public Image getById(int id);

	/**
	 * Returns all the Image stored in the database
	 * 
	 * @return all the VM stored in the database
	 */
	public List<Image> getAll();

	/**
	 * Stores a Image into the database
	 * 
	 * @param Image to be saved.
	 * @return <code>true</code> if the Image was saved correctly
	 */
	public boolean save(Image image);

	/**
	 * Updates a Image in the database
	 * 
	 * @param Image to be updated
	 * @return <code>true</code> if the Image was updated correctly
	 */
	public boolean update(Image image);

	/**
	 * Deletes a Image from the database
	 * 
	 * @param VM to be deleted
	 * @return <code>true</code> if the Image was deleted correctly
	 */
	public boolean delete(Image image);
	
	/**
	 * Selects the last created image with an specific ovf-id
	 * 
	 * @param ovf-id
	 * @return the last image with that ovf-id
	 */
	public Image getLastImageWithOvfId(String ovfId);

	public Image getDemoCacheImage(String string, String string2);
}
