package utility;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import data.Sample;


public class DbUtility {
	private Connection connection=null;
	private boolean initialized = false;
	private String CREATE_DB = "CREATE TABLE IF NOT EXISTS SERIES(TIMESTAMP bigint, POWER double, CPU double, MEM double)";
	private String INSERT_DATA = "INSERT INTO SERIES (TIMESTAMP,POWER,CPU,MEM) values (?,?,?,?)";
	private String SELECT_DATA = "select * from SERIES";
	
	public void init(){
		if (initialized) return;
		try {
			Class.forName("org.h2.Driver").newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println("Connecting");
			connection = DriverManager.getConnection("jdbc:h2:~/db/test", "sa",  "");
			PreparedStatement ps = connection.prepareStatement(CREATE_DB);
			ps.executeUpdate();
			initialized = true;
			System.out.println("Connected");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void storeData(long timestamp,String power, double CPU,double Mem ){
		try {
			PreparedStatement ps =	connection.prepareStatement(INSERT_DATA);
			ps.setLong(1, timestamp);
			ps.setString(2, power);
			ps.setDouble(3, CPU);
			ps.setDouble(4, Mem);
			ps.executeUpdate();
			System.out.println("iNSERTED");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Sample> getData(){
		
		List<Sample> listofresults = new Vector<Sample>();
		System.out.println("Checking");
		PreparedStatement ps;
		try {
			ps = connection.prepareStatement(SELECT_DATA);
			ResultSet rs = ps.executeQuery();
			System.out.println("Checked");
			while(rs.next()){
				Sample thissample = new Sample(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4));
				listofresults.add(thissample);
			}
			System.out.println("Finished");
			return listofresults;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
		
	}

	
}
