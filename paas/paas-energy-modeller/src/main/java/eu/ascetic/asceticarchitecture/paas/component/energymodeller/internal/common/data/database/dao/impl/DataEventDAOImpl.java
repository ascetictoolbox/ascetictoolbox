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
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.DataeEventDAO;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.mapper.DataEventMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataEvent;

public class DataEventDAOImpl implements DataeEventDAO {


	private JdbcTemplate jdbcTemplate;
	private final static Logger LOGGER = Logger.getLogger(DataEventDAOImpl.class.getName());
	private static String SQL_CREATE="CREATE TABLE IF NOT EXISTS DATAEVENT (applicationid varchar(50),deploymentid varchar(50),"
			+ "vmid varchar(50), eventid varchar(50), data varchar(100), starttime bigint,endtime bigint, energy double)";
	private static String SQL_INSERT="insert ignore into DATAEVENT (applicationid,deploymentid,vmid, data, starttime,endtime, energy,eventid ) values (?,?,?, ?, ?, ?, ?,?) ON DUPLICATE KEY IGNORE ";
	private static String SQL_Q_APPID="select * from DATAEVENT where applicationid = ? and vmid = ? and eventid = ?";
	private static String SQL_Q_DEPID="select * from DATAEVENT where applicationid = ? and deploymentid = ? and vmid = ? and eventid = ?";
	private static String SQL_Q_APPIDTime="select * from DATAEVENT where applicationid = ? and vmid = ? and eventid = ? and starttime >= ? and starttime <= ?";
	private static String SQL_Q_DEPIDTime="select * from DATAEVENT where applicationid = ? and deploymentid = ? and vmid = ? and eventid = ? and starttime >= ? and starttime <= ?";
	private static String SQL_CLEAN="DELETE FROM DATAEVENT";
	
	public void initialize() {
		// TODO: Add a purge policy to check if data need to be cleaned up at each restart
	    LOGGER.debug("Created table DATAEVENT");
	}
	
	
	
	@Override
	public void save(DataEvent data) {
		LOGGER.debug("Inserting into table DATAEVENT");
		 Object[] params = new Object[] { data.getApplicationid() , data.getDeploymentid() , data.getVmid(), data.getData(), data.getBegintime(), data.getEndtime() , data.getEnergy(), data.getEventid() };
		 int[] types = new int[] { Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.BIGINT,Types.BIGINT, Types.DOUBLE, Types.VARCHAR };
		 jdbcTemplate.update(SQL_INSERT, params, types);
		 LOGGER.debug("Inserted");
	}

	
	public void setDataSource(DataSource dataSource) {
		LOGGER.debug("table created DATAEVENT");
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<DataEvent> getByApplicationId(String applicationid,String vmid,String eventid) {
		try{
			return jdbcTemplate.query(SQL_Q_APPID,new Object[]{applicationid,vmid,eventid}, new DataEventMapper());
		}catch (Exception e){
			return null;
		}
	}
	@Override
	public List<DataEvent> getByApplicationIdTime(String applicationid,String vmid, String eventid ,Timestamp start,Timestamp end) {
		try{
			LOGGER.info("times "+start.getTime()+" " + end.getTime());
			return jdbcTemplate.query(SQL_Q_APPIDTime,new Object[]{applicationid,vmid,eventid,start.getTime(),end.getTime()}, new DataEventMapper());
		}catch (Exception e){
			return null;
		}
	}

	public void purgedata(String appid,String vmid,String eventid){
		String delete = "DELETE FROM DATAEVENT WHERE applicationid = \""+appid+"\" and vmid=\""+vmid+"\" and eventid=\""+eventid+"\"";
		LOGGER.info("delete "+delete);
		jdbcTemplate.execute(delete);
	}


	@Override
	public List<DataEvent> getByDeployIdTime(String applicationid, String deploymentid,  String vmid,	String eventid, Timestamp start, Timestamp end) {
		try{
			LOGGER.info("times "+start.getTime()+" " + end.getTime());
			return jdbcTemplate.query(SQL_Q_DEPIDTime,new Object[]{applicationid,deploymentid,vmid,eventid,start.getTime(),end.getTime()}, new DataEventMapper());
		}catch (Exception e){
			return null;
		}
	}

	@Override
	public List<DataEvent> getByDeployId(String applicationid, String deploymentid, String vmid, String eventid) {
		try{
			return jdbcTemplate.query(SQL_Q_DEPID,new Object[]{applicationid, deploymentid,vmid,eventid}, new DataEventMapper());
		}catch (Exception e){
			return null;
		}
	}
	

}
