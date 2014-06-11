package es.bsc.vmmanagercore.scheduler;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import es.bsc.vmmanagercore.monitoring.HostInfoFake;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedulerDistributionTest {
	
	private SchedulerDistribution scheduler;
	
	@Before
	public void setUp() {
		scheduler = new SchedulerDistribution();
	}

	@Test 
	public void oneHostHasLessCpuLoad() {
		//create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
		//used={cpus=1, memory=2GB, disk=4GB}
		HostInfoFake hostInfo1 = new HostInfoFake("host1", 4, 4096, 8, 1, 2048, 4);
		
		//create a fake host with total={cpus=2, memory=4GB, disk=8GB} and
		//used={cpus=1, memory=2GB, disk=4GB}
		HostInfoFake hostInfo2 = new HostInfoFake("host2", 2, 4096, 8, 1, 2048, 4);
		
		//build the array of hosts that will be passed to the schedule function
		ArrayList<HostInfo> hostsInfo = new ArrayList<HostInfo>();
		hostsInfo.add(hostInfo1);
		hostsInfo.add(hostInfo2);
		
		//create a VM with {cpus=1, memory=1GB, disk=1GB}
		ArrayList<Vm> vmDescriptions = new ArrayList<Vm>();
		Vm vmDescription = 
				new Vm("TestVM1", "fakeImageId", 1, 1024, 1, null, "app1");
		vmDescriptions.add(vmDescription);
		
		//schedule the VM
		HashMap<Vm, String> scheduleResult = scheduler.schedule(
				vmDescriptions, hostsInfo);
		
		//after deploying the VM, host1 the CPU load of host1 would be 50% whereas the CPU load
		//of host2 would be 100%, therefore, host1 should be chosen
		assertEquals("host1", scheduleResult.get(vmDescription));
	}
	
	@Test
	public void oneHostHasLessMemory() {
		//create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
		//used={cpus=1, memory=2GB, disk=4GB}
		HostInfoFake hostInfo1 = new HostInfoFake("host1", 4, 4096, 8, 1, 2048, 4);
		
		//create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
		//used={cpus=1, memory=1GB, disk=4GB}
		HostInfoFake hostInfo2 = new HostInfoFake("host2", 4, 4096, 8, 1, 1024, 4);
		
		//build the array of hosts that will be passed to the schedule function
		ArrayList<HostInfo> hostsInfo = new ArrayList<HostInfo>();
		hostsInfo.add(hostInfo1);
		hostsInfo.add(hostInfo2);
		
		//create a VM with {cpus=1, memory=1GB, disk=1GB}
		ArrayList<Vm> vmDescriptions = new ArrayList<Vm>();
		Vm vmDescription = 
				new Vm("TestVM1", "fakeImageId", 1, 1024, 1, null, "app1");
		vmDescriptions.add(vmDescription);
		
		//schedule the VM
		HashMap<Vm, String> scheduleResult = scheduler.schedule(
				vmDescriptions, hostsInfo);
		
		//after deploying the VM, the CPU load of the two hosts should be the same. Also,
		//the memory load of host1 should be 75% whereas the memory load of host2 should be 50%, 
		//therefore, host2 should be chosen
		assertEquals("host2", scheduleResult.get(vmDescription));
	}
	
	@Test
	public void oneHostHasLessDisk() {
		//create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
		//used={cpus=1, memory=1GB, disk=4GB}
		HostInfoFake hostInfo1 = new HostInfoFake("host1", 4, 4096, 8, 1, 1024, 4);
		
		//create a fake host with total={cpus=4, memory=4GB, disk=8GB} and
		//used={cpus=1, memory=1GB, disk=2GB}
		HostInfoFake hostInfo2 = new HostInfoFake("host2", 4, 4096, 8, 1, 1024, 2);
		
		//build the array of hosts that will be passed to the schedule function
		ArrayList<HostInfo> hostsInfo = new ArrayList<HostInfo>();
		hostsInfo.add(hostInfo1);
		hostsInfo.add(hostInfo2);
		
		//create a VM with {cpus=1, memory=1GB, disk=1GB}
		ArrayList<Vm> vmDescriptions = new ArrayList<Vm>();
		Vm vmDescription = 
				new Vm("TestVM1", "fakeImageId", 1, 1024, 1, null, "app1");
		vmDescriptions.add(vmDescription);
		
		//schedule the VM
		HashMap<Vm, String> scheduleResult = scheduler.schedule(
				vmDescriptions, hostsInfo);
		
		//after deploying the VM, the CPU and memory load of the two hosts should be the same.
		//The disk load of host1 should be 75% whereas the disk load of host2 should be 50%, 
		//therefore, host2 should be chosen
		assertEquals("host2", scheduleResult.get(vmDescription));
	}
	
	@Test
	public void standardCaseWithThreeHosts() {
		//create a fake host with total={cpus=8, memory=4GB, disk=4GB} and
		//used={cpus=4, memory=1GB, disk=1GB}
		HostInfoFake hostInfo1 = new HostInfoFake("host1", 8, 4096, 4, 4, 1024, 1);
		
		//create a fake host with total={cpus=6, memory=4GB, disk=4GB} and
		//used={cpus=3, memory=1GB, disk=1GB}
		HostInfoFake hostInfo2 = new HostInfoFake("host2", 6, 4096, 4, 3, 1024, 1);
		
		//create a fake host with total={cpus=4, memory=4GB, disk=4GB} and
		//used={cpus=1, memory=1GB, disk=1GB}
		HostInfoFake hostInfo3 = new HostInfoFake("host3", 4, 4096, 4, 3, 3072, 3);
		
		//create a fake host with total={cpus=4, memory=4GB, disk=4GB} and
		//used={cpus=4, memory=4GB, disk=4GB}
		HostInfoFake hostInfo4 = new HostInfoFake("host4", 4, 4096, 4, 4, 4096, 4);
		
		//build the array of hosts that will be passed to the schedule function
		ArrayList<HostInfo> hostsInfo = new ArrayList<HostInfo>();
		hostsInfo.add(hostInfo1);
		hostsInfo.add(hostInfo2);
		hostsInfo.add(hostInfo3);
		hostsInfo.add(hostInfo4);
		
		//create a VM with {cpus=1, memory=1GB, disk=1GB}
		ArrayList<Vm> vmDescriptions = new ArrayList<Vm>();
		Vm vmDescription = 
				new Vm("TestVM1", "fakeImageId", 1, 1024, 1, null, "app1");
		vmDescriptions.add(vmDescription);
		
		//schedule the VM
		HashMap<Vm, String> scheduleResult = scheduler.schedule(
				vmDescriptions, hostsInfo);
		
		//after deploying the VM, the CPU load of host1 should be 75%, the load of host2 should be 
		//83%, the load of host3 should be 100%, and host4 does not have enough available resources.
		//Therefore, host2 should be chosen
		assertEquals("host1", scheduleResult.get(vmDescription));
	}
	
	@Test 
	public void noHostsWithEnoughResources() {
		//create a fake host with total={cpus=8, memory=4GB, disk=4GB} and
		//used={cpus=4, memory=1GB, disk=1GB}
		HostInfoFake hostInfo1 = new HostInfoFake("host1", 8, 4096, 4, 8, 1024, 1);
		
		//create a fake host with total={cpus=6, memory=4GB, disk=4GB} and
		//used={cpus=1, memory=4GB, disk=1GB}
		HostInfoFake hostInfo2 = new HostInfoFake("host2", 6, 4096, 4, 1, 4096, 1);
		
		//build the array of hosts that will be passed to the schedule function
		ArrayList<HostInfo> hostsInfo = new ArrayList<HostInfo>();
		hostsInfo.add(hostInfo1);
		hostsInfo.add(hostInfo2);
		
		//create a VM with {cpus=1, memory=1GB, disk=1GB}
		ArrayList<Vm> vmDescriptions = new ArrayList<Vm>();
		Vm vmDescription = 
				new Vm("TestVM1", "fakeImageId", 1, 1024, 1, null, "app1");
		vmDescriptions.add(vmDescription);
		
		//schedule the VM
		HashMap<Vm, String> scheduleResult = scheduler.schedule(
				vmDescriptions, hostsInfo);
		
		//make sure that the scheduling function returns null, because host1 does not have
		//enough CPUs available, and host2 does not have enough memory available
		assertNull(scheduleResult.get(vmDescription));
	}
	
	@Test
	public void reservation() {
		//create a fake host with total={cpus=2, memory=1GB, disk=2GB} and
		//used={cpus=0, memory=0GB, disk=0GB}
		HostInfoFake hostInfo1 = new HostInfoFake("host1", 2, 1024, 2, 0, 0, 0);

		//build the array of hosts that will be passed to the schedule function
		ArrayList<HostInfo> hostsInfo = new ArrayList<HostInfo>();
		hostsInfo.add(hostInfo1);
		
		//create two VMs with {cpus=2, memory=512MB, disk=1GB}
		ArrayList<Vm> vmDescriptions = new ArrayList<Vm>();
		Vm vmDescription1 = 
				new Vm("TestVM1", "fakeImageId", 2, 512, 1, null, "app1");
		vmDescriptions.add(vmDescription1);
		Vm vmDescription2 = 
				new Vm("TestVM2", "fakeImageId", 2, 512, 1, null, "app1");
		vmDescriptions.add(vmDescription2);
		
		//schedule the VMs
		HashMap<Vm, String> scheduleResult = scheduler.schedule(
				vmDescriptions, hostsInfo);
		
		//the first VM should be scheduled in the host1
		assertEquals("host1", scheduleResult.get(vmDescription1));
		
		//the second VM should not be scheduled, because the first one should have reserved
		//all the CPUs available in host1
		assertNull(scheduleResult.get(vmDescription2));
	}
}
