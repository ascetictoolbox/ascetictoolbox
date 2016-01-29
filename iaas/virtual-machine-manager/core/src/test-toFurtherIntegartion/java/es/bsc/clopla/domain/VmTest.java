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

package es.bsc.demiurge.core.clopla.domain;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class VmTest {
    
    @Test
    public void isInTheSameHostWorksWhenVmsInSameHost() {
        Host host = new Host((long) 1, "1", 4, 4096, 4, false);
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        Vm vm2 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        vm1.setHost(host);
        vm2.setHost(host);
        assertTrue(vm1.isInTheSameHost(vm2));
    }
    
    @Test
    public void isInTheSameHostWorksWhenVmsAreInDifferentHosts() {
        Host host1 = new Host((long) 1, "1", 4, 4096, 4, false);
        Host host2 = new Host((long) 2, "2", 2, 2048, 2, false);
        Vm vm1 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        Vm vm2 = new Vm.Builder((long) 1, 1, 1024, 1).build();
        vm1.setHost(host1);
        vm2.setHost(host2);
        assertFalse(vm1.isInTheSameHost(vm2));
    }
    
}
