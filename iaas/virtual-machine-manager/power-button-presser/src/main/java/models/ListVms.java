package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListVms {

    private final List<Vm> vms;

    public ListVms(List<Vm> vms) {
        this.vms = new ArrayList<>(vms);
    }

    public List<Vm> getVms() {
        return Collections.unmodifiableList(vms);
    }
    
}
