/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.DataeEventDAO;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.mapper.DataEventMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataEvent;

public class DataEventDAOImpl implements DataeEventDAO {

	private JdbcTemplate jdbcTemplate;
	private final static Logger LOGGER = Logger.getLogger(DataEventDAOImpl.class.getName());
	private static String SQL_CREATE="CREATE TABLE IF NOT EXISTS DATAEVENT (applicationid varchar(50),deploymentid varchar(50),"
			+ "vmid varchar(50), eventid varchar(50), data varchar(100), starttime bigint,endtime bigint, energy double)";
	private static String SQL_INSERT="insert into DATAEVENT (applicationid,deploymentid,vmid, data, starttime,endtime, energy,eventid ) values (?,?,?, ?, ?, ?, ?,?) ";
	private static String SQL_Q_APPID="select * from DATAEVENT where applicationid = ? and vmid = ? and eventid = ?";
	private static String SQL_Q_DEPID="select * from DATAEVENT where applicationid = ? and deploymentid = ? and vmid = ? and eventid = ?";
	private static String SQL_Q_APPIDTime="select * from DATAEVENT where applicationid = ? and vmid = ? and eventid = ? and starttime >= ? and starttime <= ?";
	private static String SQL_Q_DEPIDTime="select * from DATAEVENT where applicationid = ? and deploymentid = ? and vmid = ? and eventid = ? and starttime >= ? and starttime <= ?";
	private static String SQL_CLEAN="DELETE FROM DATAEVENT";
	
	@Override
	public void initialize() {
		// TODO: Add a purge policy to check if data need to be cleaned up at each restart
	    LOGGER.debug("Created table DATAEVENT");
	}
	
	@Override
	public void save(DataEvent data) {
		LOGGER.debug("Inserting into table DATAEVENT");
		 Object[] params = new Object[] { data.getApplicationid() , data.getDeploymentid() , data.getVmid(), data.getData(), data.getBegintime(), data.getEndtime() , data.getEnergy(), data.getEventid() };
		 int[] types = new int[] { Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.BIGINT,Types.BIGINT, Types.DOUBLE, Types.VARCHAR };
		 jdbcTemplate.update(SQL_INSERT, params, types);
		 LOGGER.debug("Inserted");
	}

	@Override
	public void setDataSource(DataSource dataSource) {
		LOGGER.debug("table created DATAEVENT");
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<DataEvent> getByApplicationId(String applicationid,String vmid,String eventid) {
		try{
			return jdbcTemplate.query(SQL_Q_APPID,new Object[]{applicationid,vmid,eventid}, new DataEventMapper());
		}catch (Exception e){
			return null;
		}
	}
	@Override
	public List<DataEvent> getByApplicationIdTime(String applicationid,String vmid, String eventid ,Timestamp start,Timestamp end) {
		try{
			LOGGER.info("times "+start.getTime()+" " + end.getTime());
			return jdbcTemplate.query(SQL_Q_APPIDTime,new Object[]{applicationid,vmid,eventid,start.getTime(),end.getTime()}, new DataEventMapper());
		}catch (Exception e){
			return null;
		}
	}

	public void purgedata(String appid,String vmid,String eventid){
		String delete = "DELETE FROM DATAEVENT WHERE applicationid = \""+appid+"\" and vmid=\""+vmid+"\" and eventid=\""+eventid+"\"";
		LOGGER.info("delete "+delete);
		jdbcTemplate.execute(delete);
	}
	
	// TODO workaround
	public void purgeWorkarounddata(String eventid){
		String delete = "DELETE FROM DATAEVENT WHERE eventid=\""+eventid+"\"";
		LOGGER.info("delete "+delete);
		jdbcTemplate.execute(delete);
	}

	@Override
	public List<DataEvent> getByDeployIdTime(String applicationid, String deploymentid,  String vmid,	String eventid, Timestamp start, Timestamp end) {
		try{
			LOGGER.info("times "+start.getTime()+" " + end.getTime());
			return jdbcTemplate.query(SQL_Q_DEPIDTime,new Object[]{applicationid,deploymentid,vmid,eventid,start.getTime(),end.getTime()}, new DataEventMapper());
		}catch (Exception e){
			return null;
		}
	}

	@Override
	public List<DataEvent> getByDeployId(String applicationid, String deploymentid, String vmid, String eventid) {
		try{
			return jdbcTemplate.query(SQL_Q_DEPID,new Object[]{applicationid, deploymentid,vmid,eventid}, new DataEventMapper());
		}catch (Exception e){
			return null;
		}
	}

//	private static String SQL_Q_DEPID="select * from DATAEVENT where deploymentid = ? and vmid = ? and eventid = ?";
//	private static String SQL_Q_VMID="select * from DATAEVENT where vmid = ? and eventid = ?";
//	private static String SQL_FIRST_EV="select min(starttime) from DATAEVENT where applicationid = ? and deploymentid = ? and eventid = ?";
//	private static String SQL_LAST_APP ="select max(endtime) from DATAEVENT where applicationid = ? ";
//	private static String SQL_FIRST_EV_VM="select min(starttime) from DATAEVENT where applicationid = ? and deploymentid = ? and vmid = ? and eventid = ?";
//	private static String SQL_Q_LASTVM="select max(starttime) from DATAEVENT where applicationid = ? and vmid = ? and eventid = ?";
//	private static String SQL_Q_LASTEV="select max(starttime) from DATAEVENT where applicationid = ? and eventid = ?";
//	private static String SQL_CHECK_EV="select count(*) from DATAEVENT where applicationid = ? and vmid = ?  and eventid = ? and starttime = ? and endtime = ?";
//	private static String SQL_COUNT_EV="select IFNULL(count(*),0) from DATAEVENT where applicationid = ? and deploymentid = ? and eventid = ?";
//	private static String SQL_COUNT_EV_VM="select IFNULL(count(*),0) from DATAEVENT where applicationid = ? and deploymentid = ? and vmid = ? and eventid = ?";


//	@Override
//	private Timestamp getLastEventForVM(String applicationid, String vmid, String eventid) {
//		LOGGER.info("Last event ++++++++++++++++++++++++++++");
//		Long res;
//		try{
//			if (vmid==null){
//				 res =	jdbcTemplate.queryForObject(SQL_Q_LASTEV,new Object[]{applicationid, eventid}, Long.class);
//				 LOGGER.info("Last event "+res + eventid +vmid +applicationid);
//			}else{
//				 res =	jdbcTemplate.queryForObject(SQL_Q_LASTVM,new Object[]{applicationid,vmid, eventid}, Long.class);
//				 LOGGER.info("Last event "+res + eventid +vmid +applicationid);
//			}
//			if (res==null)return null;
//			return new Timestamp(res);
//		}catch (Exception e){
//			return null;
//		}
//	}


//	@Override
//	public Timestamp getFirstEventTimeVM(String applicationid,String deploymentid, String vmid, String eventid) {
//		Long res;
//		try{
//			if (vmid==null){
//				res =	jdbcTemplate.queryForObject(SQL_FIRST_EV,new Object[]{applicationid,deploymentid, eventid}, Long.class);
//			}else {
//				res = jdbcTemplate.queryForObject(SQL_FIRST_EV_VM,new Object[]{applicationid,deploymentid, vmid, eventid}, Long.class);
//			}
//			if (res==null)return null;
//			return new Timestamp(res);
//		}catch (Exception e){
//			return null;
//		}
//	}

//	@Override
//	public Timestamp getLastEventTimeVM(String applicationid,String deploymentid, String vmid, String eventid) {
//		Timestamp ts = null;
//		if (vmid==null){
//			ts =	jdbcTemplate.queryForObject(SQL_LAST_EV,new Object[]{applicationid,deploymentid, eventid}, Timestamp.class);
//		}else {
//			ts =	jdbcTemplate.queryForObject(SQL_LAST_EV_VM,new Object[]{applicationid,deploymentid,vmid , eventid}, Timestamp.class);	
//		}
//		return ts;
//	}
	
//	@Override
//	public int getEventCountVM(String applicationid,String deploymentid, String vmid, String eventid) {
//		int res = 0;
//		try{
//			if (vmid==null){
//				res =	jdbcTemplate.queryForObject(SQL_COUNT_EV,new Object[]{applicationid,deploymentid , eventid}, Integer.class);
//			} else {
//				res =	jdbcTemplate.queryForObject(SQL_COUNT_EV_VM,new Object[]{applicationid,deploymentid,vmid , eventid}, Integer.class);
//			}
//			LOGGER.info("Count is "+res);
//			return res;
//		}catch (Exception e){
//			return 0;
//		}
//	}
	
//	public int checkIfExists(String applicationid,String vmid,String eventid, long tstart,long tend){
//		//SQL_CHECK_EV
//		int res = 0;
//		res =	jdbcTemplate.queryForObject(SQL_CHECK_EV,new Object[]{applicationid,vmid,eventid, tstart, tend}, Integer.class);
//		if (res==1){
//			LOGGER.info("Already available event "+eventid+" started "+tstart+" ended "+tend+" on vm "+vmid);
//		}
//		return res;
//		
//	}


	
	
//	@Override
//	public Timestamp getLastByApplicationId(String applicationid) {
//		Long res;
//		try{
//			res =	jdbcTemplate.queryForObject(SQL_LAST_APP,new Object[]{applicationid}, Long.class);
//			if (res==null)return null;
//			return new Timestamp(res);
//		}catch (Exception e){
//			return null;
//		}
//	}

}
