package es.bsc.demiurge.openstackjclouds;

import org.jclouds.ContextBuilder;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.neutron.v2.NeutronApiMetadata;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.NovaApiMetadata;
import org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi;
import org.jclouds.openstack.nova.v2_0.extensions.ServerAdminApi;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;

/**
 * This class contains the API connectors that JClouds defines to interact with OpenStack.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class OpenStackJcloudsApis {

    private final OpenStackCredentials openStackCredentials;
    private final NovaApi novaApi;
    private final ServerApi serverApi;
    private final ImageApi imageApi;
    private final FlavorApi flavorApi;
    private final ServerAdminApi serverAdminApi;
    private final FloatingIPApi floatingIpApi;

    public OpenStackJcloudsApis(OpenStackCredentials openStackCredentials) {

        this.openStackCredentials = openStackCredentials;
        novaApi = ContextBuilder.newBuilder(new NovaApiMetadata())
                .endpoint("http://" + openStackCredentials.getOpenStackIP() + ":" +
                        openStackCredentials.getKeyStonePort() + "/v2.0")
                .credentials(openStackCredentials.getKeyStoneTenant() + ":" +
                        openStackCredentials.getKeyStoneUser(), openStackCredentials.getKeyStonePassword())
                .buildApi(NovaApi.class);
        String zone = novaApi.getConfiguredZones().toArray()[0].toString(); // Assuming that there is only 1 zone
        serverApi = novaApi.getServerApiForZone(zone);
        imageApi = novaApi.getImageApiForZone(zone);
        flavorApi = novaApi.getFlavorApiForZone(zone);
        serverAdminApi = novaApi.getServerAdminExtensionForZone(zone).get();
        if (novaApi.getFloatingIPExtensionForZone(zone).isPresent()) {
            floatingIpApi = novaApi.getFloatingIPExtensionForZone(zone).get();
        }
        else {
            floatingIpApi = null;
        }
    }

    public NovaApi getNovaApi() {
        return novaApi;
    }

    public ServerApi getServerApi() {
        return serverApi;
    }

    public ImageApi getImageApi() {
        return imageApi;
    }

    public FlavorApi getFlavorApi() {
        return flavorApi;
    }

    public ServerAdminApi getServerAdminApi() {
        return serverAdminApi;
    }

    public FloatingIPApi getFloatingIpApi() {
        return floatingIpApi;
    }

    // Not always available. For now, call this function in OpenStack installations with Neutron enabled
    public NeutronApi getNeutronApi() {
        return ContextBuilder.newBuilder(new NeutronApiMetadata())
                .endpoint("http://" + openStackCredentials.getOpenStackIP() + ":" +
                        openStackCredentials.getKeyStonePort() + "/v2.0")
                .credentials(openStackCredentials.getKeyStoneTenant() + ":" +
                        openStackCredentials.getKeyStoneUser(), openStackCredentials.getKeyStonePassword())
                .buildApi(NeutronApi.class);
    }

	public void resize(String vmId, String flavourId) {
		String v3Endpoint = "http://" + openStackCredentials.getOpenStackIP() + ":" +
				openStackCredentials.getKeyStonePort() + "/v2.0";

	}

}
