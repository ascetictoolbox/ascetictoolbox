/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.ApplicationSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.DataConsumptionDAO;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.mapper.ApplicationSampleMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;

public class DataConsumptionDAOImpl implements DataConsumptionDAO {

	private JdbcTemplate jdbcTemplate;
	private final static Logger LOGGER = Logger.getLogger(DataConsumptionDAOImpl.class.getName());
	
	private static String SQL_CREATE="CREATE TABLE IF NOT EXISTS DATACONSUMPTION (applicationid varchar(50),deploymentid varchar(50),"
			+ "vmid varchar(50), eventid varchar(50), time bigint, vmenergy double, vmpower double,vmcpu double)";
	private static String SQL_CLEAN="DELETE FROM DATACONSUMPTION";
	private static String SQL_INSERT="insert into DATACONSUMPTION (applicationid,deploymentid,vmid, eventid, time, vmenergy,vmpower , vmcpu ) values (?, ?, ?, ? ,?, ?,  ?, ?) ";
	private static String SQL_Q_LASTVM="select max(time) from DATACONSUMPTION where applicationid = ? and vmid like ?";
	private static String SQL_AVG_VM="select IFNULL(sum(vmenergy),0) from DATACONSUMPTION where applicationid = ? and  vmid like ?";
	private static String SQL_E_SUM_VMTIME="select IFNULL(sum(vmenergy),0) from DATACONSUMPTION where applicationid = ? and vmid like ? and time >= ? and time <= ?";
	private static String SQL_AVG_VMPOWER="select IFNULL(avg(vmpower),0) from DATACONSUMPTION where applicationid = ? and vmid like ? and time >= ? and time <= ?";
	private static String SQL_MEASURES_VMTIME="select vmid,time,vmenergy, vmpower from DATACONSUMPTION where applicationid = ? and vmid like ? and time >= ? and time <= ?";
	private static String SQL_Q_COUNTSAMPLES="select count(*) from DATACONSUMPTION where applicationid = ? and vmid like ? and time >= ? and time <= ?";
	private static String SQL_SAMPLE_BEFORE_TIME="select max(time) from DATACONSUMPTION where applicationid = ? and vmid like ? and time <= ?";
	private static String SQL_SAMPLE_AFTER_TIME="select min(time) from DATACONSUMPTION where applicationid = ? and vmid like ? and time >= ? ";
	private static String SQL_SAMPLE_AT_TIME="select vmid,time,vmenergy, vmpower from DATACONSUMPTION where applicationid = ? and vmid like ? and time = ? ";
	
	
	@Override
	public void initialize() {
		// TODO: Add a purge policy to check if data need to be cleaned up at each restart
	    LOGGER.debug("Created table DATACONSUMPTION");
	}

	@Override
	public void setDataSource(DataSource dataSource) {
		 this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void save(DataConsumption data) {
		 LOGGER.debug("Inserting into table DATACONSUMPTION");
		 Object[] params = new Object[] { data.getApplicationid() , data.getDeploymentid() , data.getVmid(), data.getEventid(), data.getTime() ,  data.getVmenergy(),data.getVmpower() ,data.getVmcpu() };
		 int[] types = new int[] { Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.BIGINT, Types.DOUBLE, Types.DOUBLE,Types.DOUBLE};
		 jdbcTemplate.update(SQL_INSERT, params, types);
		 LOGGER.debug("Inserted");
	}

	@Override
	public Timestamp getLastConsumptionForVM(String applicationid, String vmid) {
		LOGGER.info("Getting last measurement ");
		try{
			Long results =	jdbcTemplate.queryForObject(SQL_Q_LASTVM,new Object[]{applicationid,vmid+"%"}, Long.class);
			LOGGER.info("Got "+results);
			if (results==null)return null;
			return new Timestamp(results);
		}catch (Exception e) {
			return null;
		}
	}
	@Override
	public double getTotalEnergyForVM(String applicationid, String deploymentid, String vmid) {
		try{
			LOGGER.info("performing the query "+SQL_AVG_VM);
			LOGGER.info("with "+vmid);
			Double results = jdbcTemplate.queryForObject(SQL_AVG_VM,new Object[]{applicationid,vmid+"%"}, Double.class);
			if (results==null)return 0;
			LOGGER.info("Total Energy is "+results);
			return results;
		}catch (Exception e) {
			return 0;
		}
	}
	
	@Override
	public double getPowerInIntervalForVM(String applicationid, String vmid,Timestamp start,Timestamp end) {
		try{
			Double  results = jdbcTemplate.queryForObject(SQL_AVG_VMPOWER,new Object[]{applicationid,vmid+"%",start.getTime(),end.getTime()}, Double.class);
			LOGGER.debug("Power is "+results);
			if (results==null)return 0;
			LOGGER.info("Total Energy is "+results);
			return results;
		}catch (Exception e) {
			return 0;
		}
	}
	
	@Override
	public double getTotalEnergyForVMTime(String applicationid, String vmid,Timestamp start,Timestamp end) {
		try{
			Double results = jdbcTemplate.queryForObject(SQL_E_SUM_VMTIME,new Object[]{applicationid,vmid+"%",start.getTime(),end.getTime()}, Double.class);
			LOGGER.debug("Power is "+results);
			if (results==null)return 0;
			LOGGER.info("Total Energy is "+results);
			return results;
		}catch (Exception e) {
			return 0;
		}
	}



	/**
	 * 
	 * These methods are used to retrieve data exposed externally by EM and in the packaage datatype
	 */
	
	@Override
	public List<ApplicationSample> getDataSamplesVM(String applicationid, String deployment,String vmid,long start,long end) {
		try{
			List<ApplicationSample> res = jdbcTemplate.query(SQL_MEASURES_VMTIME,new Object[]{applicationid,vmid+"%",start,end}, new ApplicationSampleMapper());
			if (res==null) return null;
			LOGGER.info("Total vm time samples "+res.size());
			return res;
		}catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public int getSamplesBetweenTime(String applicationid, String vmid,	long start, long end) {
		Integer results = jdbcTemplate.queryForObject(SQL_Q_COUNTSAMPLES,new Object[]{applicationid,vmid+"%",start,end}, Integer.class);
		return results;
	}
	@Override
	public ApplicationSample getSampleAtTime(String applicationid, String vmid,long time) {
		try{
			ApplicationSample res = jdbcTemplate.queryForObject(SQL_SAMPLE_AT_TIME,new Object[]{applicationid,vmid+"%",time}, new ApplicationSampleMapper());
			if (res==null) return null;
			LOGGER.info("Sample found ");
			return res;
		}catch (Exception e) {
			return null;
		}
	}
	@Override
	public long getSampleTimeBefore(String applicationid, String vmid, long time) {
		try{
			Long results =	jdbcTemplate.queryForObject(SQL_SAMPLE_BEFORE_TIME,new Object[]{applicationid,vmid+"%",time}, Long.class);
			LOGGER.info("Got "+results);
			if (results==null)return 0;
			return results;
		}catch (Exception e) {
			return 0;
		}
	}

	@Override
	public long getSampleTimeAfter(String applicationid, String vmid, long time) {
		try{
			Long results =	jdbcTemplate.queryForObject(SQL_SAMPLE_AFTER_TIME,new Object[]{applicationid,vmid+"%",time}, Long.class);
			LOGGER.info("Got "+results);
			if (results==null)return 0;
			return results;
		}catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * 
	 * This method is used by the DataCollector to store data into db after retrieving it from the Zabbix interface,
	 * for efficiency the insert operation is applied as a whole and not for each row
	 * 
	 */
	@Override
	public void insertBatch(List<DataConsumption> samples){
		if (samples==null)return;
		Connection connection;
		try {
			connection = jdbcTemplate.getDataSource().getConnection();
			connection.setAutoCommit(false);
			PreparedStatement ps = connection.prepareStatement(SQL_INSERT);
			for (DataConsumption sample: samples) {
			    ps.setString(1, sample.getApplicationid());
			    ps.setString(2, sample.getDeploymentid());
			    ps.setString(3, sample.getVmid());
			    ps.setString(4, sample.getEventid());
			    ps.setLong(5, sample.getTime());
			    ps.setDouble(6, sample.getVmenergy());
			    ps.setDouble(7, sample.getVmpower());
			    ps.setDouble(8, sample.getVmcpu());
			    ps.addBatch();
			}
			ps.executeBatch();
			ps.clearBatch(); 
			connection.commit();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
