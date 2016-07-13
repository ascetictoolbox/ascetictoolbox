package integratedtoolkit.nio.worker.executors;


import integratedtoolkit.nio.NIOTask;
import integratedtoolkit.nio.worker.NIOWorker;
import integratedtoolkit.nio.worker.util.JobsThreadPool;
import integratedtoolkit.nio.worker.util.TaskResultReader;
import integratedtoolkit.util.RequestQueue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class PythonExecutor extends ExternalExecutor {
	
	public static final String PYCOMPSS_RELATIVE_PATH = File.separator + "Bindings" + File.separator + "python";

	//private static final String WORKER_TRACING_CONFIG_FILE_RELATIVE_PATH = File.separator + "Runtime" + File.separator + "configuration" +
    //        File.separator + "xml" + File.separator + "tracing";
	
	
	public PythonExecutor(NIOWorker nw, JobsThreadPool pool, RequestQueue<NIOTask> queue,
			String writePipe, TaskResultReader resultReader) {
		
		super(nw, pool, queue, writePipe, resultReader);
	}
	
	@Override
	public ArrayList<String> getTaskExecutionCommand(NIOWorker nw, NIOTask nt, String sandBox) {
		// The execution command in python its empty (the handler adds the pre-command and the application args)
		ArrayList<String> lArgs = new ArrayList<String>();		
		return lArgs;
	}

	public static Map<String, String> getEnvironment(NIOWorker nw) {
		// PyCOMPSs HOME
		Map<String, String> env = new HashMap<String, String>();
		String pycompssHome = nw.getInstallDir() + PYCOMPSS_RELATIVE_PATH;
		env.put("PYCOMPSS_HOME", pycompssHome);
		
		// PYTHONPATH
		String pythonPath = System.getenv("PYTHONPATH");
		if (pythonPath == null) {
			pythonPath = pycompssHome + ":" + nw.getPythonpath() + ":" + nw.getAppDir();
		} else {
			pythonPath = pycompssHome + ":" + nw.getPythonpath() + ":" + nw.getAppDir() + pythonPath;
		}

		env.put("PYTHONPATH", pythonPath);
		
		// LD_LIBRARY_PATH
		String ldLibraryPath = System.getenv("LD_LIBRARY_PATH");
		if (ldLibraryPath == null) {
			ldLibraryPath = nw.getLibPath();
		} else {
			ldLibraryPath = ldLibraryPath.concat(":" + nw.getLibPath());
		}
		env.put("LD_LIBRARY_PATH", ldLibraryPath);
               
		return env;
	}

}
