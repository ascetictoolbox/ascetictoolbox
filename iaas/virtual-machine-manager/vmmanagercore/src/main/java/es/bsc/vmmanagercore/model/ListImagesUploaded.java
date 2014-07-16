package es.bsc.vmmanagercore.model;

import java.util.List;

/**
 * List of uploaded images.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
// Note: At least for now, this class is only useful to make easier the conversion from JSON using Gson.
public class ListImagesUploaded {

    private List<ImageUploaded> images;

    public ListImagesUploaded(List<ImageUploaded> images) {
        this.images = images;
    }

    public List<ImageUploaded> getImages() {
        return images;
    }

}
