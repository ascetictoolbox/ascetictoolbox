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

import eu.ascetic.asceticarchitecture.paas.component.common.dao.EnergyModellerTrainingDAO;
import eu.ascetic.asceticarchitecture.paas.component.common.mapper.EnergyModellerTrainingMapper;
import eu.ascetic.asceticarchitecture.paas.component.common.model.EnergyModellerTraining;

public class EnergyModellerTrainingDAOImpl implements EnergyModellerTrainingDAO {
	
	private JdbcTemplate jdbcTemplate;
	private final static Logger LOGGER = Logger.getLogger(EnergyModellerTrainingDAOImpl.class.getName());
	private static String SQL_CREATE="CREATE TABLE IF NOT EXISTS ETRAINING (trainingid int not null auto_increment, applicationid varchar(20),deploymentid varchar(20),"
			+ "started timestamp, ended timestamp, status boolean, events varchar(100),PRIMARY KEY (trainingid))";
	private static String SQL_INSERT="insert into ETRAINING (applicationid,deploymentid,started, ended, status , events ) values (?, ?, ?, ?, ?, ?) ";
	private static String SQL_Q_UPDATE="UPDATE ETRAINING SET status = ?, ended = ?  WHERE applicationid = ? and deploymentid = ?";
	private static String SQL_Q_DEPID="select * from ETRAINING where applicationid = ? and deploymentid = ?";
	private static String SQL_Q_ALL="select * from ETRAINING where status = true";
	
	@Override
	public void initialize() {
		jdbcTemplate.execute(SQL_CREATE);
	    LOGGER.info("Created table ETRAINING");
	}
	
	@Override
	public void save(EnergyModellerTraining data) {
		LOGGER.info("Inserting into table ETRAINING");
		 Object[] params = new Object[] { data.getApplicationid() , data.getDeploymentid() , data.getEnded() , data.getStarted(), data.getStatus() , data.getEvents() };
		 int[] types = new int[] { Types.VARCHAR,Types.VARCHAR,Types.TIMESTAMP,Types.TIMESTAMP, Types.BOOLEAN, Types.VARCHAR };
		 jdbcTemplate.update(SQL_INSERT, params, types);
		 LOGGER.info("Inserted");
		
	}

	@Override
	public void setDataSource(DataSource dataSource) {
		LOGGER.info("table created ETRAINING");
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<EnergyModellerTraining> getByDeploymentId(String applicationid, String deploymentyid) {
		return jdbcTemplate.query(SQL_Q_DEPID,new Object[]{applicationid, deploymentyid}, new EnergyModellerTrainingMapper());
	}


	@Override
	public void terminateTraining(String applicationid, String deploymentid) {
		LOGGER.info("updating status");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		Timestamp ts = Timestamp.valueOf(dateFormat.format(cal.getTime()));
		int[] types = new int[] { Types.BOOLEAN, Types.TIMESTAMP , Types.VARCHAR,Types.VARCHAR };
		jdbcTemplate.update(SQL_Q_UPDATE,new Object[]{false, ts,  applicationid, deploymentid}, types);
		
	}

	@Override
	public List<EnergyModellerTraining> getByStatus() {
		return jdbcTemplate.query(SQL_Q_ALL, new EnergyModellerTrainingMapper());
	}




}
