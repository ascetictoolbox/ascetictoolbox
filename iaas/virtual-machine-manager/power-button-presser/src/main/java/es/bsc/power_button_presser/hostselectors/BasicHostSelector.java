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

import java.util.Collections;
import java.util.List;

public class BasicHostSelector implements HostSelector {
    
    @Override
    public List<Host> selectHostsToBeTurnedOn(List<Host> candidateHosts, int nHosts) {
        Collections.shuffle(candidateHosts);
        return candidateHosts.subList(0, Math.min(nHosts, candidateHosts.size()));
    }

    @Override
    public List<Host> selectHostsToBeTurnedOff(List<Host> candidateHosts, int nHosts) {
        Collections.shuffle(candidateHosts);
        return candidateHosts.subList(0, Math.min(nHosts, candidateHosts.size()));
    }

    @Override
    public List<Host> selectHostsToBeTurnedOn(List<Host> candidateHosts, int minTotalCpus, int maxTotalCpus) {
        //TODO
        return null;
    }

    @Override
    public List<Host> selectHostsToBeTurnedOff(List<Host> candidateHosts, int minTotalCpus, int maxTotalCpus) {
        //TODO
        return null;
    }

}
