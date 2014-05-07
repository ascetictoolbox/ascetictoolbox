/**
 *     Copyright (C) 2013 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package wattsup.jsdk.core.data.storage.database;

import java.util.List;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import wattsup.jsdk.core.data.Measurement;

import static junit.framework.Assert.*;


public class MeasurementDaoTest
{
    private MeasurementDao measurementDao;
    
    private final String table = "create table measurement (" +
    		"id IDENTITY not null primary key," +
    		"time long not null," +
    		"watts double not null," +
    		"volts double not null, " +
    		"amps double not null, " +
    		"wattskwh double not null, " +
    		"maxwatts double not null," +
    		"maxvolts double not null," +
    		"maxamps double not null," +
    		"minwatts double not null," +
    		"minvolts double not null," +
    		"minamps double not null," +
    		"powerfactor double not null," +
    		"dutycycle double not null," +
    		"powercycle double not null)";

    @Before
    public void setup() throws ClassNotFoundException
    {
        DataSource ds = JdbcConnectionPool.create("jdbc:h2:mem:test", "sa", "sa");
        DBI dbi = new DBI(ds);
        
        Handle h = dbi.open();
        h.execute(table);
        h.close();
        
        measurementDao = dbi.open(MeasurementDao.class);
    }
    
    @Test
    public void must_insert_one_measurement()
    {
        Measurement measurement = new Measurement();
        measurement.setAmps(10);
        measurement.setDutyCycle(1);
        measurement.setMaxAmps(10);
        measurement.setMaxVolts(119);
        measurement.setMaxWatts(101);
        measurement.setMinAmps(10);
        measurement.setMinVolts(119);
        measurement.setMinWatts(50);
        measurement.setPowerCycle(1.2);
        measurement.setPowerFactor(1);
        measurement.setTime(System.currentTimeMillis());
        measurement.setVolts(119);
        measurement.setWatts(62);
        measurement.setWattsKWh(100);
        
        measurementDao.insert(measurement);
        
        List<Measurement> measurements = measurementDao.getAllMeasurments();
        assertNotNull(measurements);
        assertEquals(1, measurements.size());
        
        assertEquals(measurement, measurements.get(0));
    }
    
}
