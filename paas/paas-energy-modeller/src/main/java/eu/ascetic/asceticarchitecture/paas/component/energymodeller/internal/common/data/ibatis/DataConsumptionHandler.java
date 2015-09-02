package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.DataConsumptionMapper;


public class DataConsumptionHandler {

	private static SqlSessionFactory sqlSessionFactory;
	private static DataConsumptionMapper mapper;
	private static DataConsumptionHandler instance;
	
	private final static Logger LOGGER = Logger.getLogger(DataConsumptionHandler.class.getName());
	
	
	private DataConsumptionHandler(String driver,String url,String uname,String pwd){
		TransactionFactory transactionFactory = new JdbcTransactionFactory();
		Environment environment = new Environment("development", transactionFactory, getDataSource(driver,url,uname,pwd));
		Configuration configuration = new Configuration(environment);
		configuration.addMapper(DataConsumptionMapper.class);
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
		LOGGER.info("Data collector set up complete");
	}
	
	public static DataConsumptionHandler getHandler(String driver,String url,String uname,String pwd){
		LOGGER.info("Returning the handler");
		if (instance==null) {
			instance = new DataConsumptionHandler(driver,url,uname,pwd);
			LOGGER.info("Returned the handler instance");
		}
		
		return instance;
		
		
	}

	private static UnpooledDataSource getDataSource(String driver,String url,String uname,String pwd) {
		UnpooledDataSource uds = new UnpooledDataSource(driver,url,uname,pwd);
		uds.setAutoCommit(true);
        return uds;
    }
	 
	public void initializeTable(UnpooledDataSource uds){
		
//		ScriptRunner sr;
//		try {
//			sr = new ScriptRunner(uds.getConnection());
//			Reader reader = new BufferedReader(new FileReader("createRegistry.sql"));
//			sr.runScript(reader);
//			LOGGER.info("Initialize the registry");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
	}
	
	public DataConsumptionMapper getMapper(){
		if (mapper==null) mapper = sqlSessionFactory.openSession(true).getMapper(DataConsumptionMapper.class);
		LOGGER.info("Returning the mapper");
		return mapper;
	}
}
