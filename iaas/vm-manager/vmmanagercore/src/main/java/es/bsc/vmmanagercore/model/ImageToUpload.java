package es.bsc.vmmanagercore.model;

public class ImageToUpload {
	private String name;
	private String url;
	
	public ImageToUpload(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
