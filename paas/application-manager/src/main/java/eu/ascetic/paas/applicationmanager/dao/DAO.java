package eu.ascetic.paas.applicationmanager.dao;

import java.util.List;

/**
* Describes the generic Data Access Object 
* It is used by Spring to do the specific DAO injections
* @author David Garcia Perez - AtoS
*/
public interface DAO<T> {
	public boolean save(T t);
	public List<T> getAll();
	public T getById(int id);
	public boolean delete(T something);
	public boolean update(T something);
}
