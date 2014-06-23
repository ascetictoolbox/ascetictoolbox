package eu.ascetic.utils.ovf.api;

import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanFileType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanReferencesType;

import eu.ascetic.utils.ovf.api.AbstractElement;

public class References extends AbstractElement<XmlBeanReferencesType> {

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

	public File getFileAtIndex(int i) {
		return new File(delegate.getFileArray(i));
	}

	// FIXME: We should support multiple images of any type

	public File getImageFile() {
		return new File(delegate.getFileArray(0));
	}

	public File getContextualizationFile() {
		return new File(delegate.getFileArray(1));
	}

}
