package es.bsc.vmmclient.vmm;

import es.bsc.vmmclient.models.*;
import es.bsc.vmmclient.rest.VmmRestClient;

import java.util.ArrayList;
import java.util.List;

public class VmManagerClient implements VmManager {

    public VmManagerClient() { }

    @Override
    public List<VmDeployed> getVms() {
        return VmmRestClient.getVmmService().getVms().getVms();
    }

    @Override
    public List<String> deployVms(List<Vm> vms) {
        List<String> result = new ArrayList<>();
        List<IdResponse> idResponses = VmmRestClient.getVmmService().deployVms(new VmsList(vms)).getIds();
        for (IdResponse idResponse: idResponses) {
            result.add(idResponse.getId());
        }
        return result;
    }

    @Override
    public VmDeployed getVm(String id) {
        return VmmRestClient.getVmmService().getVm(id);
    }

    @Override
    public void performActionOnVm(String id, VmAction action) {
        VmmRestClient.getVmmService().performActionOnVm(id, new VmActionQuery(action.toString()));
    }

    @Override
    public void destroyVm(String id) {
        VmmRestClient.getVmmService().destroyVm(id);
    }

    @Override
    public List<VmDeployed> getAppVms(String id) {
        return VmmRestClient.getVmmService().getAppVms(id).getVms();
    }

    @Override
    public void destroyAppVms(String id) {
        VmmRestClient.getVmmService().destroyAppVms(id);
    }

    @Override
    public List<ImageUploaded> getImages() {
        return VmmRestClient.getVmmService().getImages().getImages();
    }

    @Override
    public String uploadImage(ImageToUpload image) {
        return VmmRestClient.getVmmService().uploadImage(image).getId();
    }

    @Override
    public ImageUploaded getImage(String id) {
        return VmmRestClient.getVmmService().getImage(id);
    }

    @Override
    public void destroyImage(String id) {
        VmmRestClient.getVmmService().destroyImage(id);
    }

    @Override
    public List<Node> getNodes() {
        return VmmRestClient.getVmmService().getNodes().getNodes();
    }

}
