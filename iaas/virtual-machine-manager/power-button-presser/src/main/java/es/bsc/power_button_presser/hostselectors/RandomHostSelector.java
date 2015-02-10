package es.bsc.power_button_presser.hostselectors;

import es.bsc.power_button_presser.models.Host;

import java.util.Collections;
import java.util.List;

public class RandomHostSelector implements HostSelector {
    
    @Override
    public List<Host> selectHostsToBeTurnedOn(List<Host> candidateHosts, int nHosts) {
        int hostsToTurnOn = Math.min(nHosts, candidateHosts.size());
        Collections.shuffle(candidateHosts);
        return candidateHosts.subList(0, hostsToTurnOn);
    }

    @Override
    public List<Host> selectHostsToBeTurnedOff(List<Host> candidateHosts, int nHosts) {
        int hostsToTurnOff = Math.min(nHosts, candidateHosts.size());
        Collections.shuffle(candidateHosts);
        return candidateHosts.subList(0, hostsToTurnOff);
    }
    
}
