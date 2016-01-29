package es.bsc.vmmclient.vmm;

import es.bsc.vmmclient.models.*;
import es.bsc.vmmclient.rest.VmmRestClient;

import java.util.ArrayList;
import java.util.List;

public class VmManagerClient implements VmManager {

    private final VmmRestClient vmmRestClient;

    public VmManagerClient(String url) {
        vmmRestClient = new VmmRestClient(url);
    }

    @Override
    public List<VmDeployed> getVms() {
        return vmmRestClient.getVmmService().getVms().getVms();
    }

    @Override
    public List<String> deployVms(List<Vm> vms) {
        List<String> result = new ArrayList<>();
        DeployVmsResponse deployVmsResponse = vmmRestClient.getVmmService().deployVms(new VmsList(vms));
        List<IdResponse> idResponses = deployVmsResponse.getIds();
        for (IdResponse idResponse: idResponses) {
            result.add(idResponse.getId());
        }
        return result;
    }

    @Override
    public VmDeployed getVm(String id) {
        return vmmRestClient.getVmmService().getVm(id);
    }

    @Override
    public void performActionOnVm(String id, VmAction action) {
        vmmRestClient.getVmmService().performActionOnVm(id, new VmActionQuery(action.toString()));
    }

    @Override
    public void destroyVm(String id) {
        vmmRestClient.getVmmService().destroyVm(id);
    }

    @Override
    public List<VmDeployed> getAppVms(String id) {
        return vmmRestClient.getVmmService().getAppVms(id).getVms();
    }

    @Override
    public void destroyAppVms(String id) {
        vmmRestClient.getVmmService().destroyAppVms(id);
    }

    @Override
    public List<ImageUploaded> getImages() {
        return vmmRestClient.getVmmService().getImages().getImages();
    }

    @Override
    public String uploadImage(ImageToUpload image) {
        return vmmRestClient.getVmmService().uploadImage(image).getId();
    }

    @Override
    public ImageUploaded getImage(String id) {
        return vmmRestClient.getVmmService().getImage(id);
    }

    @Override
    public void destroyImage(String id) {
        vmmRestClient.getVmmService().destroyImage(id);
    }

    @Override
    public List<Node> getNodes() {
        return vmmRestClient.getVmmService().getNodes().getNodes();
    }

    @Override
    public List<VmEstimate> getEstimates(List<VmToBeEstimated> vms) {
        return vmmRestClient.getVmmService().getEstimates(new VmsToBeEstimatedList(vms)).getEstimates();
    }

	@Override
	public List<VmCost> getCosts(List<String> vmIds) {
		return vmmRestClient.getVmmService().getCosts(vmIds);
	}

	@Override
	public void migrate(String vmId, String destinationHostName) {
		vmmRestClient.getVmmService().migrate(vmId, destinationHostName);
	}
}
