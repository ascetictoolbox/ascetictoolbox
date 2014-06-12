package net.atos.ari.seip.CloudManager.utils;

import eu.optimis.manifest.api.sp.Manifest;
import eu.optimis.types.xmlbeans.servicemanifest.XmlBeanServiceManifestDocument;

public class ManifestUtils {

	public static Manifest string2manifest(String strManifest){
		return Manifest.Factory.newInstance(strManifest);
	}
	
	public static Manifest jaxb2manifest(XmlBeanServiceManifestDocument manifestAsJaxB){
		return Manifest.Factory.newInstance(manifestAsJaxB);
	}
	
	public static String manifest2string(Manifest manifest){
		return manifest.toString();
	}
	
	public static XmlBeanServiceManifestDocument manifest2manifestJaxB(Manifest manifest){
		return manifest.toXmlBeanObject();
	}
}
