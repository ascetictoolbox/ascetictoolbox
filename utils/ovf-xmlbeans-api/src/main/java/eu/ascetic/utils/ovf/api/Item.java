package eu.ascetic.utils.ovf.api;

import java.math.BigInteger;
import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanRASDType;
import org.dmtf.schemas.wbem.wscim.x1.cimSchema.x2.cimResourceAllocationSettingData.XmlBeanResourceTypeDocument.ResourceType;
import org.dmtf.schemas.wbem.wscim.x1.common.CimString;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

public class Item extends AbstractElement<XmlBeanRASDType> {
	
	public static ItemFactory Factory = new ItemFactory();
	
	public Item(XmlBeanRASDType base) {
		super(base);
	}

	public String getDescription() {
		if (delegate.isSetDescription()) {
			return delegate.getDescription().getStringValue();
		}
		return null;
	}
	
	public void setDescription(String description) {
		
		if(delegate.isSetDescription()) {
			delegate.unsetResourceType();
		}
		
		delegate.setDescription(XmlSimpleTypeConverter.toCimString(description));
	}

	public String getElementName() {
		return delegate.getElementName().getStringValue();
	}
	
	public void setElementName(String elementName) {
		delegate.setElementName(XmlSimpleTypeConverter.toCimString(elementName));
	}

	public String getInstanceID() {
		return delegate.getInstanceID().getStringValue();
	}
	
	public void setInstanceId(String instanceId) {
		delegate.setInstanceID(XmlSimpleTypeConverter.toCimString(instanceId));
	}

	public int getResourceType() {
		return delegate.getResourceType().getIntValue();
	}
	
	public void setResourceType(int resourceType) {
		
		if(delegate.isSetResourceType()) {
			delegate.unsetResourceType();
		}
		
		ResourceType newResourceType = delegate.addNewResourceType();
		newResourceType.setIntValue(resourceType);
		delegate.setResourceType(newResourceType);
	}

	public BigInteger getVirtualQuantity() {
		if (delegate.isSetVirtualQuantity()) {
			return delegate.getVirtualQuantity().getBigIntegerValue();
		}
		return null;
	}
	
	public void setVirtualQuantity(BigInteger virtualQuantity) {
		if (delegate.isSetVirtualQuantity()) {
			delegate.unsetVirtualQuantity();
		}
		delegate.setVirtualQuantity(XmlSimpleTypeConverter.toCimUnsignedLong(virtualQuantity.longValue()));
	}

	public String getAllocationUnits() {
		if (delegate.isSetAllocationUnits()) {
			return delegate.getAllocationUnits().getStringValue();
		}
		return null;
	}
	
	public void setAllocationUnits(String allocationUnits) {
		if (delegate.isSetAllocationUnits()) {
			delegate.unsetAllocationUnits();
		}
		delegate.setAllocationUnits(XmlSimpleTypeConverter.toCimString(allocationUnits));
	}

	public Boolean getAutomaticAllocation() {
		return delegate.getAutomaticAllocation().getBooleanValue();
	}
	
	public void setAutomaticAllocation(Boolean automaticAllocation) {
		delegate.setAutomaticAllocation(XmlSimpleTypeConverter.toCimBoolean(automaticAllocation));
	}

	public String[] getConnectionArray() {
		Vector<String> vector = new Vector<String>();
		for (CimString type : delegate.getConnectionArray()) {
			vector.add(type.getStringValue());
		}
		return vector.toArray(new String[vector.size()]);
	}
	
	public void setConnectionArray(String[] connectionArray) {
		Vector<CimString> newConnectionArray = new Vector<CimString>();
		for (int i = 0; i < connectionArray.length; i++) {
			CimString cimString = CimString.Factory.newInstance();
			cimString.setStringValue(connectionArray[i]);
			newConnectionArray.add(cimString);
		}
		delegate.setConnectionArray((CimString[]) newConnectionArray.toArray());
	}

	public String getConnectionAtIndex(int i) {
		return delegate.getConnectionArray(i).getStringValue();
	}
	
	public void addConnection(String connection) {
		CimString cimString = delegate.addNewConnection();
		cimString.setStringValue(connection);
	}

	public String getParent() {
		if (delegate.isSetParent()) {
			return delegate.getParent().getStringValue();
		}
		return null;
	}
	
	public void setParent(String parent) {
		if (delegate.isSetParent()) {
			delegate.unsetParent();
		}
		delegate.setParent(XmlSimpleTypeConverter.toCimString(parent));
	}

	public String[] getHostResourceArray() {
		Vector<String> vector = new Vector<String>();
		for (CimString cimString : delegate.getHostResourceArray()) {
			vector.add(cimString.getStringValue());
		}
		return vector.toArray(new String[vector.size()]);
	}
	
	public void setHostResourceArray(String[] hostResourceArray) {
		Vector<CimString> newHostResourceArray = new Vector<CimString>();
		for (int i = 0; i < hostResourceArray.length; i++) {
			CimString cimString = CimString.Factory.newInstance();
			cimString.setStringValue(hostResourceArray[i]);
			newHostResourceArray.add(cimString);
		}
		delegate.setHostResourceArray((CimString[]) newHostResourceArray.toArray());
	}

	public String getHostResourceArray(int i) {
		if (delegate.getHostResourceArray().length > i) {
			return delegate.getHostResourceArray(i).getStringValue();
		}
		return null;
	}
	
	public void addHostResource(String hostResource) {
		CimString cimString = delegate.addNewHostResource();
		cimString.setStringValue(hostResource);
	}
}
