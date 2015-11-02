package es.bsc.vmmanagercore.models.scheduling;

/**
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public abstract class LocalSearchAlgorithm {

    protected final String name;

    public LocalSearchAlgorithm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}