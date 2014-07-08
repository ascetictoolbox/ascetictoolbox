package eu.ascetic.utils.ovf.api;

import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanFileType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanReferencesType;

import eu.ascetic.utils.ovf.api.AbstractElement;

public class References extends AbstractElement<XmlBeanReferencesType> {

	public static ReferencesFactory Factory = new ReferencesFactory();
	
	protected References(XmlBeanReferencesType base) {
		super(base);
	}

	public File[] getFileArray() {
		Vector<File> vector = new Vector<File>();
		for (XmlBeanFileType type : delegate.getFileArray()) {
			vector.add(new File(type));
		}
		return vector.toArray(new File[vector.size()]);
	}
	
	public void setFileArray(File[] fileArray) {
		Vector<XmlBeanFileType> vector = new Vector<XmlBeanFileType>();
		
		for (int i = 0; i < fileArray.length; i++) {
			vector.add(fileArray[i].getXmlObject());
		}
		
		delegate.setFileArray((XmlBeanFileType[]) vector.toArray());
	}

	public File getFileAtIndex(int i) {
		return new File(delegate.getFileArray(i));
	}
	
	public void addFile(File file) {
		XmlBeanFileType newfile = delegate.addNewFile();
		newfile.set(file.getXmlObject());
	}
}
