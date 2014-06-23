package eu.ascetic.utils.ovf.api;

import java.text.MessageFormat;

@SuppressWarnings("javadoc")
public enum OperatingSystemType {

	// CHECKSTYLE:OFF
	Unknown(0), Other(1), MACOS(2), ATTUNIX(3), DGUX(4), DECNT(5), Tru64_UNIX(6), OpenVMS(
			7), HPUX(8), AIX(9), MVS(10), OS400(11), OS_2(12), JavaVM(13), MSDOS(
			14), WIN3x(15), WIN95(16), WIN98(17), WINNT(18), WINCE(19), NCR3000(
			20), NetWare(21), OSF(22), DC_OS(23), ReliantUNIX(24), SCO_UnixWare(
			25), SCO_OpenServer(26), Sequent(27), IRIX(28), Solaris(29), SunOS(
			30), U6000(31), ASERIES(32), HP_NonStop_OS(33), HP_NonStop_OSS(34), BS2000(
			35), LINUX(36), Lynx(37), XENIX(38), VM(39), Interactive_UNIX(40), BSDUNIX(
			41), FreeBSD(42), NetBSD(43), GNU_Hurd(44), OS9(45), MACH_Kernel(46), Inferno(
			47), QNX(48), EPOC(49), IxWorks(50), VxWorks(51), MiNT(52), BeOS(53), HP_MPE(
			54), NextStep(55), PalmPilot(56), Rhapsody(57), Windows_2000(58), Dedicated(
			59), OS_390(60), VSE(61), TPF(62), Windows_R_Me(63), Caldera_Open_UNIX(
			64), OpenBSD(65), Not_Applicable(66), WindowsXP(67), zOS(68), Microsoft_Windows_Server_2003(
			69), Microsoft_Windows_Server_2003_64Bit(70), Windows_XP_64Bit(71), Windows_XP_Embedded(
			72), Windows_Vista(73), Windows_Vista_64Bit(74), Windows_Embedded_for_Point_of_Service(
			75), Microsoft_Windows_Server_2008(76), Microsoft_Windows_Server_2008_64Bit(
			77);
	// CHECKSTYLE:ON

	private final int number;

	OperatingSystemType(int number) {
		this.number = number;
	}

	public int number() {
		return number;
	}

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
