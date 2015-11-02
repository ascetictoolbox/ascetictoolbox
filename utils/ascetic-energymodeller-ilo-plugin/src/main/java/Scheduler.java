import java.util.TimerTask;

import utility.CSVWriter;
import utility.DbUtility;
import monitor.EnergyMonitor;
import monitor.ResoucesMonitor;


public class Scheduler extends TimerTask{

	private DbUtility dbutility = new DbUtility();
	private EnergyMonitor em = new EnergyMonitor();
	private ResoucesMonitor rm = new ResoucesMonitor();
	private CSVWriter datacsv;
	private CSVWriter memcsv;
	private CSVWriter cpucsv;
		
	Scheduler(){
		System.out.println("Scheduler starting");
		//datacsv = new CSVWriter("/home/ascetic/Documents/Ascetic/data/", "", "power,cpu,mem");
		//memcsv = new CSVWriter("/home/ascetic/Documents/Ascetic/data/", "", "time,mem");
		//cpucsv = new CSVWriter("/home/ascetic/Documents/Ascetic/data/", "", "time,cpu");
		//datacsv.initializeFile("power");
		//memcsv.initializeFile("mem");
		//cpucsv.initializeFile("cpu");
		System.out.println("Scheduler initialzied files");
		dbutility.init();
	}
	
	@Override
	public void run() {
		System.out.println("Running");
		em.runMonitor();
		rm.monitorResources();
		dbutility.storeData(System.currentTimeMillis(), em.lastvalpower(), rm.lastvalcpu(), rm.lastvalmem());
		//datacsv.writeToFile(em.lastvalpower()+","+rm.lastvalcpu()+","+rm.lastvalmem());
		//memcsv.writeToFile(System.currentTimeMillis()+","+rm.lastvalmem());
		//cpucsv.writeToFile(System.currentTimeMillis()+","+rm.lastvalcpu());
	}

	
}

