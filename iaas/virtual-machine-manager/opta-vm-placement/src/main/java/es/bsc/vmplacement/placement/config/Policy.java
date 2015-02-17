package es.bsc.vmplacement.placement.config;

/**
 * Enumeration of the policies supported by this library.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public enum Policy {

    CONSOLIDATION("Consolidation"), 
    DISTRIBUTION("Distribution"), 
    PRICE("Price"), 
    ENERGY("Energy"),
    GROUP_BY_APP("Group by App"), 
    RANDOM("Random");

    private final String name;

    private Policy(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
    
}