package eu.ascetic.utils.ovf.api;

public enum DiskFormatType {
	VMDK("http://www.vmware.com/interfaces/specifications/vmdk.html#streamOptimized"),
	VHD("http://technet.microsoft.com/en-us/library/bb676673.aspx"),
	QCOW2("http://www.gnome.org/~markmc/qcow-image-format.html");
	// TODO: Add others here?

	private String specificationUrl;

	DiskFormatType(String formatSpecificationUrl) {
		this.specificationUrl = formatSpecificationUrl;
	}

	public String getSpecificationUrl() {
		return specificationUrl;
	}

	public static DiskFormatType findBySpecificationURI(String specificationUri) {
		if (specificationUri != null) {
			for (DiskFormatType df : DiskFormatType.values()) {
				if (df.getSpecificationUrl().equals(specificationUri)) {
					return df;
				}
			}
		}
		throw new IllegalArgumentException(
				"There is no disk getSpecificationUrl with getSpecificationUrl '"
						+ specificationUri + "' specified.");
	}
}
