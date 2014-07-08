package integratedtoolkit.types.request.td;

import java.util.concurrent.Semaphore;

import integratedtoolkit.types.request.td.TDRequest;

public class UpdateLocalCEIRequest extends TDRequest {
	
	private Class<?> ceiClass;
	private Semaphore sem;
	public UpdateLocalCEIRequest() {
		super(TDRequestType.UPDATE_LOCAL_CEI);
		
	}
	
	public UpdateLocalCEIRequest(Class<?> ceiClass, Semaphore sem) {
		super(TDRequestType.UPDATE_LOCAL_CEI);
		this.ceiClass = ceiClass;
		this.sem = sem;
	}
	
	/**
     * Returns the semaphore where to synchronize until the operation is done
     * @return Semaphore where to synchronize until the operation is done
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /**
     * Sets the semaphore where to synchronize until the operation is done
     * @param sem Semaphore where to synchronize until the operation is done
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }
    
    public void setCeiClass(Class<?> ceiClass){
    	this.ceiClass = ceiClass;
    }
    
    public Class<?> getCeiClass(){
    	return this.ceiClass;
    }

}
