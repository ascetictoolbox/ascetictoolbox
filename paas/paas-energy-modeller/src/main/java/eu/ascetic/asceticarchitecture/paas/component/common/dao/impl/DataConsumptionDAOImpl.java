package eu.ascetic.asceticarchitecture.paas.component.common.dao.impl;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.DataConsumptionDAO;
import eu.ascetic.asceticarchitecture.paas.component.common.mapper.DataConsumptionMapper;
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.common.model.IaaSVMConsumption;

public class DataConsumptionDAOImpl implements DataConsumptionDAO {

	private JdbcTemplate jdbcTemplate;
	private final static Logger LOGGER = Logger.getLogger(DataConsumptionDAOImpl.class.getName());
	private static String SQL_CREATE="CREATE TABLE IF NOT EXISTS DATACONSUMPTION (applicationid varchar(50),deploymentid varchar(50),"
			+ "vmid varchar(50), eventid varchar(50), time timestamp, cpu double, memory double, disk double, network double, hostcpu double, vmtotalcpu double, hosttotalcpu double, vmenergy double, hostenergy double)";
	private static String SQL_INSERT="insert into DATACONSUMPTION (applicationid,deploymentid,vmid, eventid, time, cpu , memory , disk , network,hostcpu, vmtotalcpu, hosttotalcpu,vmenergy,hostenergy ) values (?, ?, ?, ? ,?, ?,  ?, ?, ?,?,?,?,?,?) ";
	private static String SQL_Q_APPID="select * from DATACONSUMPTION where applicationid = ?";
	private static String SQL_Q_DEPID="select * from DATACONSUMPTION where deploymentid = ?";
	private static String SQL_Q_VMID="select * from DATACONSUMPTION where vmid = ?";
	private static String SQL_Q_ALLTIME="select UNIX_TIMESTAMP(time) from DATACONSUMPTION where applicationid = ? and vmid = ? order by time asc";
	private static String SQL_Q_ENERGYBYTIME="select vmenergy from DATACONSUMPTION where applicationid = ? and vmid = ?  order by time asc";
	private static String SQL_Q_ENERGY="select vmenergy from DATACONSUMPTION where applicationid = ? and vmid = ?  order by cpu asc";
	private static String SQL_Q_CPU="select cpu from DATACONSUMPTION where applicationid = ? and vmid = ? order by cpu asc";
	private static String SQL_Q_LASTVM="select max(time) from DATACONSUMPTION where applicationid = ? and vmid = ?";
	private static String SQL_Q_FIRSTVM="select min(time) from DATACONSUMPTION where applicationid = ? and vmid = ?";
	private static String SQL_Q_EMID="select * from DATACONSUMPTION where eventid = ?";
	private static String SQL_SUM_DEPLOY="select IFNULL(avg(vmenergy),0) from DATACONSUMPTION where applicationid = ? and deploymentid = ?";
	private static String SQL_SUM_VM="select IFNULL(avg(vmenergy),0) from DATACONSUMPTION where applicationid = ? and deploymentid = ? and vmid = ?";
	private static String SQL_CLEAN="DELETE FROM DATACONSUMPTION";
	
	@Override
	public void initialize() {
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
		 Object[] params = new Object[] { data.getApplicationid() , data.getDeploymentid() , data.getVmid(), data.getEventid(), data.getTime() , data.getCpu() , data.getMemory(), data.getDisk(), data.getNetwork() ,data.getHostcpu(),data.getVmtotalcpu(),data.getHosttotalcpu(),data.getVmenergy(),data.getHostenergy() };
		 int[] types = new int[] { Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.TIMESTAMP, Types.DOUBLE, Types.DOUBLE,Types.DOUBLE,Types.DOUBLE,Types.DOUBLE,Types.DOUBLE,Types.DOUBLE,Types.DOUBLE,Types.DOUBLE };
		 jdbcTemplate.update(SQL_INSERT, params, types);
		 LOGGER.debug("Inserted");
	}

	@Override
	public List<DataConsumption> getByApplicationId(String applicationid) {
		return jdbcTemplate.query(SQL_Q_APPID,new Object[]{applicationid}, new DataConsumptionMapper());
	}

	@Override
	public List<DataConsumption> getByDeploymentId(String deploymentyid) {
		return jdbcTemplate.query(SQL_Q_DEPID,new Object[]{deploymentyid}, new DataConsumptionMapper());
	}

	@Override
	public List<DataConsumption> getByVMId(String vmid) {
		return jdbcTemplate.query(SQL_Q_VMID,new Object[]{vmid}, new DataConsumptionMapper());
	}

	@Override
	public List<DataConsumption> getByEventId(String eventid) {
		return jdbcTemplate.query(SQL_Q_EMID,new Object[]{eventid}, new DataConsumptionMapper());
	}

	@Override
	public Timestamp getLastConsumptionForVM(String applicationid, String vmid) {
		 
		Timestamp results =	jdbcTemplate.queryForObject(SQL_Q_LASTVM,new Object[]{applicationid,vmid}, Timestamp.class);
		return results;
	}
	
	@Override
	public double getTotalEnergyForDeployment(String applicationid, String deploymentid) {
		 
		double results = jdbcTemplate.queryForObject(SQL_SUM_DEPLOY,new Object[]{applicationid,deploymentid}, Double.class);
		LOGGER.info("Total Energy is "+results);
		return results;
	}
	
	@Override
	public double getTotalEnergyForVM(String applicationid, String deploymentid, String vmid) {
		 
		double results = jdbcTemplate.queryForObject(SQL_SUM_VM,new Object[]{applicationid,deploymentid,vmid}, Double.class);
		LOGGER.info("Energy is "+results);
		return results;
	}

	@Override
	public Timestamp getFirsttConsumptionForVM(String applicationid, String vmid) {
		Timestamp results =	jdbcTemplate.queryForObject(SQL_Q_FIRSTVM,new Object[]{applicationid,vmid}, Timestamp.class);
		return results;
	}

	@Override
	public double[] getConsumptionDataVM(String applicationid, String vmid) {
		List<Object> res = jdbcTemplate.queryForList(SQL_Q_ENERGY,new Object[]{applicationid,vmid}, Object.class);
		LOGGER.info("Total vm energy samples "+res.size());
		double[] results = new double[res.size()];
		for ( int i=0;i<res.size();i++){
			results[i]=Double.parseDouble(res.get(i).toString());
		}
		return results;
	}
	@Override
	public double[] getConsumptionByTimeVM(String applicationid, String vmid) {
		List<Object> res = jdbcTemplate.queryForList(SQL_Q_ENERGYBYTIME,new Object[]{applicationid,vmid}, Object.class);
		LOGGER.info("Total vm energy samples "+res.size());
		double[] results = new double[res.size()];
		for ( int i=0;i<res.size();i++){
			results[i]=Double.parseDouble(res.get(i).toString());
		}
		return results;
	}
	@Override
	public double[] getTimeDataVM(String applicationid, String vmid) {
		List<Object> res = jdbcTemplate.queryForList(SQL_Q_ALLTIME,new Object[]{applicationid,vmid}, Object.class);
		LOGGER.info("Total vm time samples "+res.size());
		double[] results = new double[res.size()];
		for ( int i=0;i<res.size();i++){
			results[i]=Double.parseDouble(res.get(i).toString());
		}
		return results;
	}
	
	@Override
	public double[] getCpuDataVM(String applicationid, String vmid) {
		List<Object> res = jdbcTemplate.queryForList(SQL_Q_CPU,new Object[]{applicationid,vmid}, Object.class);
		LOGGER.info("Total vm time samples "+res.size());
		double[] results = new double[res.size()];
		for ( int i=0;i<res.size();i++){
			results[i]=Double.parseDouble(res.get(i).toString());
		}
		return results;
	}





}
