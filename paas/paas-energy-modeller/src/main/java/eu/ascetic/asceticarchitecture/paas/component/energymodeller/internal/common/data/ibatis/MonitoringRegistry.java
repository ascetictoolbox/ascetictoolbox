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

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.MonitoringMapper;

/**
 * 
 * @author sommacam
 * build a connection with the databse to store and retrieve data about application
 */
public class MonitoringRegistry {

	private static SqlSessionFactory sqlSessionFactory;
	//private static AppRegistryMapper mapper;
	private static MonitoringRegistry instance;
	private final static Logger LOGGER = Logger.getLogger(MonitoringRegistry.class.getName());

	
	public static MonitoringRegistry getRegistry(String driver,String url,String uname,String pwd){
		LOGGER.info("Returning the registry");
		if (instance==null) {
			instance = new MonitoringRegistry(driver,url,uname,pwd);
			LOGGER.info("Returned the registry instance");
		}
		return instance;
	}
	
	
	private MonitoringRegistry(String driver,String url,String uname,String pwd){
		TransactionFactory transactionFactory = new JdbcTransactionFactory();
		Environment environment = new Environment("development", transactionFactory, getDataSource(driver,url,uname,pwd));
		Configuration configuration = new Configuration(environment);
		configuration.addMapper(MonitoringMapper.class);
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
		LOGGER.info("Registry set up complete");
	}

	private static PooledDataSource getDataSource(String driver,String url,String uname,String pwd) {
		
		PooledDataSource pooledConnection = new PooledDataSource(driver,url,uname,pwd);
		pooledConnection.setDefaultAutoCommit(true);
		pooledConnection.setPoolPingQuery("SELECT 1");
		pooledConnection.setPoolPingConnectionsNotUsedFor(1500000);
		pooledConnection.setPoolPingEnabled(true);
        return pooledConnection;
    }
	 
	public SqlSession getSession(){
		return sqlSessionFactory.openSession(true);
	}
	
}
