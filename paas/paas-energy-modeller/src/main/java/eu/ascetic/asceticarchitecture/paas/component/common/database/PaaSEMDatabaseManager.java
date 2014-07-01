package eu.ascetic.asceticarchitecture.paas.component.common.database;

import java.sql.Timestamp;
import java.util.List;

public class PaaSEMDatabaseManager {

	//application id | deployment id | event id |  start time | end time | event load (cpu/ram usage) | energy consumed total | min | max | avg
	//application id | deployment id | start time | end time | event load (cpu/ram usage) | total energy consumed | min | max | avg
	
	public boolean setupDatabase(){
		return true;
	}
	
	public boolean createDatabase(){
		return true;
	}
	
	public boolean purgeDatabase(){
		return true;
	}
	
	public void getMeasurementQuery(String applicationid,String deployment, List<String> vms, Timestamp start, Timestamp end){
		
	}
	
}
