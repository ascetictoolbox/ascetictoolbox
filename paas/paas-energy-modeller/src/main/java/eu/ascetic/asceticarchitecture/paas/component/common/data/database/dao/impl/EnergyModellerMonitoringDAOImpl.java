/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.common.data.database.dao.impl;

import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import eu.ascetic.asceticarchitecture.paas.component.common.data.database.dao.EnergyModellerMonitoringDAO;
import eu.ascetic.asceticarchitecture.paas.component.common.data.database.mapper.EnergyModellerMonitoringMapper;
import eu.ascetic.asceticarchitecture.paas.component.common.data.database.table.EnergyModellerMonitoring;

public class EnergyModellerMonitoringDAOImpl implements EnergyModellerMonitoringDAO {

	private JdbcTemplate jdbcTemplate;
	private final static Logger LOGGER = Logger.getLogger(EnergyModellerMonitoringDAOImpl.class.getName());
	private static String SQL_CREATE="CREATE TABLE IF NOT EXISTS EMONITORING (monitoringid int NOT NULL AUTO_INCREMENT, applicationid varchar(20),deploymentid varchar(20),"
			+ "type varchar(20), started timestamp DEFAULT '2014-01-01 00:00:00', ended timestamp DEFAULT  '2014-01-01 00:00:00', status boolean, events varchar(50), PRIMARY KEY (monitoringid))";
	private static String SQL_INSERT="insert into EMONITORING (applicationid,deploymentid,type, started, status , events ) values (?, ?, ?, ?, ?, ?) ";
	private static String SQL_Q_UPDATE_TIME="UPDATE EMONITORING SET status = ?, ended = ? WHERE applicationid = ? and deploymentid = ? and type = ?";
	private static String SQL_Q_DEPID="select * from EMONITORING where applicationid = ? and deploymentid = ?";
	private static String SQL_Q_DEPID_monitoring="select * from EMONITORING where status=true and type='MONITORING'";
	private static String SQL_Q_DEPID_training="select * from EMONITORING where status=true and type='TRAINING'";
	private static String SQL_CLEAN="DELETE FROM EMONITORING";
	
	@Override
	public void initialize() {
		//jdbcTemplate.execute(SQL_CREATE);
		//jdbcTemplate.execute(SQL_CLEAN);
	    LOGGER.debug("Created table EMONITORING");
	}

	@Override
	public void setDataSource(DataSource dataSource){
		LOGGER.debug("datasource for EMONITORING ready");
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public void createTraining(String applicationid, String deploymentid, String events) {
		LOGGER.debug("Inserting into table EMONITORING");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		Timestamp ts = Timestamp.valueOf(dateFormat.format(cal.getTime()));
		Object[] params = new Object[] { applicationid , deploymentid , "TRAINING" , ts , Boolean.TRUE , events };
		int[] types = new int[] { Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.TIMESTAMP, Types.BOOLEAN, Types.VARCHAR };
		jdbcTemplate.update(SQL_INSERT, params, types);
		LOGGER.debug("Inserted new training");	
	}
	
	@Override
	public void createMonitoring(String applicationid, String deploymentid, String events) {
		LOGGER.debug("Inserting into table EMONITORING");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		Timestamp ts = Timestamp.valueOf(dateFormat.format(cal.getTime()));
		Object[] params = new Object[] { applicationid , deploymentid , "MONITORING" , ts , Boolean.TRUE , events };
		int[] types = new int[] { Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.TIMESTAMP, Types.BOOLEAN ,Types.VARCHAR};
		jdbcTemplate.update(SQL_INSERT, params, types);
		LOGGER.debug("Inserted new monitoring");	
		
	}
	
	@Override
	public void terminateMonitoring(String applicationid, String deploymentid) {
		int[] types = new int[] { Types.BOOLEAN,Types.TIMESTAMP,Types.VARCHAR,Types.VARCHAR, Types.VARCHAR };
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		Timestamp ts = Timestamp.valueOf(dateFormat.format(cal.getTime()));
		jdbcTemplate.update(SQL_Q_UPDATE_TIME,new Object[]{false, ts, applicationid, deploymentid,"MONITORING"}, types);
		LOGGER.info("terminated monitoring");
		
	}
	
	@Override
	public void terminateTraining(String applicationid, String deploymentid) {
		int[] types = new int[] { Types.BOOLEAN,Types.TIMESTAMP,Types.VARCHAR,Types.VARCHAR, Types.VARCHAR };
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		Timestamp ts = Timestamp.valueOf(dateFormat.format(cal.getTime()));
		jdbcTemplate.update(SQL_Q_UPDATE_TIME,new Object[]{false, ts, applicationid, deploymentid,"TRAINING"}, types);
		LOGGER.info("terminated training");
		
	}

	@Override
	public List<EnergyModellerMonitoring> getByDeploymentId(String applicationid, String deploymentyid) {
		LOGGER.info("retrieving monitoring..");
		return jdbcTemplate.query(SQL_Q_DEPID,new Object[]{applicationid, deploymentyid}, new EnergyModellerMonitoringMapper());
	}

	@Override
	public List<EnergyModellerMonitoring> getTrainingActive() {
		LOGGER.info("getting list of active training");
		return jdbcTemplate.query(SQL_Q_DEPID_training,new Object[]{},new EnergyModellerMonitoringMapper());
	}

	@Override
	public List<EnergyModellerMonitoring> getMonitoringActive() {
		LOGGER.debug("getting list of active training");
		List<EnergyModellerMonitoring> active = jdbcTemplate.query(SQL_Q_DEPID_monitoring,new EnergyModellerMonitoringMapper());
		LOGGER.debug("got "+active.size());
		return active;
	}

}
