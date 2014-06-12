package datastructure;

import utils.Constant;


/**
 * <code>Memory</code> represents Memory in resource request.
 * 
 */
public class Memory extends Resource {

    public Memory() {
        this.setResourceName(Constant.Memory);
    }

    /**
     * memory_redundancy
     */
    private boolean memory_redundancy;

    /**
     * To evaluate whether memory_redundancy is selected or not.
     */
    public boolean isMemory_redundancy() {
        return memory_redundancy;
    }

    /**
     * To set memory_redundancy.
     */
    public void setMemory_redundancy(boolean memoryRedundancy) {
        memory_redundancy = memoryRedundancy;
    }
}
