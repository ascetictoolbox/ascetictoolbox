/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
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

	private static PooledDataSource getDataSource(String driver,String url,String uname,String pwd) {
		PooledDataSource pds = new PooledDataSource(driver,url,uname,pwd);
		pds.setDefaultAutoCommit(true);
		pds.setPoolPingQuery("SELECT 1");
		pds.setPoolPingConnectionsNotUsedFor(1500000);
		pds.setPoolPingEnabled(true);
        return pds;
    }
	 

//	public DataConsumptionMapper getMapper(){
//		if (mapper==null) mapper = sqlSessionFactory.openSession(true).getMapper(DataConsumptionMapper.class);
//		LOGGER.info("Returning the mapper");
//		return mapper;
//	}
	
	public SqlSession getSession(){
		return sqlSessionFactory.openSession(true);
	}
}
