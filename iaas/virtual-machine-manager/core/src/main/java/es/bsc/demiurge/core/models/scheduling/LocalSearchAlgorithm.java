package es.bsc.demiurge.core.models.scheduling;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
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