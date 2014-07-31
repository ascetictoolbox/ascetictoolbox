package eu.ascetic.iaas.slamanager.main.beans.client;

public interface Params
{
    public interface VMCharacteristic
    {
        public static final String VMTYPES = "VMTYPES";

        public static final String VM_DEVELOPERS     = "VM_DEVELOPERS";
        public static final String VM_DEVELOPERS_LOW = "VM_DEVELOPERS_LOW";
        public static final String VM_OFFICE         = "VM_OFFICE";
        public static final String VM_DESIGN         = "VM_DESIGN";
        public static final String VM_RESEARCH       = "VM_RESEARCH";
        
        public static final String[] VMTYPES_SET = { VM_DEVELOPERS, VM_DEVELOPERS_LOW, VM_OFFICE, VM_DESIGN, VM_RESEARCH };
        
        public static final String VM_QUANTITY_VAR      = "VM_QUANTITY_VAR";
        public static final String VM_CORES_VAR         = "VM_CORES_VAR";
        public static final String VM_CPU_SPEED_VAR     = "VM_CPU_SPEED_VAR";
        public static final String VM_MEMORY_SIZE_VAR   = "VM_MEMORY_SIZE_VAR";
        public static final String VM_HARDDISK_SIZE_VAR = "VM_HARDDISK_SIZE_VAR";
        public static final String START_TIME_VAR       = "START_TIME_VAR";
        public static final String END_TIME_VAR         = "END_TIME_VAR";
        public static final String VM_ISOLATION_VAR     = "VM_ISOLATION_VAR";
        public static final String VM_PERSISTENCE_VAR   = "VM_PERSISTENCE_VAR";
        public static final String VM_IMAGE_VAR         = "VM_IMAGE_VAR";
    }
}
