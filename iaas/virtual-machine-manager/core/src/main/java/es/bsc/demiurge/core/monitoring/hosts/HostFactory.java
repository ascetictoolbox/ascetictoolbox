/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.core.monitoring.hosts;

import com.google.gson.Gson;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddleware;
import es.bsc.demiurge.core.drivers.Monitoring;

import java.util.HashMap;
import java.util.Map;

/**
 * Host factory
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class HostFactory {

    private Map<String, Host> hosts = new HashMap<>(); // List of hosts already created

    private static final Gson gson = new Gson();

	private CloudMiddleware cloudMiddleware;
	private Monitoring<Host> monitoring;

	public HostFactory(CloudMiddleware cloudMiddleware, Monitoring monitoring) {
		this.cloudMiddleware = cloudMiddleware;
		this.monitoring = monitoring;
	}

	/**
     * Returns a host given a hostname, a type of host, and the openStackJclouds connector
     *
     * @param hostname the hostname
     * @return the host
     */
    public Host getHost(String hostname) {

        // If host type is fake and fake hosts have not been generated, generate them
//        if (type == HostType.FAKE && !fakeHostsGenerated) {
//            generateFakeHosts((FakeCloudMiddleware) cloudMiddleware);
//            fakeHostsGenerated = true;
//        }

        // If the host already exists, return it
		Host host = (Host) hosts.get(hostname);
        if (host != null) {
//            if (host instanceof HostOpenStack) {
//                assert(cloudMiddleware instanceof OpenStackJclouds);
//                ((HostOpenStack) host).setOpenStackJclouds((OpenStackJclouds) cloudMiddleware);
//            }
            host.refreshMonitoringInfo();
            return host;
        }

        // If the host does not already exist, create and return it.
        // If the type is Fake, this switch will not be called, because all the fake hosts are created beforehand.
        // This is because we do not want to have to read the description file more than once.
		Host newHost = monitoring.createHost(hostname);
        hosts.put(hostname, newHost);
        return newHost;
    }

    /**
     * Generates fake hosts read from a JSON file.
     * The hosts generated are added to the fake middleware connector received.
     *
     * @param fakeCloudMiddleware fake cloud middleware connector
     */
/*    private void generateFakeHosts(FakeCloudMiddleware fakeCloudMiddleware) {
        BufferedReader bReader = new BufferedReader(new InputStreamReader(
                HostFactory.class.getResourceAsStream(FAKE_HOSTS_DESCRIPTIONS_PATH)));
        List<HostFake> hostsFromFile = Arrays.asList(gson.fromJson(bReader, HostFake[].class));
        for (Host host: hostsFromFile) {
            HostFake hostFake = new HostFake(host.getHostname(),
                    host.getTotalCpus(),
                    (int) host.getTotalMemoryMb(),
                    (int) host.getTotalDiskGb(),
                    0, 0, 0);

            hosts.put(host.getHostname(), hostFake);
            fakeCloudMiddleware.addHost(hostFake);
        }
        try {
            bReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}
