package integratedtoolkit.api;


public class COMPSs {
	
	private static final String SKIP_MESSAGE = "COMPSs Runtime is not loaded. Skipping call";
	
	/**
	 * Barrier
	 * 
	 */
	public static void waitForAllTasks() {
		// This is only a handler, it is never executed
		System.out.println(SKIP_MESSAGE);
	}

}
