package integratedtoolkit.types;

import integratedtoolkit.scheduler.ascetic.AsceticSchedulingInformation;
import integratedtoolkit.scheduler.exceptions.BlockedActionException;
import integratedtoolkit.scheduler.exceptions.FailedActionException;
import integratedtoolkit.scheduler.exceptions.UnassignedActionException;
import integratedtoolkit.scheduler.types.AllocatableAction;

import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.util.ResourceScheduler;
import java.util.LinkedList;

public class OptimizationAction extends AllocatableAction {

	public OptimizationAction() {
		super(new AsceticSchedulingInformation());
		AsceticSchedulingInformation asi = (AsceticSchedulingInformation) this.getSchedulingInfo();
		asi.scheduled();
		asi.setOnOptimization(true);
		asi.setExpectedStart(0);
		asi.setExpectedEnd(Long.MAX_VALUE);
	}

	@Override
	protected void doAction() {

	}

	@Override
	protected void doCompleted() {

	}

	@Override
	protected void doError() throws FailedActionException {

	}

	@Override
	protected void doFailed() {

	}

	@Override
	public Integer getCoreId() {
		return null;
	}

	@Override
	public LinkedList getCompatibleWorkers() {
		return null;
	}

	@Override
	public Implementation[] getImplementations() {
		return null;
	}

	@Override
	public boolean isCompatible(Worker r) {
		return true;
	}

	@Override
	public LinkedList getCompatibleImplementations(ResourceScheduler r) {
		return null;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public Score schedulingScore(ResourceScheduler targetWorker, Score actionScore) {
		return null;
	}

	@Override
	public void schedule(Score actionScore) throws BlockedActionException, UnassignedActionException {

	}

	@Override
	public void schedule(ResourceScheduler targetWorker, Score actionScore) throws BlockedActionException, UnassignedActionException {

	}

	@Override
	public void schedule(ResourceScheduler targetWorker, Implementation impl) throws BlockedActionException, UnassignedActionException {

	}

	@Override
	public boolean isToReserveResources() {
		return true;
	}

	@Override
	public boolean isToReleaseResources() {
		return true;
	}

	public String toString() {
		return "Scheduling blocking action";
	}
}
