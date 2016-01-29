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
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public class HostStrengthComparatorTest {

    private final HostStrengthComparator hostStrengthComparator = new HostStrengthComparator();

    @Test
    public void comparisonShouldReturnPositiveWhenFirstHostStronger() {
        // Compare host1(CPUs = 2, ramMb = 1, diskGb = 1) vs host2(CPUs = 1, ramMb = 1, diskGb = 1)
        assertTrue(hostStrengthComparator.compare(
                new Host((long) 1, "1", 2, 1, 1, false),
                new Host((long) 2, "2", 1, 1, 1, false)) > 0);
    }

    @Test
    public void comparisonShouldReturnZeroWhenHostsEquallyStronger() {
        // Compare host1(CPUs = 2, ramMb = 1, diskGb = 1) vs host2(CPUs = 2, ramMb = 1, diskGb = 1)
        assertTrue(hostStrengthComparator.compare(
                new Host((long) 1, "1", 2, 1, 1, false),
                new Host((long) 2, "2", 2, 1, 1, false)) == 0);
    }

    @Test
    public void comparisonShouldReturnNegativeWhenFirstHostWeaker() {
        // Compare host1(CPUs = 1, ramMb = 1, diskGb = 1) vs host2(CPUs = 2, ramMb = 1, diskGb = 1)
        assertTrue(hostStrengthComparator.compare(
                new Host((long) 1, "1", 1, 1, 1, false),
                new Host((long) 2, "2", 2, 1, 1, false)) < 0);
    }
    
    @Test
    public void comparisonShouldReturnPositiveWhenFirstHostWasOff() {
        assertTrue(hostStrengthComparator.compare(
                new Host((long) 1, "1", 1, 1, 1, true),
                new Host((long) 2, "2", 2, 1, 1, false)) > 0);
    }

    @Test
    public void comparisonShouldReturnNegativeWhenSecondHostWasOff() {
        assertTrue(hostStrengthComparator.compare(
                new Host((long) 1, "1", 2, 1, 1, false),
                new Host((long) 2, "2", 1, 1, 1, true)) < 0);
    }

}
