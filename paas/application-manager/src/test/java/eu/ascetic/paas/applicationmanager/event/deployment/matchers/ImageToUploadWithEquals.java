package eu.ascetic.paas.applicationmanager.event.deployment.matchers;

import es.bsc.vmmclient.models.ImageToUpload;

public class ImageToUploadWithEquals extends ImageToUpload {
	private String name;
	private String url;

	public ImageToUploadWithEquals(String name, String url) {
		super(name, url);
		
		this.name = name;
		this.url = url;
	}
	
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImageToUpload))
             return false;
         if (obj == this)
             return true;

         ImageToUpload imageToUpload = (ImageToUpload) obj;
         if(this.name.equals(imageToUpload.getName()) && this.url.equals(imageToUpload.getUrl())) {
        	 return true;
         } else {
        	 return false;
         }
     }
}
