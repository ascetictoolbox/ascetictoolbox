package eu.ascetic.experimentmanager.plugin.launchers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;

public class SourcePathComputer implements ISourcePathComputer {

	@Override
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration arg0, IProgressMonitor arg1)
			throws CoreException {
		return new ISourceContainer[3];
	}

	@Override
	public String getId() {
		return "experimentmanager.plugin.launchers.SourcePathComputer";
	}

}
