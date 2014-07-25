package eu.ascetic.asceticarchitecture.paas.component.common.dao.impl;

import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.IaaSDataDAO;
import eu.ascetic.asceticarchitecture.paas.component.common.mapper.IaaSVMConsumptionMapper;
import eu.ascetic.asceticarchitecture.paas.component.common.model.IaaSVMConsumption;

public class IaaSDataDAOImpl implements IaaSDataDAO {

	private JdbcTemplate jdbcTemplate;
	private final static Logger LOGGER = Logger.getLogger(IaaSDataDAOImpl.class.getName());
	// queries
	private static String QUERY_GETHOSTCPU="select cpu from host_calibration_data where host_id = ?";
	private static String QUERY_GETHOSTFORVM="select host_id from vm_measurement where vm_id = ? group by host_id";
	private static String QUERY_GETENERGYFORVM="select vmm.vm_id as vm_id,hmm.energy as energy ,vmm.clock as clock ,vmm.cpu_load as cpu, hmm.host_id as host_id "
			+ "								from vm_measurement as vmm, host_measurement as hmm "
			+ "								where vmm.host_id = ? and vmm.vm_id = ? "
						+ "						and hmm.host_id=vmm.host_id and vmm.clock = hmm.clock";
	
	
	@Override
	public void initialize() {
		  LOGGER.info("Initialized connection to table IaaS VM data");
	}

	@Override
	public void setDataSource(DataSource dataSource) {
		LOGGER.info("datasource for IaaS VM Data ready");
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		
	}

	@Override
	public String getHostIdForVM(String VMid) {
		LOGGER.info("getting host for vm "+VMid);
		return jdbcTemplate.queryForObject(	QUERY_GETHOSTFORVM, new Object[] { VMid }, String.class);
		
	}

	@Override
	public  String getHostTotalCpu(String hostid) {
		LOGGER.info("getting cpu for host "+hostid);
		return jdbcTemplate.queryForObject(	QUERY_GETHOSTCPU, new Object[] { hostid }, String.class);
	}

	@Override
	public List<IaaSVMConsumption> getEnergyForVM(String hostid, String vmid) {
		LOGGER.info("getting vm consumption for "+hostid);
		List<IaaSVMConsumption> data = jdbcTemplate.query(QUERY_GETENERGYFORVM,new Object[] { hostid,vmid },new IaaSVMConsumptionMapper());
		return data;
	}

}
