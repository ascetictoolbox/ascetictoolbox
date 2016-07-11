package integratedtoolkit.connectors.ascetic;

import java.util.HashMap;

import eu.ascetic.saas.application_uploader.ApplicationUploader;
import eu.ascetic.saas.application_uploader.ApplicationUploaderException;
import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.ascetic.Configuration;
import integratedtoolkit.ascetic.VM;
import integratedtoolkit.connectors.AbstractSSHConnector;
import integratedtoolkit.connectors.ConnectorException;
import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;



public class AsceticConnector extends AbstractSSHConnector {

	private ApplicationUploader appUploaderClient;
	private String server;
	private String applicationId;
	private String deploymentId;
    private static final long POLLING_INTERVAL = 5;
    private static final int TIMEOUT = 1800;
	
	public AsceticConnector(String providerName, HashMap<String, String> props) {
		super(providerName, props);
		//super.setDefaultUser("root");
        server = props.get("Server");
		appUploaderClient = new ApplicationUploader(server);
		applicationId = Configuration.getApplicationId();
		deploymentId = Configuration.getDeploymentId();
	}

	@Override
	public void destroy(Object envId) throws ConnectorException {
		try {
			eu.ascetic.paas.applicationmanager.model.VM vmd = appUploaderClient
					.getVM(applicationId, deploymentId, ((Integer) envId).toString());
			
			appUploaderClient.deleteVM(applicationId, deploymentId, ((Integer) envId).toString());
			Ascetic.removeResource(vmd.getIp());
		} catch (ApplicationUploaderException e) {
			logger.error("Error deleting VM "+ envId, e);
			throw new ConnectorException(e);
			
		}
		
	}

	@Override
	public Object create(String name, CloudMethodResourceDescription rd)
			throws ConnectorException {
		try {
			eu.ascetic.paas.applicationmanager.model.VM vm = appUploaderClient.addNewVM(applicationId, deploymentId, rd.getType());
			return vm.getId();
		} catch (ApplicationUploaderException e) {
			logger.error("Error creating VM "+ rd.getType() +" for application "+ applicationId +" and deployment "+ deploymentId,e);
			throw new ConnectorException(e);
		}
	}

	@Override
	public CloudMethodResourceDescription waitUntilCreation(Object envId,
			CloudMethodResourceDescription requested) throws ConnectorException {
		CloudMethodResourceDescription granted = new CloudMethodResourceDescription();

		try {
			eu.ascetic.paas.applicationmanager.model.VM vmd = appUploaderClient
					.getVM(applicationId, deploymentId, ((Integer) envId).toString());

			logger.info("VM State is " + vmd.getStatus().toString());
			int tries = 0;
			while (vmd.getStatus() == null || !vmd.getStatus().equals("ACTIVE")) {

				if (vmd.getStatus().equals("ERROR")) {
					logger.error("Error waiting for VM Creation. Middleware has return an error state");
					throw new ConnectorException(
							"Error waiting for VM Creation. Middleware has return an error state");
				}
				if (tries * POLLING_INTERVAL > TIMEOUT) {
					throw new ConnectorException(
							"Maximum VM creation time reached.");
				}

				tries++;

				try {
					Thread.sleep(POLLING_INTERVAL * 1000);
				} catch (InterruptedException e) {
					// ignore
				}
				vmd = appUploaderClient.getVM(applicationId, deploymentId, ((Integer) envId).toString());
				 
			}
			String ip = vmd.getIp();
			granted.setName(ip);

			granted.setType(requested.getType());
			granted.setProcessorCPUCount(vmd.getCpuActual());
			granted.setProcessorCoreCount(vmd.getCpuActual());
			granted.setProcessorArchitecture(requested
					.getProcessorArchitecture());
			granted.setProcessorSpeed(requested.getProcessorSpeed());
			granted.setMemoryPhysicalSize(vmd.getRamActual() / 1024);
			granted.setMemoryAccessTime(requested.getMemoryAccessTime());
			granted.setMemorySTR(requested.getMemorySTR());
			granted.setMemoryVirtualSize(requested.getMemoryVirtualSize());
			granted.setStorageElemSize(vmd.getDiskActual());
			granted.setStorageElemAccessTime(requested
					.getStorageElemAccessTime());
			granted.setStorageElemSTR(requested.getStorageElemSTR());

			granted.setOperatingSystemType("Linux");
			granted.setSlots(requested.getSlots());

			granted.getAppSoftware().addAll(requested.getAppSoftware());
			granted.setImage(requested.getImage());
			granted.setValue(requested.getValue());
			granted.setValue(getMachineCostPerTimeSlot(granted));
			Ascetic.addNewResource(new VM(vmd));
			return granted;
		} catch (Exception e) {
			logger.error("Exception getting VM" + envId, e);
			throw new ConnectorException(e);
		}
	}

	@Override
	public float getMachineCostPerTimeSlot(CloudMethodResourceDescription rd) {
		return 0;
	}

	@Override
	public long getTimeSlot() {
		return FIVE_MIN;
	}

}
