package integratedtoolkit.types;

import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.types.resources.Worker;

public class AsceticProfile extends Profile {

    private Worker worker;
    private Implementation impl;

    public AsceticProfile() {

    }

    public AsceticProfile(Worker w, Implementation impl) {
        this.worker = w;
        this.impl = impl;
        long defaultTime = Ascetic.getExecutionTime(worker, impl);
        this.minTime = defaultTime;
        this.averageTime = defaultTime;
        this.maxTime = defaultTime;
    }

    public double getEnergy() {
        return Ascetic.getPower(worker, impl) * this.averageTime;
    }

    public double getPrice() {
        return Ascetic.getPrice(worker, impl) * this.averageTime;
    }

}
