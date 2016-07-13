package integratedtoolkit.types.resources;


public abstract class WorkerResourceDescription extends ResourceDescription {
	
    // Unassigned values 
    // !!!!!!!!!! WARNING: Coherent with constraints class
	public static final int UNASSIGNED_INT 		= -1;
    public static final String UNASSIGNED_STR 	= "[unassigned]";
    public static final float UNASSIGNED_FLOAT 	= (float) -1.0;
    
    public static final int ZERO_INT			= 0;
    public static final int ONE_INT				= 1;

	
    public WorkerResourceDescription() {
        super();
    }

    public WorkerResourceDescription(WorkerResourceDescription desc) {
        super(desc);
    }
    
}
