package monitor;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class ResoucesMonitor{

	private static Sigar sigar = new Sigar();
	private double lastvalcpu = 0;
	private long lastvalmem = -1;
		
	public void monitorResources(){
		Mem mem = null;
        try {
            mem = sigar.getMem();
        } catch (SigarException se) {
            se.printStackTrace();
        }

        System.out.println("Actual total used system memory: " + mem.getActualUsed() / 1024 / 1024 + " MB");
        lastvalmem = mem.getActualUsed() / 1024 / 1024;
        CpuPerc cpu=null;
		try {
			cpu = sigar.getCpuPerc();
		} catch (SigarException e) {
			e.printStackTrace();
		}
		
        System.out.println("Actual CPU: " +  cpu.getUser());
        lastvalcpu = cpu.getUser();
        try {
			long[] lng = sigar.getProcList();
			System.out.println("Total processes: " +  lng.length);
		} catch (SigarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
	}
	
	public double lastvalcpu(){
		return lastvalcpu;
	}
	
	public long lastvalmem(){
		return lastvalmem;
	}

}
