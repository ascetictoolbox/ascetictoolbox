package eu.ascetic.paas.applicationmanager.dao;

import java.util.List;

import eu.ascetic.paas.applicationmanager.model.Deployment;

/**
 * DAO interface to perform database actions over an Deployment object
 * @author David Garcia Perez - Atos
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
}
