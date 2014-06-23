package eu.ascetic.paas.applicationmanager.dao;

import java.util.List;

import eu.ascetic.paas.applicationmanager.model.Application;


/**
 * DAO interface to perform database actions over an Application object
 * @author David Garcia Perez - Atos
 */
public interface ApplicationDAO extends DAO<Application> {

	/**
	 * Returns the Application from the database by its Id
	 * 
	 * @param id of the Application
	 * @return the corresponding Application from the database
	 */
	public Application getById(int id);

	/**
	 * Returns all the Application stored in the database
	 * 
	 * @return all the Application stored in the database
	 */
	public List<Application> getAll();

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
}