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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;



import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.log4j.Logger;
import org.apache.ibatis.datasource.pooled.PooledDataSource;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.AppRegistryMapper;


public class ApplicationRegistry {

	private static SqlSessionFactory sqlSessionFactory;
	//private static AppRegistryMapper mapper;
	private static ApplicationRegistry instance;
	private final static Logger LOGGER = Logger.getLogger(ApplicationRegistry.class.getName());

	
	public static ApplicationRegistry getRegistry(String driver,String url,String uname,String pwd){
		LOGGER.info("Returning the registry");
		if (instance==null) {
			instance = new ApplicationRegistry(driver,url,uname,pwd);
			LOGGER.info("Returned the registry instance");
		}
		return instance;
	}
	
	
	private ApplicationRegistry(String driver,String url,String uname,String pwd){
		TransactionFactory transactionFactory = new JdbcTransactionFactory();
		Environment environment = new Environment("development", transactionFactory, getDataSource(driver,url,uname,pwd));
		Configuration configuration = new Configuration(environment);
		configuration.addMapper(AppRegistryMapper.class);
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
	
//	public AppRegistryMapper getMapper(){
//	mapper = sqlSessionFactory.openSession(true).getMapper(AppRegistryMapper.class);
//	LOGGER.info("Returning the mapper");
//	
//	
//	
//	return mapper;
//}
}
