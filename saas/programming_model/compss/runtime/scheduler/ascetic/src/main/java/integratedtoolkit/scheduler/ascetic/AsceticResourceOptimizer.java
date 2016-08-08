package integratedtoolkit.scheduler.ascetic;

import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.types.WorkloadState;
import integratedtoolkit.util.ResourceOptimizer;
import integratedtoolkit.util.ResourceScheduler;

public class AsceticResourceOptimizer extends ResourceOptimizer {

    public AsceticResourceOptimizer(AsceticScheduler ts) {
        super(ts);
    }

    @Override
    protected void initialCreations() {
        try {
            Thread.sleep(20_000l);
        } catch (Exception e) {
        }
    }

    @Override
    protected void applyPolicies(WorkloadState workload) {
        AsceticWorkloadState awl = (AsceticWorkloadState) workload;
        long elapsedTime = Ascetic.getAccumulatedTime();
        double elapsedEnergy = Ascetic.getAccumulatedEnergy();
        double elapsedCost = Ascetic.getAccumulatedEnergy();

        long timeBoundary = Ascetic.getTimeBoundary();
        double energyBoundary = Ascetic.getEnergyBoundary();
        double costBoundary = Ascetic.getEconomicalBoundary();

        addToLog("Boundaries\n"
                + "\tTime: " + timeBoundary + "s\n"
                + "\tEnergy: " + energyBoundary + "Wh\n"
                + "\tCost: " + costBoundary + "€\n");

        addToLog("Elapsed\n"
                + "\tTime: " + elapsedTime + "s\n"
                + "\tEnergy: " + elapsedEnergy + "Wh\n"
                + "\tCost: " + elapsedCost + "€\n");

        long timeBudget = timeBoundary - elapsedTime;
        double energyBudget = energyBoundary - elapsedEnergy;
        double costBudget = costBoundary - elapsedCost;

        addToLog("Budget\n"
                + "\tTime: " + timeBudget + "s\n"
                + "\tEnergy: " + energyBudget + "Wh - " + (energyBudget * 3600) + "J\n"
                + "\tCost: " + costBudget + "€\n");

        long time = 0;
        double actionsCost = 0;
        double idlePrice = 0;
        double actionsEnergy = 0;
        double idlePower = 0;
        ResourceScheduler[] workers = ts.getWorkers();
        addToLog("Current Resources\n");
        for (ResourceScheduler w : workers) {
            AsceticResourceScheduler aw = (AsceticResourceScheduler) w;
            addToLog("\tName:" + aw.getName() + "\n");

            time = Math.max(time, aw.getLastGapExpectedStart());
            addToLog("\t\tTime:" + aw.getLastGapExpectedStart() + " ms -> total " + time + "\n");

            actionsCost += aw.getActionsCost();
            addToLog("\t\tactions Cost:" + aw.getActionsCost() + " € -> total " + actionsCost + "€\n");

            idlePrice += aw.getIdlePrice();
            addToLog("\t\tIdle Price:" + aw.getIdlePrice() + " € -> total " + idlePrice + "€\n");

            actionsEnergy += aw.getActionsEnergy();
            addToLog("\t\tactions Energy:" + aw.getActionsEnergy() + " mJ -> total " + actionsEnergy + "mJ\n");

            idlePower += aw.getIdlePower();
            addToLog("\t\tIdle Power:" + aw.getIdlePower() + " W -> total " + idlePower + "W\n");
        }

        Action current = new Action(new ConfigurationCost(time, idlePower, actionsEnergy, idlePrice, actionsCost));
        addToLog(current.toString());

        for (String componentName : Ascetic.getComponentNames()) {
            if (Ascetic.canReplicateComponent(componentName)) {
                double extraIdlePower = Ascetic.getPower(componentName);
                double extraIdlePrice = Ascetic.getPrice(componentName);
                ConfigurationCost cc = new ConfigurationCost(time, idlePower + extraIdlePower, actionsEnergy, idlePrice + extraIdlePrice, actionsCost);
                Action action = new Action.Add(componentName, cc);
                addToLog(action.toString());
            }
        }

        for (ResourceScheduler w : workers) {
            if (Ascetic.canTerminateVM(w.getResource())) {
                AsceticResourceScheduler ars = (AsceticResourceScheduler) w;
                double extraIdlePower = ars.getIdlePower();
                double extraIdlePrice = ars.getIdlePrice();
                ConfigurationCost cc = new ConfigurationCost(time, idlePower - extraIdlePower, actionsEnergy, idlePrice - extraIdlePrice, actionsCost);
                Action action = new Action.Remove(w, cc);
                addToLog(action.toString());
            }
        }

        printLog();
    }

    StringBuilder log = new StringBuilder(
            "-------------------------\n"
            + "    CHECK SCALABILITY    \n"
            + "-------------------------\n");

    private void addToLog(String s) {
        log.append(s);
    }

    private void printLog() {
        System.out.println(log.toString() + "\n"
                + "-----------------------------\n");
        log = new StringBuilder(
                "-------------------------\n"
                + "    CHECK SCALABILITY    \n"
                + "-------------------------\n");
    }

    private static class Action {

        public enum Movement {

            NONE,
            REPLICATE,
            TERMINATE
        }

        ConfigurationCost cost;

        public Action(ConfigurationCost cost) {
            this.cost = cost;
        }

        public String toString() {
            return cost.toString();
        }

        protected Movement getType() {
            return Movement.NONE;
        }

        
        public static class Add extends Action {

            String component;

            public Add(String component, ConfigurationCost cost) {
                super(cost);
                this.component = component;
            }

            public Movement getType() {
                return Movement.REPLICATE;
            }

            public String toString() {
                return "Add " + component + "\n" + super.toString();
            }
        }

        public static class Remove extends Action {

            ResourceScheduler ars;

            public Remove(ResourceScheduler ars, ConfigurationCost cost) {
                super(cost);
                this.ars = ars;
            }

            public Movement getType() {
                return Movement.TERMINATE;
            }

            public String toString() {
                return "Remove " + ars.getName() + "\n" + super.toString();
            }
        }
    }

    private static class ConfigurationCost {

        long time;
        double energy;
        double cost;

        ConfigurationCost(long time, double idlePower, double actionsEnergy, double idlePrice, double actionsCost) {
            this.time = time / 1000;
            this.energy = (idlePower * time + actionsEnergy) / 3_600_000;
            this.cost = idlePrice + actionsCost;
        }

        @Override
        public String toString() {
            return "\tTime: " + time + "s\n"
                    + "\tEnergy: " + energy + "Wh\n"
                    + "\tCost: " + cost + "€\n";
        }
    }
}
