package integratedtoolkit.scheduler.ascetic;

import integratedtoolkit.scheduler.exceptions.BlockedActionException;
import integratedtoolkit.scheduler.exceptions.InvalidSchedulingException;
import integratedtoolkit.scheduler.exceptions.UnassignedActionException;
import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.types.AsceticScore;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.OptimizationWorker;
import integratedtoolkit.types.Profile;
import integratedtoolkit.types.Score;
import integratedtoolkit.util.ResourceScheduler;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

public class ScheduleOptimizer extends Thread {

    private static long OPTIMIZATION_THRESHOLD = 5_000;
    private AsceticScheduler<?, ?> scheduler;
    private boolean stop = false;
    private Semaphore sem = new Semaphore(0);

    public ScheduleOptimizer(AsceticScheduler<?, ?> scheduler) {
        this.setName("ScheduleOptimizer");
        this.scheduler = scheduler;
    }

    public void run() {
        long lastUpdate = System.currentTimeMillis();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            //Do nothing
        }
        while (!stop) {
            long optimizationTS = System.currentTimeMillis();
            ResourceScheduler<?, ?>[] workers = scheduler.getWorkers();
            globalOptimization(optimizationTS, workers);
            lastUpdate = optimizationTS;
            waitForNextIteration(lastUpdate);
        }
        sem.release();
    }

    public void shutdown() throws InterruptedException {
        stop = true;
        this.interrupt();
        sem.acquire();
    }

    private void waitForNextIteration(long lastUpdate) {
        long difference = OPTIMIZATION_THRESHOLD - (System.currentTimeMillis() - lastUpdate);
        if (difference > 0) {
            try {
                Thread.sleep(difference);
            } catch (InterruptedException ie) {
                //Do nothing. Wake up in case of shutdown received
            }
        }
    }

    /*--------------------------------------------------
     ---------------------------------------------------
     --------------- Local  optimization ---------------
     ---------------------------------------------------
     --------------------------------------------------*/
    public void globalOptimization(long optimizationTS,
            ResourceScheduler<?, ?>[] workers
    ) {
        int workersCount = workers.length;
        if (workersCount == 0) {
            return;
        }
        OptimizationWorker[] optimizedWorkers = new OptimizationWorker[workersCount];
        LinkedList<OptimizationWorker> receivers = new LinkedList();

        for (int i = 0; i < workersCount; i++) {
            optimizedWorkers[i] = new OptimizationWorker((AsceticResourceScheduler) workers[i]);
        }

        boolean hasDonated = true;
        while (hasDonated) {
            optimizationTS = System.currentTimeMillis();
            hasDonated = false;
            System.out.println("-----------------------------------------");
            //Perform local optimizations
            for (int i = 0; i < workersCount; i++) {
                optimizedWorkers[i].localOptimization(optimizationTS);
                System.out.println(optimizedWorkers[i].getName() + " will end at " + optimizedWorkers[i].getDonationIndicator());
            }

            LinkedList<OptimizationWorker> donors = determineDonorAndReceivers(optimizedWorkers, receivers);

            while (!hasDonated && !donors.isEmpty()) {
                OptimizationWorker donor = donors.remove();
                AllocatableAction candidate = donor.pollDonorAction();
                if (candidate == null) {
                    break;
                }
                Iterator<OptimizationWorker> recIt = receivers.iterator();
                while (recIt.hasNext()) {
                    OptimizationWorker receiver = recIt.next();
                    if (move(candidate, donor, receiver)) {
                        hasDonated = true;
                        break;
                    }
                }
            }
            System.out.println("-----------------------------------------");
        }
    }

    public static LinkedList<OptimizationWorker> determineDonorAndReceivers(
            OptimizationWorker[] workers,
            LinkedList<OptimizationWorker> receivers
    ) {
        receivers.clear();
        PriorityQueue<OptimizationWorker> receiversPQ = new PriorityQueue<OptimizationWorker>(1, getReceptionComparator());
        long topIndicator = Long.MIN_VALUE;
        LinkedList<OptimizationWorker> top = new LinkedList();

        for (OptimizationWorker ow : workers) {
            long indicator = ow.getDonationIndicator();
            if (topIndicator > indicator) {
                receiversPQ.add(ow);
            } else {
                if (indicator > topIndicator) {
                    topIndicator = indicator;
                    for (OptimizationWorker extop : top) {
                        receiversPQ.add(extop);
                    }
                    top.clear();
                }
                top.add(ow);
            }
        }
        OptimizationWorker ow;
        while ((ow = receiversPQ.poll()) != null) {
            receivers.add(ow);
        }
        return top;
    }

    /*--------------------------------------------------
     ---------------------------------------------------
     ----------- Comparators  optimization -------------
     ---------------------------------------------------
     --------------------------------------------------*/
    public static Comparator<AllocatableAction> getSelectionComparator() {
        return new Comparator<AllocatableAction>() {
            @Override
            public int compare(AllocatableAction action1, AllocatableAction action2) {
                int priority = Integer.compare(action1.getPriority(), action2.getPriority());
                if (priority == 0) {
                    return Long.compare(action1.getId(), action2.getId());
                } else {
                    return -priority;
                }
            }
        };
    }

    public static Comparator<AllocatableAction> getDonationComparator() {
        return new Comparator<AllocatableAction>() {
            @Override
            public int compare(AllocatableAction action1, AllocatableAction action2) {
                AsceticSchedulingInformation action1DSI = (AsceticSchedulingInformation) action1.getSchedulingInfo();
                AsceticSchedulingInformation action2DSI = (AsceticSchedulingInformation) action2.getSchedulingInfo();
                int priority = Long.compare(action2DSI.getExpectedEnd(), action1DSI.getExpectedEnd());
                if (priority == 0) {
                    return Long.compare(action1.getId(), action2.getId());
                } else {
                    return priority;
                }
            }
        };
    }

    public static final Comparator<OptimizationWorker> getReceptionComparator() {
        return new Comparator<OptimizationWorker>() {
            @Override
            public int compare(OptimizationWorker worker1, OptimizationWorker worker2) {
                return Long.compare(worker1.getDonationIndicator(), worker2.getDonationIndicator());
            }
        };
    }

    private boolean move(AllocatableAction action, OptimizationWorker donor, OptimizationWorker receiver) {
        LinkedList<AllocatableAction> dataPreds = action.getDataPredecessors();
        long dataAvailable = 0;
        try {
            for (AllocatableAction dataPred : dataPreds) {
                AsceticSchedulingInformation dsi = (AsceticSchedulingInformation) dataPred.getSchedulingInfo();
                dataAvailable = Math.max(dataAvailable, dsi.getExpectedEnd());
            }
        } catch (ConcurrentModificationException cme) {
            dataAvailable = 0;
            dataPreds = action.getDataPredecessors();
        }

        Implementation bestImpl = null;
        long bestTime = Long.MAX_VALUE;

        LinkedList<Implementation> impls = action.getCompatibleImplementations(receiver.getResource());
        for (Implementation impl : impls) {
            Profile p = receiver.getResource().getProfile(impl);
            long avgTime = p.getAverageExecutionTime();
            if (avgTime < bestTime) {
                bestTime = avgTime;
                bestImpl = impl;
            }
        }

        AsceticSchedulingInformation dsi = (AsceticSchedulingInformation) action.getSchedulingInfo();
        long currentEnd = dsi.getExpectedEnd();

        if (bestImpl != null && currentEnd > receiver.getResource().getFirstGapExpectedStart() + bestTime) {
            System.out.println("Moving " + action + " from " + donor.getName() + " to " + receiver.getName());
            unschedule(action);
            schedule(action, bestImpl, receiver);
            return true;
        }
        return false;
    }

    private AsceticScore dummyScore = new AsceticScore(0, 0, 0, 0, 0, 0);

    public void schedule(AllocatableAction action, Implementation impl, OptimizationWorker ow) {
        try {
            action.schedule(ow.getResource(), impl);
            action.tryToLaunch();
        } catch (InvalidSchedulingException ise) {
            try {
                long actionScore = AsceticScore.getActionScore(action);
                long dataTime = dummyScore.getDataPredecessorTime(action.getDataPredecessors());
                Score aScore = new AsceticScore(actionScore, dataTime, 0, 0, 0, 0);
                action.schedule(action.getConstrainingPredecessor().getAssignedResource(), aScore);
                try {
                    action.tryToLaunch();
                } catch (InvalidSchedulingException ise2) {
                    ise2.printStackTrace();
                    //Impossible exception. 
                }
            } catch (BlockedActionException | UnassignedActionException be) {
                //Can not happen since there was an original source
                be.printStackTrace();

            }
        } catch (BlockedActionException | UnassignedActionException be) {
            //Can not happen since there was an original source
            be.printStackTrace();
        }
    }

    public void unschedule(AllocatableAction action) {
        AsceticResourceScheduler resource = (AsceticResourceScheduler) action.getAssignedResource();
        resource.unscheduleAction(action);
    }
}
