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
package eu.ascetic.utils.ovf.api.enums;

import java.text.MessageFormat;

import eu.ascetic.utils.ovf.api.OperatingSystem;

/**
 * Enumeration containing predefined constants of virtual resource types used in
 * {@link OperatingSystem} from CIM_OperatingSystem.OsType<br>
 * <br>
 * 
 * (See <a
 * href="@link http://www.dmtf.org/standards/cim/cim_schema_v2191">http:/
 * /www.dmtf.org/standards/cim/cim_schema_v2191</a>)
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public enum OperatingSystemType {

	/**
	 * Enumeration containing all possible virtual machine image disk format
	 * types.
	 */
	// @formatter:off
	UNKNOWN(0), OTHER(1), MACOS(2), ATTUNIX(3), DGUX(4), DECNT(5), TRU64_UNIX(6), 
	OPENVMS(7), HPUX(8), AIX(9), MVS(10), OS400(11), OS_2(12), JAVAVM(13), MSDOS(14), 
	WIN3X(15), WIN95(16), WIN98(17), WINNT(18), WINCE(19), NCR3000(20), NETWARE(21), 
	OSF(22), DC_OS(23), RELIANTUNIX(24), SCO_UNIXWARE(25), SCO_OPENSERVER(26), 
	SEQUENT(27), IRIX(28), SOLARIS(29), SUNOS(30), U6000(31), ASERIES(32), 
	HP_NONSTOP_OS(33), HP_NONSTOP_OSS(34), BS2000(35), LINUX(36), LYNX(37), 
	XENIX(38), VM(39), INTERACTIVE_UNIX(40), BSDUNIX(41), FREEBSD(42), NETBSD(43), 
	GNU_HURD(44), OS9(45), MACH_KERNEL(46), INFERNO(47), QNX(48), EPOC(49), 
	IXWORKS(50), VXWORKS(51), MINT(52), BEOS(53), HP_MPE(54), NEXTSTEP(55), 
	PALMPILOT(56), RHAPSODY(57), WINDOWS_2000(58), DEDICATED(59), OS_390(60), VSE(61), 
	TPF(62), WINDOWS_R_ME(63), CALDERA_OPEN_UNIX(64), OPENBSD(65), NOT_APPLICABLE(66), 
	WINDOWSXP(67), ZOS(68), MICROSOFT_WINDOWS_SERVER_2003(69), 
	MICROSOFT_WINDOWS_SERVER_2003_64BIT(70), WINDOWS_XP_64BIT(71), 
	WINDOWS_XP_EMBEDDED(72), WINDOWS_VISTA(73), WINDOWS_VISTA_64BIT(74), 
	WINDOWS_EMBEDDED_FOR_POINT_OF_SERVICE(75), MICROSOFT_WINDOWS_SERVER_2008(76), 
	MICROSOFT_WINDOWS_SERVER_2008_64BIT(77);
	// @formatter:on

	/**
	 * Stores the numerical value of this OperatingSystemType.
	 */
	private final int number;

	/**
	 * Default constructor.
	 * 
	 * @param number
	 *            The numerical representation of the OperatingSystemType
	 */
	private OperatingSystemType(int number) {
		this.number = number;
	}

	/**
	 * Returns the numerical representation of this OperatingSystemType.
	 * 
	 * @return The number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Finds the OperatingSystemType object for a given numerical value.
	 * 
	 * @param number
	 *            The Operating System type number to find
	 * @return The OperatingSystemType object
	 */
	public static OperatingSystemType findByNumber(Integer number) {
		if (number != null) {
			for (OperatingSystemType os : OperatingSystemType.values()) {
				if (os.number == number) {
					return os;
				}
			}
		}
		String message = "There is no operating system with number ''{0}'' specified.";
		throw new IllegalArgumentException(
				MessageFormat.format(message, number));
	}

}
