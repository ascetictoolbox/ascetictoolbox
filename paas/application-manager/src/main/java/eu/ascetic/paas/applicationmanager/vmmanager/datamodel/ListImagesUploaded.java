package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;

import java.util.List;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ListImagesUploaded {

    private List<ImageUploaded> images;

    public ListImagesUploaded(List<ImageUploaded> images) {
        this.images = images;
    }
    
    public List<ImageUploaded> getImages() {
        return images;
    }

}
