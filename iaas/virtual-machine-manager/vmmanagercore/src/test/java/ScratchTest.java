import es.bsc.vmmanagercore.configuration.VmManagerConfiguration;
import es.bsc.vmmanagercore.monitoring.zabbix.ZabbixConnector;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.*;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ScratchTest {
	@BeforeClass
	public static void setupVMConfig() {
		System.setProperty("config", "configTest.properties");
	}

	@Ignore
	@Test
	public void testZabbixScratch() {
		System.out.println("****** HOSTS *****");
		for(Host h : ZabbixConnector.getZabbixClient().getAllHosts()) {
			System.out.println("h.getHost() = " + h.getHost());
			System.out.println("h.getHostid() = " + h.getHostid());
			System.out.println("h.getAvailable() = " + h.getAvailable());
			System.out.println("h.getName() = " + h.getName());
			System.out.println();
		}
	}
}
