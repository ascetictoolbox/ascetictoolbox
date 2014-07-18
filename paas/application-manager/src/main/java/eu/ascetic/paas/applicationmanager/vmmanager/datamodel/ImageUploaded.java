package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;
/**
 * Image that has been uploaded to the cloud middleware.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class ImageUploaded {

    private String id;
    private String name;
    private String status;

    public ImageUploaded(String id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
