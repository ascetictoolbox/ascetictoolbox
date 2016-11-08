package integratedtoolkit.types;

import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.types.resources.Worker;

public class AsceticProfile extends Profile {

    protected final Worker worker;
    protected final Implementation impl;
    protected final AllocatableAction action;

    protected AsceticProfile() {
        super();
        this.worker = null;
        this.impl = null;
        this.action = null;
    }

    public AsceticProfile(Worker w, Implementation impl, AllocatableAction action) {
        super();
        this.worker = w;
        this.impl = impl;
        this.action = action;
    }

    public AsceticProfile(Worker w, Implementation impl) {
        super();
        this.worker = w;
        this.impl = impl;
        this.action = null;
        long defaultTime = Ascetic.getExecutionTime(worker, impl);
        this.minTime = defaultTime;
        this.averageTime = defaultTime;
        this.maxTime = defaultTime;
        this.sample=1;
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
