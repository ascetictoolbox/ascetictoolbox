package integratedtoolkit.types.request.td;

import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.types.Profile;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.allocatableactions.SingleExecution;
import integratedtoolkit.types.request.exceptions.ShutdownException;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.types.resources.WorkerResourceDescription;
import integratedtoolkit.util.ResourceManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;


/**
 * The DeleteIntermediateFilesRequest represents a request to delete the
 * intermediate files of the execution from all the worker nodes of the resource
 * pool.
 */
public class PrintCurrentGraphRequest<P extends Profile, T extends WorkerResourceDescription> extends TDRequest<P,T> {
	
	private static final String ERROR_PRINT_CURRENT_GRAPH = "ERROR: Cannot print current graph state";
	protected static final Logger logger = Logger.getLogger(Loggers.TD_COMP);

    /**
     * Semaphore to synchronize until the representation is constructed
     */
    private Semaphore sem;
    
    /**
     * BufferedWriter describing the graph file where to write the information
     */
    private BufferedWriter graph;
    
    /**
     * Constructs a GetCurrentScheduleRequest
     *
     * @param sem Semaphore to synchronize until the representation is
     * constructed
     *
     */
    public PrintCurrentGraphRequest(Semaphore sem, BufferedWriter graph) {
        this.sem = sem;
        this.graph = graph;
    }

    /**
     * Returns the semaphore to synchronize until the representation is
     * constructed
     *
     * @result Semaphore to synchronize until the representation is constructed
     *
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /**
     * Changes the semaphore to synchronize until the representation is
     * constructed
     *
     * @param sem New semaphore to synchronize until the representation is
     * constructed
     *
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }

    @Override
    public void process(TaskScheduler<P,T> ts) throws ShutdownException {
    	try {
    		PriorityQueue<Task> pending = new PriorityQueue<Task>();
    		HashSet<Task> tasks = new HashSet<Task>();
    		String prefix = "  ";
    		
    		// Header options
    		graph.write(prefix + "outputorder=\"edgesfirst\";");
    		graph.newLine();
    		graph.write(prefix + "compound=true;");
    		graph.newLine();
    		
    		/* Subgraph for blocked and scheduled tasks *********************/
    		graph.write(prefix + "subgraph cluster1 {");
    		graph.newLine();
    		graph.write(prefix + prefix + "color=white");
            graph.newLine();
    		
    		// Room for Blocked tasks
    		int roomIndex = 1;
    		graph.write(prefix + prefix + "subgraph cluster_room" + roomIndex + " {");
    		++roomIndex;
    		graph.newLine();
    		graph.write(prefix + prefix + prefix + "ranksep=0.20;");
            graph.newLine();
            graph.write(prefix + prefix + prefix + "node[height=0.75];");
            graph.newLine();
            graph.write(prefix + prefix + prefix + "label = \"Blocked\"");
            graph.newLine();
            graph.write(prefix + prefix + prefix + "color=red");
            graph.newLine();
	    	LinkedList<AllocatableAction<P,T>> blockedActions = ts.getBlockedActions();
	    	for (AllocatableAction<P,T> action : blockedActions) {
	    		if (action instanceof SingleExecution) {
	    			SingleExecution<P,T> se = (SingleExecution<P,T>) action;
	    			Task t = se.getTask();
	    			graph.write(prefix + prefix + prefix + t.getDotDescription());
	    			graph.newLine();

	    			pending.addAll(t.getSuccessors());
	    			tasks.add(t);
	    		}
	    	}
    		graph.write(prefix + prefix + "}");
    		graph.newLine();
    		
    		// Add another room for each worker
    		for (Worker<?> r : ResourceManager.getAllWorkers()) {
	    		Worker<T> worker = (Worker<T>) r;
	    		graph.write(prefix + prefix + "subgraph cluster_room" + roomIndex + " {");
	    		graph.newLine();
	    		graph.write(prefix + prefix + prefix + "label = \"" + worker.getName() + "\"");
	            graph.newLine();
	            graph.write(prefix + prefix + prefix + "color=black");
	            graph.newLine();
	            
	            // Create a box for running tasks
	            graph.write(prefix + prefix + prefix + "subgraph cluster_box" + roomIndex + "1 {");
	            graph.newLine();
	            graph.write(prefix + prefix + prefix + prefix + "label = \"Running\"");
	            graph.newLine();
	            graph.write(prefix + prefix + prefix + prefix + "ranksep=0.20;");
	            graph.newLine();
	            graph.write(prefix + prefix + prefix + prefix + "node[height=0.75];");
	            graph.newLine();
	            graph.write(prefix + prefix + prefix + prefix + "color=green");
	            graph.newLine();
	            LinkedList<AllocatableAction<P,T>> hostedActions = ts.getHostedActions(worker);
	    		for (AllocatableAction<P,T> action : hostedActions) {
	        		if (action instanceof SingleExecution) {
	        			SingleExecution<P,T> se = (SingleExecution<P,T>) action;
	        			Task t = se.getTask();
	        			graph.write(prefix + prefix + prefix + prefix + t.getDotDescription());
	        			graph.newLine();
	        			
	        			pending.addAll(t.getSuccessors());
		    			tasks.add(t);
	        		}
	        	}
	    		graph.write(prefix + prefix + prefix + "}");
		        graph.newLine();
	    		
		        graph.write(prefix + prefix + prefix + "subgraph cluster_box" + roomIndex + "2 {");
	            graph.newLine();
	            graph.write(prefix + prefix + prefix + prefix + "label = \"Resource Blocked\"");
	            graph.newLine();
	            graph.write(prefix + prefix + prefix + prefix + "ranksep=0.20;");
	            graph.newLine();
	            graph.write(prefix + prefix + prefix + prefix + "node[height=0.75];");
	            graph.newLine();
	            graph.write(prefix + prefix + prefix + prefix + "color=red");
	            graph.newLine();
	    		LinkedList<AllocatableAction<P,T>> blockedActionsOnResource = ts.getBlockedActionsOnResource(worker);
	    		for (AllocatableAction<P,T> action : blockedActionsOnResource) {
	        		if (action instanceof SingleExecution) {
	        			SingleExecution<P,T> se = (SingleExecution<P,T>) action;
	        			Task t = se.getTask();
	        			graph.write(prefix + prefix + prefix + prefix + t.getDotDescription());
	        			graph.newLine();
	        			
	        			pending.addAll(t.getSuccessors());
		    			tasks.add(t);
	        		}
	        	}
	    		// Close box
	    		graph.write(prefix + prefix + prefix + "}");
		        graph.newLine();
		        
		        // Close room
		        graph.write(prefix + prefix + "}");
		        graph.newLine();
		        ++roomIndex;
	    	}
    		
    		// Close cluster
    		graph.write(prefix + "}");
    		graph.newLine();
    		
    		/* Subgraph for pending tasks ***********************************/
    		graph.write(prefix + "subgraph cluster2 {");
    		graph.newLine();
    		graph.write(prefix + prefix + "label = \"Pending\"");
            graph.newLine();
            graph.write(prefix + prefix + "ranksep=0.20;");
            graph.newLine();
            graph.write(prefix + prefix + "node[height=0.75];");
            graph.newLine();
            graph.write(prefix + prefix + "color=blue");
            graph.newLine();
            
            while (!pending.isEmpty()) {
            	Task t = pending.poll();
            	if (!tasks.contains(t)) {
	            	graph.write(prefix + prefix + t.getDotDescription());
	            	graph.newLine();
	    			tasks.add(t);
	    			pending.addAll(t.getSuccessors());
            	}
            }
    		
            // Close cluster
    		graph.write(prefix + "}");
    		graph.newLine();
    		
    		/* Write edges ****************************************************/
    		for (Task t : tasks) {
    			HashSet<Task> successors = new HashSet<Task>();
    			successors.addAll(t.getSuccessors());
				for (Task t2 : successors) {
					graph.write(prefix + t.getId() + " -> " + t2.getId() + ";");
					graph.newLine();
				}
    		}

    		/* Force flush before end ******************************************/
	    	graph.flush();
    	} catch (IOException e) {
    		logger.error(ERROR_PRINT_CURRENT_GRAPH);
    	}

        sem.release();
    }

    @Override
    public TDRequestType getType(){
        return TDRequestType.PRINT_CURRENT_GRAPH;
    }

}
