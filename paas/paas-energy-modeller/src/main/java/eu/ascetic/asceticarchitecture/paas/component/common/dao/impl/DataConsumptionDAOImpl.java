package eu.ascetic.asceticarchitecture.paas.component.common.dao.impl;

import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.DataConsumptionDAO;
import eu.ascetic.asceticarchitecture.paas.component.common.mapper.DataConsumptionMapper;
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataConsumption;

public class DataConsumptionDAOImpl implements DataConsumptionDAO {

	private JdbcTemplate jdbcTemplate;
	private final static Logger LOGGER = Logger.getLogger(DataConsumptionDAOImpl.class.getName());
	private static String SQL_CREATE="CREATE TABLE IF NOT EXISTS DATACONSUMPTION (applicationid varchar(20),deploymentid varchar(20),"
			+ "vmid varchar(20), eventid varchar(20), starttime timestamp, endtime timestamp, cpu double, memory double, disk double, network double)";
	private static String SQL_INSERT="insert into DATACONSUMPTION (applicationid,deploymentid,vmid, eventid, starttime,endtime, cpu , memory , disk , network ) values (?, ?, ?, ? ,?, ?, ?, ?, ?, ?) ";
	private static String SQL_Q_APPID="select * from DATACONSUMPTION where applicationid = ?";
	private static String SQL_Q_DEPID="select * from DATACONSUMPTION where deploymentid = ?";
	private static String SQL_Q_VMID="select * from DATACONSUMPTION where vmid = ?";
	private static String SQL_Q_EMID="select * from DATACONSUMPTION where eventid = ?";
	
	@Override
	public void initialize() {
		jdbcTemplate.execute(SQL_CREATE);
	    LOGGER.info("Created table DATACONSUMPTION");
	}

	@Override
	public void setDataSource(DataSource dataSource) {
		
		 this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void save(DataConsumption data) {
		 LOGGER.info("Inserting into table DATACONSUMPTION");
		 Object[] params = new Object[] { data.getApplicationid() , data.getDeploymentid() , data.getVmid(), data.getEventid(), data.getEndtime() , data.getStarttime(), data.getCpu() , data.getMemory(), data.getDisk(), data.getNetwork() };
		 int[] types = new int[] { Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.TIMESTAMP,Types.TIMESTAMP, Types.DOUBLE, Types.DOUBLE,Types.DOUBLE,Types.DOUBLE };
		 jdbcTemplate.update(SQL_INSERT, params, types);
		 LOGGER.info("Inserted");
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

}
