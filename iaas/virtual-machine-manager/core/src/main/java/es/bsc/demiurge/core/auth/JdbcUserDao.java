package es.bsc.demiurge.core.auth;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.*;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
public class JdbcUserDao implements UserDao {
	private Connection connection;

	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	private Logger log = LogManager.getLogger(JdbcUserDao.class);

	public JdbcUserDao(Connection connection) {
		this.connection = connection;
	}

	@Override
	public int countUsers() {
		try (Statement stmt = connection.createStatement()) {
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS numberUsers FROM users");
			rs.next();
			return rs.getInt("numberUsers");
		} catch(Exception e) {
			log.error(e.getMessage(),e);
			return -1;
		}

	}

	@Override
	public void insertUser(String username, String plainPassword) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO users(username,password,enabled) VALUES (?,?,?)")) {
			stmt.setString(1, username);
			stmt.setString(2, encoder.encode(plainPassword));
			stmt.setBoolean(3, true);
			log.debug("Inserting user. ExecuteUpdate result: " + stmt.executeUpdate());
			connection.commit();
		}
	}

	@Override
	public User loadUser(String username){
		try(PreparedStatement stmt = connection.prepareStatement("SELECT password, enabled FROM users WHERE username = ?")) {
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			if(!rs.next()) {
				return null;
			} else {
				return new User(username, rs.getString(1), rs.getBoolean(2));
			}
		} catch(SQLException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public boolean checkUser(String username, String plainPassword) {
		User user = loadUser(username);
		return user != null && user.isEnabled() && encoder.matches(plainPassword,user.getCipheredPassword());
	}

	@Override
	public void updateUserPassword(String username, String plainPassword) throws SQLException {
		try(PreparedStatement stmt = connection.prepareStatement("UPDATE users SET password=? WHERE username=?")) {
			stmt.setString(1, encoder.encode(plainPassword));
			stmt.setString(2, username);
			stmt.executeUpdate();
			connection.commit();
		}
	}
}
