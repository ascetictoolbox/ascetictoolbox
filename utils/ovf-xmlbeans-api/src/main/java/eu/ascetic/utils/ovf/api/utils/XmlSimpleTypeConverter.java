/**
 *  Copyright 2014 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.utils.ovf.api.utils;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanMsgType;
import org.dmtf.schemas.wbem.wscim.x1.common.CimBoolean;
import org.dmtf.schemas.wbem.wscim.x1.common.CimString;
import org.dmtf.schemas.wbem.wscim.x1.common.CimUnsignedLong;

import java.math.BigInteger;

/**
 * Class to convert simple elements in the OVF like CimString from and to normal
 * Java Strings.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public final class XmlSimpleTypeConverter {

	/**
	 * Converts Java Strings to CimStrings.
	 * 
	 * @param string
	 *            The String to convert
	 * @return The CimStrings object representation of the Java String
	 */
	public static CimString toCimString(String string) {
		CimString cimString = CimString.Factory.newInstance();
		cimString.setStringValue(string);
		return cimString;
	}

	/**
	 * Converts Java longs to CimUnsignedLong.
	 * 
	 * @param longValue
	 *            The long to convert
	 * @return The CimUnsignedLong object representation of the Java long
	 */
	public static CimUnsignedLong toCimUnsignedLong(long longValue) {
		CimUnsignedLong cimValue = CimUnsignedLong.Factory.newInstance();
		cimValue.setBigIntegerValue(BigInteger.valueOf(longValue));
		return cimValue;
	}

	/**
	 * Converts Java Strings to XmlBeanMsgType objects.
	 * 
	 * @param string
	 *            The String to convert
	 * @return The XmlBeanMsgType object representation of the Java string
	 */
	public static XmlBeanMsgType toMsgType(String string) {
		XmlBeanMsgType msg = XmlBeanMsgType.Factory.newInstance();
		msg.setStringValue(string);
		return msg;
	}

	/**
	 * Converts Java Booleans to CimBoolean.
	 * 
	 * @param bool
	 *            The Boolean to convert
	 * @return The CimBoolean object representation of the Java Boolean
	 */
	public static CimBoolean toCimBoolean(Boolean bool) {
		CimBoolean cimVBoolean = CimBoolean.Factory.newInstance();
		cimVBoolean.setBooleanValue(bool);
		return cimVBoolean;
	}
}
