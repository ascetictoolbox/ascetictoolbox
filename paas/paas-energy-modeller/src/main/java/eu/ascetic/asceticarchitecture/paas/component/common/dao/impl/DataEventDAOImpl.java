package eu.ascetic.asceticarchitecture.paas.component.common.dao.impl;

import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.DataeEventDAO;
import eu.ascetic.asceticarchitecture.paas.component.common.mapper.DataEventMapper;
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataEvent;

public class DataEventDAOImpl implements DataeEventDAO {

	private JdbcTemplate jdbcTemplate;
	private final static Logger LOGGER = Logger.getLogger(DataEventDAOImpl.class.getName());
	private static String SQL_CREATE="CREATE TABLE IF NOT EXISTS DATAEVENT (applicationid varchar(20),deploymentid varchar(20),"
			+ "vmid varchar(20), eventid varchar(20), time timestamp, energy double)";
	private static String SQL_INSERT="insert into DATAEVENT (applicationid,deploymentid,vmid, time, energy ) values (?, ?, ?, ?, ?) ";
	private static String SQL_Q_APPID="select * from DATAEVENT where applicationid = ?";
	private static String SQL_Q_DEPID="select * from DATAEVENT where deploymentid = ?";
	private static String SQL_Q_VMID="select * from DATAEVENT where vmid = ?";
	
	@Override
	public void initialize() {
		jdbcTemplate.execute(SQL_CREATE);
	    LOGGER.info("Created table DATAEVENT");
	}
	
	@Override
	public void save(DataEvent data) {
		LOGGER.info("Inserting into table DATAEVENT");
		 Object[] params = new Object[] { data.getApplicationid() , data.getDeploymentid() , data.getVmid(), data.getTime() , data.getEnergy() };
		 int[] types = new int[] { Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.TIMESTAMP, Types.DOUBLE };
		 jdbcTemplate.update(SQL_INSERT, params, types);
		 LOGGER.info("Inserted");
		
	}

	@Override
	public void setDataSource(DataSource dataSource) {
		LOGGER.info("table created DATAEVENT");
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<DataEvent> getByApplicationId(String applicationid) {
		return jdbcTemplate.query(SQL_Q_APPID,new Object[]{applicationid}, new DataEventMapper());
		
	}

	@Override
	public List<DataEvent> getByDeploymentId(String deploymentyid) {
		return jdbcTemplate.query(SQL_Q_DEPID,new Object[]{deploymentyid}, new DataEventMapper());
	}

	@Override
	public List<DataEvent> getByVMId(String vmid) {
		return jdbcTemplate.query(SQL_Q_VMID,new Object[]{vmid}, new DataEventMapper());
	}


}
