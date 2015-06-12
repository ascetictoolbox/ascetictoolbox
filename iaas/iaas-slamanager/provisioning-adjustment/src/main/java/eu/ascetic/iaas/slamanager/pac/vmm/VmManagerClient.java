package eu.ascetic.iaas.slamanager.pac.vmm;

import java.util.ArrayList;
import java.util.List;

import eu.ascetic.iaas.slamanager.pac.vmm.models.DeployVmsResponse;
import eu.ascetic.iaas.slamanager.pac.vmm.models.IdResponse;
import eu.ascetic.iaas.slamanager.pac.vmm.models.ImageToUpload;
import eu.ascetic.iaas.slamanager.pac.vmm.models.ImageUploaded;
import eu.ascetic.iaas.slamanager.pac.vmm.models.Node;
import eu.ascetic.iaas.slamanager.pac.vmm.models.Vm;
import eu.ascetic.iaas.slamanager.pac.vmm.models.VmAction;
import eu.ascetic.iaas.slamanager.pac.vmm.models.VmActionQuery;
import eu.ascetic.iaas.slamanager.pac.vmm.models.VmDeployed;
import eu.ascetic.iaas.slamanager.pac.vmm.models.VmEstimate;
import eu.ascetic.iaas.slamanager.pac.vmm.models.VmToBeEstimated;
import eu.ascetic.iaas.slamanager.pac.vmm.models.VmsList;
import eu.ascetic.iaas.slamanager.pac.vmm.models.VmsToBeEstimatedList;

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

}