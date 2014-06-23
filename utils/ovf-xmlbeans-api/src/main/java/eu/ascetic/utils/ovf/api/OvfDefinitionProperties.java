package eu.ascetic.utils.ovf.api;

import java.io.IOException;
import java.util.Properties;

public class OvfDefinitionProperties extends Properties {

	private static final long serialVersionUID = -8667017931205941694L;

	public static final String VM_IMAGE_FILE_HREF = "vmImageFile";

	public static final String VM_IMAGE_FILE_FORMAT = "vmImageFormat";

	public static final String VM_IMAGE_FILE_CAPACITY = "vmImageCapacity";

	public static final String VM_CONTEXTUALIZATION_FILE_HREF = "contextualizationImageFile";

	public static final String VM_CONTEXTUALIZATION_FILE_FORMAT = "contextualizationImageFormat";

	public static final String VM_CONTEXTUALIZATION_FILE_CAPACITY = "contextualizationImageCapacity";

	public static final String VM_NUMBER_OF_VIRTUAL_CPU = "numberOfVirtualCPUs";

	public static final String VM_OPERATING_SYSTEM_ID = "operatingSystemId";

	public static final String VM_OPERATING_SYSTEM_DESCRIPTION = "operatingSystemDescription";

	public static final String VM_VIRTUAL_HARDWARE_FAMILY = "virtualHardwareFamily";

	public static final String VM_MEMORY_SIZE = "memorySize";

	public static final String VM_CPU_SPEED = "cpuSpeed";

	private static final String DEFAULT_PROPERTIES_FILE = "/ovf.properties";

	/**
	 * This will create a properties object that contains all default values for
	 * an OVF definition loaded from the ovf.properties file.
	 */
	public OvfDefinitionProperties() {
		try {
			this.load(this.getClass().getResourceAsStream(
					DEFAULT_PROPERTIES_FILE));
		} catch (IOException e) {
			throw new RuntimeException("Loading default properties failed.", e);
		}
	}
}
