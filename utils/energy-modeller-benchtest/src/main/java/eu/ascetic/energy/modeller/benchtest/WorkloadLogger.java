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

import eu.ascetic.ioutils.GenericLogger;
import eu.ascetic.ioutils.ResultsStore;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.hyperic.sigar.CpuPerc;

/**
 *
 * @author Richard
 */
public class WorkloadLogger extends GenericLogger<CpuPerc> {

    private static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

    /**
     * This creates a cpu utilisation usage logger.
     *
     * @param file The file to write to
     * @param overwrite If the file should be overwritten on startup.
     */
    public WorkloadLogger(File file, boolean overwrite) {
        super(file, overwrite);
    }

    @Override
    public void writeHeader(ResultsStore store) {
        store.add("Date");
        store.append("Idle");
        store.append("Irq");
        store.append("Nice");
        store.append("SoftIrq");
        store.append("Stolen");
        store.append("System");
        store.append("User");
        store.append("Wait");
        store.append("Combined");
    }

    @Override
    public void writebody(CpuPerc item, ResultsStore store) {
        store.add(format.format(new Date()));
        store.append(item.getIdle());
        store.append(item.getIrq());
        store.append(item.getNice());
        store.append(item.getSoftIrq());
        store.append(item.getStolen());
        store.append(item.getSys());
        store.append(item.getUser());
        store.append(item.getWait());
        store.append(item.getCombined());
    }

}
