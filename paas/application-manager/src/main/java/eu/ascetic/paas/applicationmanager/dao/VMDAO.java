package eu.ascetic.paas.applicationmanager.dao;

import java.util.List;

import eu.ascetic.paas.applicationmanager.model.VM;

/**
 * DAO interface to perform database actions over an Deployment object
 * @author David Garcia Perez - Atos
 */
public interface VMDAO extends DAO<VM> {
	/**
	 * Returns the VM from the database by its Id
	 * 
	 * @param id of the VM
	 * @return the corresponding VM from the database
	 */
	public VM getById(int id);

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
}
