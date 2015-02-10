package hostselectors;

import models.Host;

import java.util.List;

public interface HostSelector {
    
    public List<Host> selectHostsToBeTurnedOn(List<Host> candidateHosts, int nHosts);
    
    public List<Host> selectHostsToBeTurnedOff(List<Host> candidateHosts, int nHosts);
    
}
