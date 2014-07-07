package eu.ascetic.asceticarchitecture.paas.component.common.dao.impl;

import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.EnergyModellerMonitoringDAO;
import eu.ascetic.asceticarchitecture.paas.component.common.mapper.EnergyModellerMonitoringMapper;
import eu.ascetic.asceticarchitecture.paas.component.common.model.EnergyModellerMonitoring;

public class EnergyModellerMonitoringDAOImpl implements EnergyModellerMonitoringDAO {

	private JdbcTemplate jdbcTemplate;
	private final static Logger LOGGER = Logger.getLogger(EnergyModellerMonitoringDAOImpl.class.getName());
	private static String SQL_CREATE="CREATE TABLE IF NOT EXISTS EMONITORING (monitoringid int NOT NULL AUTO_INCREMENT, applicationid varchar(20),deploymentid varchar(20),"
			+ "started timestamp, ended timestamp, status boolean, PRIMARY KEY (monitoringid))";
	private static String SQL_INSERT="insert into EMONITORING (applicationid,deploymentid,started, ended, status ) values (?, ?, ?, ?, ?) ";
	private static String SQL_Q_UPDATE="UPDATE EMONITORING SET status = ?,ended = ? WHERE applicationid = ? and deploymentid = ?";
	private static String SQL_Q_DEPID="select * from EMONITORING where applicationid = ? and deploymentid = ?";
	private static String SQL_Q_ALL="select * from EMONITORING where status = true";
	
	@Override
	public void initialize() {
		jdbcTemplate.execute(SQL_CREATE);
	    LOGGER.info("Created table EMONITORING");
	}
	
	@Override
	public void save(EnergyModellerMonitoring data) {
		LOGGER.info("Inserting into table EMONITORING");
		 Object[] params = new Object[] { data.getApplicationid() , data.getDeploymentid() , data.getEnded() , data.getStarted() , data.isStatus() };
		 int[] types = new int[] { Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.TIMESTAMP, Types.BOOLEAN };
		 jdbcTemplate.update(SQL_INSERT, params, types);
		 LOGGER.info("Inserted");
		
	}

	@Override
	public void setDataSource(DataSource dataSource){
		LOGGER.info("table created EMONITORING");
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<EnergyModellerMonitoring> getByDeploymentId(String applicationid, String deploymentyid) {
		LOGGER.info("retrieving moniroting..");
		return jdbcTemplate.query(SQL_Q_DEPID,new Object[]{applicationid, deploymentyid}, new EnergyModellerMonitoringMapper());
	}


	@Override
	public void terminateModel(String applicationid, String deploymentid) {
		int[] types = new int[] { Types.BOOLEAN,Types.TIMESTAMP,Types.VARCHAR,Types.VARCHAR };
		LOGGER.info("updating status");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		Timestamp ts = Timestamp.valueOf(dateFormat.format(cal.getTime()));
		jdbcTemplate.update(SQL_Q_UPDATE,new Object[]{false, ts, applicationid, deploymentid}, types);
		
	}

	@Override
	public List<EnergyModellerMonitoring> getByStatus() {
		LOGGER.info("getting active monitoring");
		return jdbcTemplate.query(SQL_Q_ALL, new EnergyModellerMonitoringMapper());
	}



}
