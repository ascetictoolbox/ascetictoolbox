package eu.ascetic.utils.ovf.api;

import java.math.BigInteger;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanFileType;

import eu.ascetic.utils.ovf.api.AbstractElement;

public class File extends AbstractElement<XmlBeanFileType> {

	public static FileFactory Factory = new FileFactory();
	
	public File(XmlBeanFileType base) {
		super(base);
	}

	public String getId() {
		return delegate.getId();
	}
	
	public void setId(String id) {
		delegate.setId(id);
	}

	public String getHref() {
		return delegate.getHref();
	}

	public void setHref(String href) {
		delegate.setHref(href);
	}

	public BigInteger getSize() {
		return delegate.getSize();
	}

	public void setSize(BigInteger size) {
		delegate.setSize(size);
	}

	public String getCompression() {
		return delegate.getCompression();
	}

	public void setCompression(String compression) {
		delegate.setCompression(compression);
	}

	public long getChunkSize() {
		return delegate.getChunkSize();
	}

	public void setChunkSize(long chunkSize) {
		delegate.setChunkSize(chunkSize);
	}

}
