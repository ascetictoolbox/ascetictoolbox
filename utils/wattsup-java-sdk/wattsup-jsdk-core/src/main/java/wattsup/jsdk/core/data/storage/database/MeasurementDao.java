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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.Interval;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import wattsup.jsdk.core.data.Measurement;
import wattsup.jsdk.core.data.storage.database.MeasurementDao.MeasurementMapper;

@RegisterMapper(MeasurementMapper.class)
public interface MeasurementDao
{
    /**
     * @param measurement
     */
    @SqlUpdate("INSERT INTO measurement (time, watts, volts, amps, wattskwh, maxwatts, maxvolts, maxamps,minwatts, minvolts, minamps, powerfactor, dutycycle, powercycle) VALUES (:time, :watts, :volts, :amps, :wattsKWh, :maxWatts, :maxVolts, :maxAmps, :minWatts, :minVolts, :minAmps, :powerFactor, :dutyCycle, :powerCycle)")
    void insert(@BindBean Measurement measurement);

    /**
     * Returns all measurements, in ascending order, realized in a given interval.
     * 
     * @param interval
     *            Interval to filter the measurements.
     * @return A not null {@link List} with the measurements realized in the given interval.
     */
    @SqlQuery("SELECT id, time, watts, volts, amps, wattskwh, maxwatts, maxvolts, maxamps, minwatts, minvolts, minamps, powerfactor, dutycycle, powercycle FROM measurement where time between :it.startMillis and :it.endMillis order by time asc")
    List<Measurement> findInInterval(@Bind("it") Interval interval);

    /**
     * Returns all measurements, in ascending order, realized in a given interval.
     * 
     * @param fromInMillis
     *            Start time in milliseconds.
     * @param toInMillis
     *            End time in milliseconds.
     * @return A not null {@link List} with the measurements realized in the given interval.
     */
    @SqlQuery("SELECT id, time, watts, volts, amps, wattskwh, maxwatts, maxvolts, maxamps, minwatts, minvolts, minamps, powerfactor, dutycycle, powercycle FROM measurement where time between :from and :to order by time asc")
    List<Measurement> findInInterval(@Bind("from") long fromInMillis, @Bind("to") long toInMillis);

    /**
     * Returns all measurements, in ascending order.
     * 
     * @return A not <code>null</code> {@link List} with all measurements.
     */
    @SqlQuery("SELECT id, time, watts, volts, amps, wattskwh, maxwatts, maxvolts, maxamps,minwatts, minvolts, minamps, powerfactor, dutycycle, powercycle FROM measurement order by time asc")
    List<Measurement> getAllMeasurments();

    /**
     * Counts and returns the number of measurements realized in a given interval.
     * 
     * @param fromInMillis
     *            Start time in milliseconds.
     * @param toInMillis
     *            End time in milliseconds.
     * @return Number of records realized in the given interval.
     */
    @SqlQuery("SELECT count(time) FROM measurement where time >= :from and time <= :to")
    int countInInterval(@Bind("from") long fromInMillis, @Bind("to") long toInMillis);

    /**
     * Removes the measurements in a given interval.
     * 
     * @param fromInMillis
     *            Start time in milliseconds.
     * @param toInMillis
     *            End time in milliseconds.
     * @return Number of measurements deleted.
     */
    @SqlQuery("DELETE FROM measurement where time >= :from and time <= :to")
    int deleteInInterval(@Bind("from") long fromInMillis, @Bind("to") long toInMillis);

    /**
     * Close the SQL connection.
     */
    void close();

    public static final class MeasurementMapper implements ResultSetMapper<Measurement>
    {
        @Override
        public Measurement map(int index, ResultSet rs, StatementContext ctx) throws SQLException
        {
            int i = 2;
            Measurement measurement = new Measurement();
            measurement.setTime(rs.getLong(i++));
            measurement.setWatts(rs.getDouble(i++));
            measurement.setVolts(rs.getDouble(i++));
            measurement.setAmps(rs.getDouble(i++));
            measurement.setWattsKWh(rs.getDouble(i++));
            measurement.setMaxWatts(rs.getDouble(i++));
            measurement.setMaxVolts(rs.getDouble(i++));
            measurement.setMaxAmps(rs.getDouble(i++));
            measurement.setMinWatts(rs.getDouble(i++));
            measurement.setMinVolts(rs.getDouble(i++));
            measurement.setMinAmps(rs.getDouble(i++));
            measurement.setPowerFactor(rs.getDouble(i++));
            measurement.setDutyCycle(rs.getDouble(i++));
            measurement.setPowerCycle(rs.getDouble(i++));

            return measurement;
        }
    }
}
