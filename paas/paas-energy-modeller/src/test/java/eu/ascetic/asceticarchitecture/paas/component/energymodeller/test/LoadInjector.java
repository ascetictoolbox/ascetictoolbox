package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EMSettings;
import eu.ascetic.asceticarchitecture.paas.component.loadinjector.service.LoadInjectorService;

public class LoadInjector {

	private static LoadInjectorService theService;

	
	@BeforeClass
	public static void setup() {
		theService = new LoadInjectorService();
		Properties props = new Properties();
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("testconfig.properties");
		try {
			props.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		EMSettings settings = new EMSettings(props);	
		theService.configureLoadInjector(settings.getServerPath(), settings.getServerurl(), settings.getPropertyFile(), settings.getJmxFilePath());
		
	}
	
	
	@Test
	public void testEnergyModellerApplicationConsumption() {
		//theService.runTestFromFile("loadtest.jmx");
		assertTrue(theService.runTestFromFile("loadtest.jmx",10000));
	}
}
