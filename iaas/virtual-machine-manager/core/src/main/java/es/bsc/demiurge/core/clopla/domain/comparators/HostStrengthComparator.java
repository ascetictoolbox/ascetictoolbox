/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package es.bsc.demiurge.core.clopla.domain.comparators;

import es.bsc.demiurge.core.clopla.domain.Host;

import java.io.Serializable;
import java.util.Comparator;

/**
 * This class compares the "strength" of two hosts. Hosts with more resources are considered to be "stronger"
 * because they are more likely to meet the requirements needed to deploy a VM.
 * Comparing the "strength" of two hosts is needed to apply some construction heuristic algorithms like the best fit
 * (aka weakest fit).
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public class HostStrengthComparator implements Comparator<Host>, Serializable {

    /**
     * This function compares the "strength" of two hosts.
     *
     * @param host1 a host
     * @param host2 a host
     * @return a positive number if host1 was off at the start of the planning and host2 was on.
     * a negative number if host2 was off at the start of the planning and host1 was on.
     * If both of them were on/off, a negative number if host1 is weaker than host2, 
     * a positive number if host1 is stronger than host2, and 0 if they have the same strength.
     */
    @Override
    public int compare(Host host1, Host host2) {
        if (host1.wasOffInitiallly() && !host2.wasOffInitiallly()) {
            return 1;
        }
        else if (!host1.wasOffInitiallly() && host2.wasOffInitiallly()) {
            return -1;
        }
        return Double.compare(strength(host1), strength(host2));
    }

    /**
     * This function calculate the strength of a host.
     * This is the formula used to calculate the difficulty: host.cpus * (host.ramMb/1000) * (host.diskGb/100).
     * The memory and the disk capacity are divided in the formula because it would not be fair to give the same
     * weight to 1 CPU than to 1 MB or RAM.
     */
    private double strength(Host host) {
        return host.getNcpus()*(host.getRamMb()/1000.0)*(host.getDiskGb()/100.0);
    }

}
