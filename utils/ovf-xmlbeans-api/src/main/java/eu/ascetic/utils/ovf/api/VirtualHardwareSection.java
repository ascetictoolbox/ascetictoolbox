package eu.ascetic.utils.ovf.api;

import java.util.List;
import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanRASDType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualHardwareSectionType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

public class VirtualHardwareSection extends
		AbstractElement<XmlBeanVirtualHardwareSectionType> {
	
	public static VirtualHardwareSectionFactory Factory = new VirtualHardwareSectionFactory();
	
	public VirtualHardwareSection(XmlBeanVirtualHardwareSectionType base) {
		super(base);
	}

	public String getInfo() {
		return delegate.getInfo().getStringValue();
	}
	
	public void setInfo(String info) {
		delegate.getInfo().setStringValue(info);
	}

	public System getSystem() {
		return new System(delegate.getSystem());
	}
	
	public void getSystem(System system) {
		delegate.setSystem(system.getXmlObject());
	}

	public Item[] getItemArray() {
		List<Item> vector = new Vector<Item>();
		for (XmlBeanRASDType type : delegate.getItemArray()) {
			vector.add(new Item(type));
		}
		return vector.toArray(new Item[vector.size()]);
	}
	
	public void setItemArray(Item[] itemArray) {
		List<XmlBeanRASDType> newItemArray = new Vector<XmlBeanRASDType>();
		for (int i = 0; i < itemArray.length; i++) {
			newItemArray.add(itemArray[i].getXmlObject());
		}
		delegate.setItemArray((XmlBeanRASDType[]) newItemArray.toArray());
	}

	public Item getItemAtIndex(int i) {
		return new Item(delegate.getItemArray(i));
	}
	
	public void addItem(Item item) {
		XmlBeanRASDType xmlBeanRASDType = delegate.addNewItem();
		xmlBeanRASDType.set(item.getXmlObject());
	}

	public String getVirtualHardwareFamily() {
		return delegate.getSystem().getVirtualSystemType().getStringValue();
	}

	public void setVirtualHardwareFamily(String virtualHardwareFamily) {
		delegate.getSystem().setVirtualSystemType(
				XmlSimpleTypeConverter.toCimString(virtualHardwareFamily));
	}

	public int getNumberOfVirtualCPUs() {
		return delegate.getItemArray(0).getVirtualQuantity()
				.getBigIntegerValue().intValue();
	}

	public void setNumberOfVirtualCPUs(int numberOfVirtualCPUs) {
		delegate.getItemArray(0).setVirtualQuantity(
				XmlSimpleTypeConverter.toCimUnsignedLong(numberOfVirtualCPUs));
	}

	public int getMemorySize() {
		return delegate.getItemArray(1).getVirtualQuantity()
				.getBigIntegerValue().intValue();
	}

	public void setMemorySize(int memorySize) {
		if (memorySize < 0) {
			throw new IllegalArgumentException("Memory size must be > -1");
		}

		delegate.getItemArray(1).setVirtualQuantity(
				XmlSimpleTypeConverter.toCimUnsignedLong(memorySize));
	}

	public void setCPUSpeed(int cpuSpeed) {
		if (!(cpuSpeed > -1)) {
			throw new IllegalArgumentException("CPU speed must be > -1");
		}
		delegate.getItemArray(2).setReservation(
				XmlSimpleTypeConverter.toCimUnsignedLong(cpuSpeed));
	}

	public int getCPUSpeed() {
		return delegate.getItemArray(2).getReservation().getBigIntegerValue()
				.intValue();
	}

}
