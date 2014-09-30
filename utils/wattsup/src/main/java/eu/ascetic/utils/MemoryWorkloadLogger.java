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
package eu.ascetic.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.hyperic.sigar.Mem;

/**
 *
 * @author Richard
 */
public class MemoryWorkloadLogger extends GenericLogger<Mem> {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

    /**
     * This creates a cpu utilisation usage logger.
     *
     * @param file The file to write to
     * @param overwrite If the file should be overwritten on startup.
     */
    public MemoryWorkloadLogger(File file, boolean overwrite) {
        super(file, overwrite);
    }

    @Override
    public void writeHeader(ResultsStore store) {
        store.add("Date");
        store.append("Actual Free");
        store.append("Acutal Used");
        store.append("Free");
        store.append("Free Percentage");
        store.append("Ram");
        store.append("Total");
        store.append("Used");
        store.append("Used Percentage");
    }

    @Override
    public void writebody(Mem item, ResultsStore store) {
        store.add(FORMAT.format(new Date()));
        store.append(item.getActualFree());
        store.append(item.getActualUsed());
        store.append(item.getFree());
        store.append(item.getFreePercent());
        store.append(item.getRam());
        store.append(item.getTotal());
        store.append(item.getUsed());
        store.append(item.getUsedPercent());
    }

}
