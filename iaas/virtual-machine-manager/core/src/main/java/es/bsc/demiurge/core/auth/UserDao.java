package es.bsc.demiurge.core.auth;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
public interface UserDao {
	int countUsers();

	void insertUser(String username, String plainPassword) throws SQLException;

	User loadUser(String username);

	boolean checkUser(String username, String plainPassword);
	void updateUserPassword(String username, String plainPassword) throws SQLException;
}
