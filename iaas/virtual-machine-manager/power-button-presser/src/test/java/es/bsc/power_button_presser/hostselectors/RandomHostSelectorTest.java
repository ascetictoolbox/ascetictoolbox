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

public class RandomHostSelectorTest {
    
    private final BasicHostSelector randomHostSelector = new BasicHostSelector();
    
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

    private List<Host> getTestHosts() {
        List<Host> candidateHosts = new ArrayList<>();
        candidateHosts.add(new Host("1", 1, 1, 1, 1, 1, 1, false));
        candidateHosts.add(new Host("2", 1, 1, 1, 1, 1, 1, false));
        candidateHosts.add(new Host("3", 1, 1, 1, 1, 1, 1, false));
        candidateHosts.add(new Host("4", 1, 1, 1, 1, 1, 1, false));
        return candidateHosts;
    }
    
    
}
