package integratedtoolkit.types;

import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.types.resources.Worker;

public class AsceticProfile extends Profile {

    private final Worker worker;
    private final Implementation impl;
    private final AllocatableAction action;

    public AsceticProfile(Worker w, Implementation impl, AllocatableAction action) {
        this.worker = w;
        this.impl = impl;
        this.action = action;
    }

    public AsceticProfile(Worker w, Implementation impl) {
        this.worker = w;
        this.impl = impl;
        this.action = null;
        long defaultTime = Ascetic.getExecutionTime(worker, impl);
        this.minTime = defaultTime;
        this.averageTime = defaultTime;
        this.maxTime = defaultTime;
    }

    
    public double getPower() {
        return Ascetic.getPower(worker, impl);
    }

    public double getPrice() {
        return Ascetic.getPrice(worker, impl);
    }

    @Override
    public void start() {
        super.start();
        Ascetic.startEvent(worker, impl, action);
    }

    @Override
    public void end() {
        super.end();
        Ascetic.stopEvent(worker, impl, action);
    }
}
