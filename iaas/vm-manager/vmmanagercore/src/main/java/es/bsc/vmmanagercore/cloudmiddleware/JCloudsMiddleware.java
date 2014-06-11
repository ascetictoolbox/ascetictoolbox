package es.bsc.vmmanagercore.cloudmiddleware;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.UrlValidator;
import org.jclouds.ContextBuilder;
import org.jclouds.openstack.nova.v2_0.domain.Address;
import org.jclouds.openstack.nova.v2_0.domain.Flavor;
import org.jclouds.openstack.nova.v2_0.domain.Image;
import org.jclouds.openstack.nova.v2_0.domain.RebootType;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
import org.jclouds.openstack.nova.v2_0.domain.ServerExtendedStatus;
import org.jclouds.openstack.nova.v2_0.extensions.ServerAdminApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.NovaApiMetadata;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;

import com.google.common.base.Optional;

import es.bsc.vmmanagercore.db.VmManagerDb;
import es.bsc.vmmanagercore.manager.VmManagerConfiguration;
import es.bsc.vmmanagercore.model.ImageToUpload;
import es.bsc.vmmanagercore.model.ImageUploaded;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmDeployed;


/**
 * Class that performs requests to OpenStack using the JClouds library.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class JCloudsMiddleware implements CloudMiddleware {
	
	//OpenStack default flavors
	public static final String[] DEFAULT_FLAVORS = new String[] {"1","2","3","4","5"};
	
	//OpenStack VMs state
	private static final String ACTIVE = "active";
	private static final String BUILD = "BUILD";
	private static final String DELETING = "deleting";
	
	//OpenStack host resources
	public final String TOTAL_RESOURCES = "(total)";
	public final String USED_RESOURCES = "(used_now)";
	
	//needed by JClouds
	private NovaApi novaApi;
	private Set<String> zones;
	
	private String[] hosts; //hosts in the cluster
	private OpenStackGlance glanceConnector = new OpenStackGlance(); //connector for OS Glance
	private VmManagerDb db; //DB that contains the relationship VM-application
	
    /**
     * Class constructor.
     * @param db Database used by the VM Manager
     */
	public JCloudsMiddleware(VmManagerDb db) {
		//connect to the infrastructure and initialize JClouds attributes
		VmManagerConfiguration conf = VmManagerConfiguration.getInstance();
		novaApi = ContextBuilder.newBuilder(new NovaApiMetadata())
				.endpoint("http://" + conf.openStackIP + ":" + conf.keyStonePort + "/v2.0")
				.credentials(conf.keyStoneTenant + ":" + conf.keyStoneUser, conf.keyStonePassword)
				.buildApi(NovaApi.class);
		zones = novaApi.getConfiguredZones();
		hosts = conf.hosts;
		this.db = db;
	}
	
	@Override
	public String deploy(Vm vmDescription, String dstNode) {
		String vmId = null;
		
		for (String zone: zones) {
			//TODO for now I assume that there is only one zone called "regionOne"
			if ("regionOne".equals(zone)) {
				
				//specs of the flavor
				int cpus = vmDescription.getCpus();
				int ram = vmDescription.getRamMb();
				int disk = vmDescription.getDiskGb();
				
				//check if exists a flavor with the same specs as the one we need to use
				String flavorId = getFlavorId(zone, cpus, ram, disk);
				
				//create the flavor if it does not exist
				if (flavorId == null) {
					String id, name;
					id = name = cpus + "-" + disk + "-" + ram;
					flavorId = createFlavor(zone, id, name, cpus, ram, disk);
				}
				
				//specify the node on which the VM needs to be deployed
				CreateServerOptions options = new CreateServerOptions();
				if (dstNode != null) {
					options.availabilityZone("nova:" + dstNode);
				}
				
				//specify an init-script
				String initScript = vmDescription.getInitScript();
				if (initScript != null) {
					InputStream inputStream = 
							getClass().getClassLoader().getResourceAsStream(initScript);
					try {
						options.userData(IOUtils.toByteArray(inputStream));
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
				
				//check whether the user specified an image ID or a URL containing the image
				String imageId;
				UrlValidator urlValidator = new UrlValidator();
				//if it is a URL
				if (urlValidator.isValid(vmDescription.getImage())) { 
					//create the image in Glance
					imageId = glanceConnector.createImageFromUrl(new ImageToUpload(
							vmDescription.getImage(), vmDescription.getImage()));
				}
				//if it is an ID
				else { 
					imageId = vmDescription.getImage();
					//throw an exception if the ID is not valid
					if (!existsImageWithId(imageId)) {
						throw new IllegalArgumentException("There is not an image with the"
								+ " specified ID");
					}
					//throw an exception if the image is not active
					if (!glanceConnector.imageIsActive(imageId)) {
						throw new IllegalArgumentException("The image specified is not active");
					}
				}
				
				//deploy the VM
				ServerApi serverApi = novaApi.getServerApiForZone(zone);
				ServerCreated server = serverApi.create(vmDescription.getName(), 
						imageId, flavorId, options);
					
				//wait until the VM is deployed
				while (serverApi.get(server.getId()).getStatus().toString().equals(BUILD));
				
				//get the VM id
				vmId = server.getId();
			}
		}
		return vmId;
	}

	@Override
	public void destroy(String vmId) {
		for (String zone: zones) {
			ServerApi serverApi = novaApi.getServerApiForZone(zone);
			Server server = serverApi.get(vmId);
			if (server != null) { //if the VM is in the zone
				serverApi.delete(vmId); // Delete the VM
				while (server.getStatus().toString().equals(DELETING)); // Wait while deleting
			}
		}
	}

	@Override
	public void migrate(String vmId, String destinationNode) {
		for (String zone: zones) {
			ServerApi serverApi = novaApi.getServerApiForZone(zone);
			Server server = serverApi.get(vmId);
			
			//if the server is in the zone
			if (server != null) {
				
				//get the API with admin functions
				Optional<? extends ServerAdminApi> serverAdminApi = 
						novaApi.getServerAdminExtensionForZone(zone);
				
				//live-migrate the VM to the destination node
				serverAdminApi.get().liveMigrate(vmId, destinationNode, false, false);
				
			}
		}
	}
	
	@Override
	public boolean existsVm(String vmId) {
		for (String zone: zones) {
			if (novaApi.getServerApiForZone(zone).get(vmId) != null) {
				return true;
			}
		}
		return false;
	}
	
	@Override
    public void rebootHardVm(String vmId) {
		for (String zone: zones) {
			novaApi.getServerApiForZone(zone).reboot(vmId, RebootType.HARD);
		}
	}
    
	@Override
    public void rebootSoftVm(String vmId) {
		for (String zone: zones) {
			novaApi.getServerApiForZone(zone).reboot(vmId, RebootType.SOFT);
		}
    }
    
	@Override
    public void startVm(String vmId) {
		for (String zone: zones) {
			novaApi.getServerApiForZone(zone).start(vmId);
		}
    }

	@Override
    public void stopVm(String vmId) {
		for (String zone: zones) {
			novaApi.getServerApiForZone(zone).stop(vmId);
		}
    }
	
	@Override
	public void suspendVm(String vmId) {
		for (String zone: zones) {
			novaApi.getServerAdminExtensionForZone(zone).get().suspend(vmId);
		}
	}
	
	@Override
	public void resumeVm(String vmId) {
		for (String zone: zones) {
			novaApi.getServerAdminExtensionForZone(zone).get().resume(vmId);
		}
	}

	@Override
	public Collection<String> getAllVMsId() {
		ArrayList<String> vmIds = new ArrayList<>();
		for (String zone: zones) {
			ServerApi serverApi = novaApi.getServerApiForZone(zone);
			for (Server server: serverApi.listInDetail().concat()) {
				ServerExtendedStatus vmStatus = server.getExtendedStatus().get();
				String vmState = vmStatus.getVmState();
				String vmTask = vmStatus.getTaskState();
				
				//add the VM to the result if it is active and it is not being deleted
				if (ACTIVE.equals(vmState) && !(DELETING.equals(vmTask))) {
					vmIds.add(server.getId());
				}
				
			}
		}
		return vmIds;
	}

	@Override
	public VmDeployed getVMInfo(String vmId) {
		VmDeployed vmDescription = null;
		
		for (String zone: zones) {
			ServerApi serverApi = novaApi.getServerApiForZone(zone);
			Server server = serverApi.get(vmId);
			
			//if the VM is in the zone
			if (server != null ) {
				
				//check the state of the VM
				ServerExtendedStatus vmStatus = server.getExtendedStatus().get();
				String vmState = vmStatus.getVmState();
				String vmTask = vmStatus.getTaskState();
				
				//if the VM is active and is not being deleted
				if (ACTIVE.equals(vmState) && !(DELETING.equals(vmTask))) {
					
					//get the information of the VM
					FlavorApi flavorApi = novaApi.getFlavorApiForZone(zone);
					Flavor flavor = flavorApi.get(server.getFlavor().getId());
					String vmIp = ((Address) server.getAddresses().get("vmnet").toArray()[0])
							.getAddr();
					vmDescription = new VmDeployed(server.getName(), 
							server.getImage().getId(), flavor.getVcpus(), flavor.getRam(), 
							flavor.getDisk(), null, db.getAppIdOfVm(vmId), vmId, 
							vmIp, server.getStatus().toString(), server.getCreated());
					
				}
			}
		}
		
		return vmDescription;
	}
	
	@Override
	public Collection<ImageUploaded> getVmImages() {
		ArrayList<ImageUploaded> vmImages = new ArrayList<>();
		for (String zone: zones) {
			ImageApi imageApi = novaApi.getImageApiForZone(zone);
			for (Image image: imageApi.listInDetail().concat()) {
				vmImages.add(new ImageUploaded(image.getId(), image.getName(), 
						image.getStatus().toString()));
			}
		}
		return vmImages;
	}
	
	@Override
	public ImageUploaded getVmImage(String imageId) {
		ImageUploaded imageDescription = null;
		for (String zone: zones) {
			ImageApi imageApi = novaApi.getImageApiForZone(zone);
			Image image = imageApi.get(imageId);
			imageDescription = new ImageUploaded(image.getId(), image.getName(), 
					image.getStatus().toString());
		}
		return imageDescription;
	}
	
	@Override
	public String createVmImage(ImageToUpload imageToUpload) {
		return glanceConnector.createImageFromUrl(imageToUpload);
	}
	
	@Override
	public void deleteVmImage(String id) {
		glanceConnector.deleteImage(id);
	}

	
	/**
	 * @return configured zones of the infrastructure
	 */
	public Set<String> getZones() {
		return zones;
	}
	
	/**
	 * @return NovaApi object associated with the JCloudsMiddleware class
	 */
	public NovaApi getNovaApi() {
		return novaApi;
	}
	
	/**
	 * @return array containing the host names of the servers of the cluster
	 */
	public String[] getHosts() {
		return hosts;
	}
	
	/**
	 * Gets the ID of the flavor with the specified characteristics.
	 * @param zone the zone where the flavor is located
	 * @param cpus the number of CPUs of the flavor
	 * @param memoryMb the amount of RAM of the flavor in MB
	 * @param diskGb the amount of disk space of the flavor in GB
	 * @return The ID of the flavor. Null if a flavor with the specified characteristics does not
	 * exist.
	 */
	private String getFlavorId(String zone, int cpus, int memoryMb, int diskGb) {
		FlavorApi flavorApi = novaApi.getFlavorApiForZone(zone);
		for (Flavor flavor: flavorApi.listInDetail().concat()) {
			if (flavor.getVcpus() == cpus && flavor.getRam() == memoryMb && 
					flavor.getDisk() == diskGb) {
				return flavor.getId();
			}
		}
		return null;
	}
	
	/**
	 * Creates a flavor with the specified characteristics.
	 * @param zone the zone where the flavor needs to be located
	 * @param cpus the number of CPUs of the flavor
	 * @param ramMb the amount of RAM of the flavor in MB
	 * @param diskGb the amount of disk space of the flavor in GB
	 * @return The ID of the created flavor.
	 */
	private String createFlavor(String zone, String id, String name, int cpus, int ramMb, 
			int diskGb) {
		FlavorApi flavorApi = novaApi.getFlavorApiForZone(zone);
		Flavor flavor = Flavor.builder().id(id).name(name).
				vcpus(cpus).ram(ramMb).disk(diskGb).build();
		flavorApi.create(flavor);
		return id;
	}
	
	private boolean existsImageWithId(String id) {
		for (String zone: zones) {
			ImageApi imageApi = novaApi.getImageApiForZone(zone);
			if (imageApi.get(id) != null) {
				return true;
			}
		}
		return false;
	}
	
}