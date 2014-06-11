package eu.ascetic.monitoring.api.datamodel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * The Class UserTest.
 */
public class UserTest {

	/**
	 * Pojo test.
	 */
	@Test
	public void pojoTest() {
		User user = new User();
		user.setLogin("Admin");
		user.setPassword("zabbix");
		user.setUserid("1");
		user.setAuth("cc911407fd49dcea238da2654d5ee929");
		
		assertEquals("Admin", user.getLogin());
		assertEquals("zabbix", user.getPassword());
		assertEquals("cc911407fd49dcea238da2654d5ee929", user.getAuth());
		assertEquals("1", user.getUserid());
	}
}
