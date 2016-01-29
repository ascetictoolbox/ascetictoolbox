/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.power_button_presser.hostselectors;

import es.bsc.power_button_presser.models.Host;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class RandomHostSelectorTest {
    
    private final RandomHostSelector randomHostSelector = new RandomHostSelector();
    
    @Test
    public void selectHostsToBeTurnedOn() {
        assertEquals(2, randomHostSelector.selectHostsToBeTurnedOn(getTestHosts(), 2).size());
    }
    
    @Test
    public void selectHostsToBeTurnedOnWhenTheGivenNumberIsGreaterThanTheNumberOfHosts() {
        assertEquals(getTestHosts().size(), 
                randomHostSelector.selectHostsToBeTurnedOn(getTestHosts(), getTestHosts().size() + 1).size());
    }

    @Test
    public void selectHostsToBeTurnedOff() {
        assertEquals(2, randomHostSelector.selectHostsToBeTurnedOff(getTestHosts(), 2).size());
    }

    @Test
    public void selectHostsToBeTurnedOffWhenTheGivenNumberIsGreaterThanTheNumberOfHosts() {
        assertEquals(getTestHosts().size(),
                randomHostSelector.selectHostsToBeTurnedOff(getTestHosts(), getTestHosts().size() + 1).size());
    }

    @Test
    public void selectHostsToBeTurnedOnGivenMinMax() {
        List<Host> candidateHosts = new ArrayList<>();
        candidateHosts.add(new Host("host1", 5, 1024, 1, 0, 0, 0, false));
        candidateHosts.add(new Host("host2", 10, 1024, 1, 0, 0, 0, false));
        candidateHosts.add(new Host("host3", 15, 1024, 1, 0, 0, 0, false));
        List<Host> selectedHosts = randomHostSelector.selectHostsToBeTurnedOn(candidateHosts, 2, 7);
        assertEquals(1, selectedHosts.size());
        assertEquals("host1", selectedHosts.get(0).getHostname());
    }
    
    @Test
    public void selectHostsToBeTurnedOffGivenMinMax() {
        List<Host> candidateHosts = new ArrayList<>();
        candidateHosts.add(new Host("host1", 5, 1024, 1, 0, 0, 0, false));
        candidateHosts.add(new Host("host2", 5, 1024, 1, 0, 0, 0, false));
        candidateHosts.add(new Host("host3", 20, 1024, 1, 0, 0, 0, false));
        List<Host> selectedHosts = randomHostSelector.selectHostsToBeTurnedOn(candidateHosts, 10, 18);
        assertEquals(2, selectedHosts.size());
        assertNotEquals("host3", selectedHosts.get(0).getHostname());
        assertNotEquals("host3", selectedHosts.get(1).getHostname());
    }
    
    private List<Host> getTestHosts() {
        List<Host> candidateHosts = new ArrayList<>();
        candidateHosts.add(new Host("1", 1, 1, 1, 1, 1, 1, false));
        candidateHosts.add(new Host("2", 1, 1, 1, 1, 1, 1, false));
        candidateHosts.add(new Host("3", 1, 1, 1, 1, 1, 1, false));
        candidateHosts.add(new Host("4", 1, 1, 1, 1, 1, 1, false));
        return candidateHosts;
    }
    
    
}
