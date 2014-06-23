package eu.ascetic.utils.ovf.api.utils;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanMsgType;
import org.dmtf.schemas.wbem.wscim.x1.common.CimString;
import org.dmtf.schemas.wbem.wscim.x1.common.CimUnsignedLong;

import java.math.BigInteger;

/**
 * This class is used mainly in the api to transfer simple elements in the OVF
 * like CimString from and to normal Strings
 * 
 */
public final class XmlSimpleTypeConverter {

	public static CimString toCimString(String string) {
		CimString cimString = CimString.Factory.newInstance();
		cimString.setStringValue(string);
		return cimString;
	}

	public static CimUnsignedLong toCimUnsignedLong(long longValue) {
		CimUnsignedLong cimValue = CimUnsignedLong.Factory.newInstance();
		cimValue.setBigIntegerValue(BigInteger.valueOf(longValue));
		return cimValue;
	}

	public static XmlBeanMsgType toMsgType(String string) {
		XmlBeanMsgType msg = XmlBeanMsgType.Factory.newInstance();
		msg.setStringValue(string);
		return msg;
	}
}
