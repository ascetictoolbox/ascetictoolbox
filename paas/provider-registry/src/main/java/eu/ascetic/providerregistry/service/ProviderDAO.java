package eu.ascetic.providerregistry.service;

import java.util.List;

import eu.ascetic.providerregistry.model.Provider;

/**
 * DAO interface to perform database actions over a Provider object
 * @author David Garcia Perez - Atos
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