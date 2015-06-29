import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import utility.DbUtility;
import data.Sample;


public class DatabaseModule{

	private static DbUtility dbutil= new DbUtility();
	private static boolean init = false;
	
	
	public void testEnergyForApp() {
		if (init)return;
		dbutil.init();
		init=true;

	}
	
	
	@Test
	public void testInsert(){
		testEnergyForApp();
		dbutil.storeData(System.currentTimeMillis(), "13", 13, 1);
	}
	
	@Test
	public void testSelect(){
		testEnergyForApp();
		List<Sample> ls = dbutil.getData();
		Assert.assertEquals(1, ls.size());
		
	}
}
