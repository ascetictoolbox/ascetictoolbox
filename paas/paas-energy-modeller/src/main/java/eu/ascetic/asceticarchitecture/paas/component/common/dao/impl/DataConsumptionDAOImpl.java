/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.common.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.DataConsumptionDAO;
import eu.ascetic.asceticarchitecture.paas.component.common.mapper.DataConsumptionMapper;
import eu.ascetic.asceticarchitecture.paas.component.common.mapper.EnergySampleMapper;
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EnergySample;

public class DataConsumptionDAOImpl implements DataConsumptionDAO {

	private JdbcTemplate jdbcTemplate;
	private final static Logger LOGGER = Logger.getLogger(DataConsumptionDAOImpl.class.getName());
	private static String SQL_CREATE="CREATE TABLE IF NOT EXISTS DATACONSUMPTION (applicationid varchar(50),deploymentid varchar(50),"
			+ "vmid varchar(50), eventid varchar(50), time bigint, vmenergy double, vmpower double,vmcpu double)";
	private static String SQL_DROP="DROP TABLE IF EXISTS DATACONSUMPTION";
	private static String SQL_INSERT="insert into DATACONSUMPTION (applicationid,deploymentid,vmid, eventid, time, vmenergy,vmpower , vmcpu ) values (?, ?, ?, ? ,?, ?,  ?, ?) ";
	private static String SQL_Q_APPID="select * from DATACONSUMPTION where applicationid = ?";
	private static String SQL_Q_DEPID="select * from DATACONSUMPTION where deploymentid = ?";
	private static String SQL_Q_VMID="select * from DATACONSUMPTION where vmid = ?";
	private static String SQL_Q_ALLTIME="select time from DATACONSUMPTION where applicationid = ? and vmid like ? order by time asc";
	private static String SQL_Q_ENERGYBYTIME="select vmenergy from DATACONSUMPTION where applicationid = ? and vmid like ?  order by time asc";
	private static String SQL_Q_ENERGY="select vmenergy from DATACONSUMPTION where applicationid = ? and vmid = ?  order by vmcpu asc";
	private static String SQL_Q_CPU="select cpu from DATACONSUMPTION where applicationid = ? and vmid = ? order by vmcpu asc";
	private static String SQL_Q_LASTVM="select max(time) from DATACONSUMPTION where applicationid = ? and vmid like ?";
	private static String SQL_Q_FIRSTVM="select min(time) from DATACONSUMPTION where applicationid = ? and vmid like ?";
	private static String SQL_Q_EMID="select * from DATACONSUMPTION where eventid = ?";
	private static String SQL_SUM_DEPLOY="select IFNULL(sum(vmenergy),0) from DATACONSUMPTION where applicationid = ? and deploymentid = ?";
	private static String SQL_SUM_VM="select IFNULL(sum(vmenergy),0) from DATACONSUMPTION where applicationid = ? and  vmid like ?";
	private static String SQL_AVG_VM="select IFNULL(sum(vmenergy),0) from DATACONSUMPTION where applicationid = ? and  vmid like ?";
	private static String SQL_COUNT_VM="select count(*) from DATACONSUMPTION where applicationid = ? and  vmid like ?";
	private static String SQL_E_SUM_VMTIME="select IFNULL(sum(vmenergy),0) from DATACONSUMPTION where applicationid = ? and vmid like ? and time >= ? and time <= ?";
	private static String SQL_AVG_VMPOWER="select IFNULL(avg(vmpower),0) from DATACONSUMPTION where applicationid = ? and vmid like ? and time >= ? and time <= ?";
	private static String SQL_MEASURES_VMTIME="select vmid,time,vmenergy, vmpower from DATACONSUMPTION where applicationid = ? and vmid like ? and time >= ? and time <= ?";
	private static String SQL_CLEAN="DELETE FROM DATACONSUMPTION";
	
	
	@Override
	public void initialize() {
		jdbcTemplate.execute(SQL_DROP);
		jdbcTemplate.execute(SQL_CREATE);
		//jdbcTemplate.execute(SQL_CLEAN);
	    LOGGER.debug("Created table DATACONSUMPTION");
	}

	@Override
	public void setDataSource(DataSource dataSource) {
		 this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void save(DataConsumption data) {
		 LOGGER.debug("Inserting into table DATACONSUMPTION");
		 Object[] params = new Object[] { data.getApplicationid() , data.getDeploymentid() , data.getVmid(), data.getEventid(), data.getTime() ,  data.getVmenergy(),data.getVmpower() ,data.getCpu() };
		 int[] types = new int[] { Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.BIGINT, Types.DOUBLE, Types.DOUBLE,Types.DOUBLE};
		 jdbcTemplate.update(SQL_INSERT, params, types);
		 LOGGER.debug("Inserted");
	}

	@Override
	public List<DataConsumption> getByApplicationId(String applicationid) {
		List<DataConsumption> result = null;
		try{
			result = jdbcTemplate.query(SQL_Q_APPID,new Object[]{applicationid}, new DataConsumptionMapper());
			return result;
		}catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<DataConsumption> getByDeploymentId(String deploymentyid) {
		List<DataConsumption> result = null;
		try{
			result = jdbcTemplate.query(SQL_Q_DEPID,new Object[]{deploymentyid}, new DataConsumptionMapper());
			return result;
		}catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<DataConsumption> getByVMId(String vmid) {
		List<DataConsumption> result = null;
		try{
			result = jdbcTemplate.query(SQL_Q_VMID,new Object[]{vmid}, new DataConsumptionMapper());
			return result;
		}catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<DataConsumption> getByEventId(String eventid) {
		List<DataConsumption> result = null;
		try{
			result = jdbcTemplate.query(SQL_Q_EMID,new Object[]{eventid}, new DataConsumptionMapper());
			return result;
		}catch (Exception e) {
			return null;
		}
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
	public double getTotalEnergyForDeployment(String applicationid, String deploymentid) {
		try{
			Double results = jdbcTemplate.queryForObject(SQL_SUM_DEPLOY,new Object[]{applicationid,deploymentid}, Double.class);
			if (results==null)return 0;
			LOGGER.info("Total Energy is "+results);
			return results;
		}catch (Exception e) {
			return 0;
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

	@Override
	public Timestamp getFirsttConsumptionForVM(String applicationid, String vmid) {
		try{
			Long results =	jdbcTemplate.queryForObject(SQL_Q_FIRSTVM,new Object[]{applicationid,vmid+"%"}, Long.class);
			LOGGER.info("Got "+results);
			if (results==null)return null;
			return new Timestamp(results);
		}catch (Exception e) {
			return null;
		}
	}

	@Override
	public double[] getConsumptionDataVM(String applicationid, String vmid) {
		try{
			List<Object> res = jdbcTemplate.queryForList(SQL_Q_ENERGY,new Object[]{applicationid,vmid}, Object.class);
			LOGGER.info("Total vm energy samples "+res.size());
			double[] results = new double[res.size()];
			for ( int i=0;i<res.size();i++){
				results[i]=Double.parseDouble(res.get(i).toString());
			}
			return results;
		}catch (Exception e) {
			return null;
		}
	}
	@Override
	public double[] getConsumptionByTimeVM(String applicationid, String vmid) {
		try{
			List<Object> res = jdbcTemplate.queryForList(SQL_Q_ENERGYBYTIME,new Object[]{applicationid,vmid+"%"}, Object.class);
			if (res==null) return null;
			LOGGER.info("app  "+applicationid + " vm "+ vmid);
			LOGGER.info("Total vm energy samples "+res.size());
			
			double[] results = new double[res.size()];
			for ( int i=0;i<res.size();i++){
				results[i]=Double.parseDouble(res.get(i).toString());
			}
			return results;
		}catch (Exception e) {
			return null;
		}
	}
	@Override
	public double[] getTimeDataVM(String applicationid, String vmid) {
		try{
			List<Object> res = jdbcTemplate.queryForList(SQL_Q_ALLTIME,new Object[]{applicationid,vmid+"%"}, Object.class);
			if (res==null) return null;
			LOGGER.info("app  "+applicationid + " vm "+ vmid);
			LOGGER.info("Total vm time samples "+res.size());
			double[] results = new double[res.size()];
			for ( int i=0;i<res.size();i++){
				results[i]=Double.parseDouble(res.get(i).toString());
			}
			return results;
		}catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public double[] getCpuDataVM(String applicationid, String vmid) {
		try{
			LOGGER.info("app  "+applicationid + " vm "+ vmid);
			List<Object> res = jdbcTemplate.queryForList(SQL_Q_CPU,new Object[]{applicationid,vmid}, Object.class);
			if (res==null) return null;
			LOGGER.info("Total vm time samples "+res.size());
			double[] results = new double[res.size()];
			for ( int i=0;i<res.size();i++){
				results[i]=Double.parseDouble(res.get(i).toString());
			}
			return results;
		}catch (Exception e) {
			return null;
		}
	}


	public List<EnergySample> getDataSamplesVM(String applicationid, String deployment,String vmid,long start,long end) {
		try{
			List<EnergySample> res = jdbcTemplate.query(SQL_MEASURES_VMTIME,new Object[]{applicationid,vmid+"%",start,end}, new EnergySampleMapper());
			if (res==null) return null;
			LOGGER.info("Total vm time samples "+res.size());
			return res;
		}catch (Exception e) {
			return null;
		}
	}

	@Override
	public void insertBatch(List<DataConsumption> samples){
		// need to use this update method to increase performance
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
			    ps.setDouble(8, sample.getCpu());
			    ps.addBatch();
	
			}
			ps.executeBatch();
			ps.clearBatch(); 
			connection.commit();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
