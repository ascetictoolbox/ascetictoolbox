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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;
import eu.ascetic.ioutils.GenericLogger;
import eu.ascetic.ioutils.ResultsStore;
import java.io.File;

/**
 * This logs Host energy records to disk.
 * @author Richard
 */
public class HostEnergyTraceDataLogger extends GenericLogger<HostEnergyRecord> {

    public HostEnergyTraceDataLogger(File file, boolean overwrite) {
        super(file, overwrite);
    }

    @Override
    public void writeHeader(ResultsStore store) {
        store.add("Time");
        store.append("Host Name");
        store.append("Host Id");
        store.append("Power");
        store.append("Energy");
    }

    @Override
    public void writebody(HostEnergyRecord item, ResultsStore store) {
        store.add(item.getTime());
        store.append(item.getHost().getHostName());
        store.append(item.getHost().getId());
        store.append(item.getPower());
        store.append(item.getEnergy());
    }

}
