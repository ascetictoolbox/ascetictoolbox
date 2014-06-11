package eu.ascetic.monitoring.api.datamodel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * The Class ItemTest.
 */
public class ItemTest {

	/**
	 * Pojo test.
	 */
	@Test
	public void pojoTest() {
		Item item = new Item("Available memory");
		item.setDelay("60");
		item.setHistory("7");
		item.setHostid("10084");
		item.setItemid("23298");
		item.setKey("vm.memory.size[available]");
		item.setLastClock(1351090998);
		item.setLastValue(2552);
		item.setTrends("365");
		
		assertEquals("Available memory", item.getName());
		assertEquals("60", item.getDelay());
		assertEquals("7", item.getHistory());
		assertEquals("10084", item.getHostid());
		assertEquals("23298", item.getItemid());
		assertEquals("vm.memory.size[available]", item.getKey());
		assertEquals(1351090998, item.getLastClock());
		assertEquals(2552, item.getLastValue());
		assertEquals("365", item.getTrends());
	}

}
