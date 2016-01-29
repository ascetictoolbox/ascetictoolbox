package es.bsc.vmmanagercore.manager;

public enum DeploymentEngine {
    LEGACY("legacy"), OPTAPLANNER("optaPlanner");
    private String name;

    private DeploymentEngine(String name) {
        this.name = name;
    }

    public static DeploymentEngine fromName(String name) {
        return DeploymentEngine.valueOf(name.toUpperCase());
    }

    @Override
    public String toString() {
        return name;
    }
}
