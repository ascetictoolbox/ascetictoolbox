package es.bsc.vmmanagercore.cloudmiddleware.openstack;

import org.jclouds.ContextBuilder;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.NovaApiMetadata;
import org.jclouds.openstack.nova.v2_0.extensions.ServerAdminApi;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;

/**
 * This class contains the API connectors that JClouds defines to interact with OpenStack.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class OpenStackJcloudsApis {

    private final NovaApi novaApi;
    private final ServerApi serverApi;
    private final ImageApi imageApi;
    private final FlavorApi flavorApi;
    private final ServerAdminApi serverAdminApi;

    public OpenStackJcloudsApis(OpenStackCredentials openStackCredentials) {
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

}
