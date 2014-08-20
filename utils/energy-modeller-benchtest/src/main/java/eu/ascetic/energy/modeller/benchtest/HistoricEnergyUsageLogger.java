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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HistoricUsageRecord;
import eu.ascetic.ioutils.GenericLogger;
import eu.ascetic.ioutils.ResultsStore;
import java.io.File;
import java.text.SimpleDateFormat;

/**
 * This logs out a historic energy usage record to disk.
 * @author Richard
 */
public class HistoricEnergyUsageLogger extends GenericLogger<HistoricUsageRecord> {

    private static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    
    /**
     * This creates a historic energy usage record logger.
     * @param file The file to write to
     * @param overwrite If the file should be overwritten on startup.
     */
    public HistoricEnergyUsageLogger(File file, boolean overwrite) {
        super(file, overwrite);
    }
    
    @Override
    public void writeHeader(ResultsStore store) {
        store.add("Start Time");
        store.append("End Time");
        store.append("Duration");        
        store.append("Average Power");
        store.append("Total Energy");
        store.append("Average Current");
        store.append("Average Voltage");
    }

    @Override
    public void writebody(HistoricUsageRecord item, ResultsStore store) {
        store.add(format.format(item.getDuration().getStartTime().getTime()));
        store.append(format.format(item.getDuration().getEndTime().getTime()));
        store.append(item.getDuration().getDuration());
        store.append(item.getAvgPowerUsed());
        store.append(item.getTotalEnergyUsed());
        store.append(item.getAvgCurrentUsed());
        store.append(item.getAvgVoltageUsed());
    }
    
}

