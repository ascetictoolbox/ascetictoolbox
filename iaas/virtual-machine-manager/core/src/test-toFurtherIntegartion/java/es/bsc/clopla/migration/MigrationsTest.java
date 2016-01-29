package es.bsc.demiurge.core.clopla.migration;

import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.domain.Vm;
import org.junit.Before;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mmacias on 26/10/15.
 */
@Ignore
public class MigrationsTest {
	private List<Vm> vms;
	private List<Host> hosts;
	@Before
	public void initTestbed() {
		// Create a list of VMs that contains a VM with id = 1, cpus = 2, ramMb = 1024, and diskGb = 4
		vms = new ArrayList<>();
		Vm vm = new Vm.Builder((long) 1, 2, 1024, 4).build();
		vms.add(vm);
		// Instantiate a lists of hosts that contains a host with id = 1, hostname = myHost, cpus = 4, ramMb = 8192,
		// diskGb=100, and that is on
		List<Host> hosts = new ArrayList<>();
		Host host = new Host((long) 1, "myHost", 4, 8192, 100, false);
		hosts.add(host);
	}

}
