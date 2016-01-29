package es.bsc.power_button_presser.config;

import es.bsc.power_button_presser.strategies.Strategy;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ConfigTest {
    
    @Test
    public void gettersTest() {
        Config config = new Config(10, Strategy.ALL_SERVERS_ON, "a", "b", "c", "d", "e");
        assertEquals(10, config.getIntervalSeconds());
        assertEquals(Strategy.ALL_SERVERS_ON, config.getStrategy());
        assertEquals("a", config.getVmmBaseUrl());
        assertEquals("b", config.getVmmVmsPath());
        assertEquals("c", config.getVmmHostsPath());
        assertEquals("d", config.getVmmNodePath());
        assertEquals("e", config.getVmmPowerButtonPath());
    }
    
}
