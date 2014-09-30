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
package eu.ascetic.energy.modeller.benchtest;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.CurrentUsageRecord;
import eu.ascetic.ioutils.GenericLogger;
import eu.ascetic.ioutils.ResultsStore;
import java.io.File;
import java.text.SimpleDateFormat;

/**
 * This logs out a current energy usage record to disk.
 *
 * @author Richard
 */
public class CurrentEnergyUsageLogger extends GenericLogger<CurrentUsageRecord> {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

    /**
     * This creates a current energy usage logger.
     * @param file The file to write to
     * @param overwrite If the file should be overwritten on startup.
     */
    public CurrentEnergyUsageLogger(File file, boolean overwrite) {
        super(file, overwrite);
    }

    @Override
    public void writeHeader(ResultsStore store) {
        store.add("Time");
        store.append("Power");
        store.append("Current");
        store.append("Voltage");
    }

    @Override
    public void writebody(CurrentUsageRecord item, ResultsStore store) {
        store.add(FORMAT.format(item.getTime().getTime()));
        store.append(item.getPower());
        store.append(item.getCurrent());
        store.append(item.getVoltage());
    }

}
