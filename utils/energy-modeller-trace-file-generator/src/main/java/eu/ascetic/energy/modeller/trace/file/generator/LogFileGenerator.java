/**
 * Copyright 2014 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.energy.modeller.trace.file.generator;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.ioutils.Settings;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * This hooks into the energy modellers stored dataset and outputs all
 * information regarding hosts and their VMs to disk.
 */
public class LogFileGenerator {

    private static final DataCollector collector = new DataCollector(new DefaultDatabaseConnector());

    public static void main(String[] args) throws ParseException {
        Settings settings = new Settings("energymodeller_dump.properties");
        GregorianCalendar defaultStart = new GregorianCalendar();
        long durationMills = TimeUnit.HOURS.toMillis(1);
        defaultStart.setTimeInMillis(defaultStart.getTimeInMillis() - durationMills);
        int start = settings.getInt("start-unix-time", 0);
        int end = settings.getInt("end-unix-time", 0);
        TimePeriod period = new TimePeriod(start, end);
        if (settings.isChanged()) {
            settings.save("energymodeller_dump.properties");
        }
        collector.gatherData(period);
    }
}
