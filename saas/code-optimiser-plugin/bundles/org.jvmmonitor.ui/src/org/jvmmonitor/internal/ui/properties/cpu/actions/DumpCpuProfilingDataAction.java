/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.properties.cpu.actions;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jvmmonitor.core.IActiveJvm;
import org.jvmmonitor.core.JvmCoreException;
import org.jvmmonitor.internal.ui.properties.AbstractJvmPropertySection;
import org.jvmmonitor.internal.ui.views.OpenSnapshotAction;
import org.jvmmonitor.ui.Activator;
import org.jvmmonitor.ui.ISharedImages;

/**
 * Dumps the CPU profiling data.
 */
public class DumpCpuProfilingDataAction extends AbstractCpuProfilingAction {

    /**
     * The constructor.
     * 
     * @param section
     *            The property section
     */
    public DumpCpuProfilingDataAction(AbstractJvmPropertySection section) {
        super(section);

        setText(Messages.dumpCpuLabel);
        setImageDescriptor(Activator
                .getImageDescriptor(ISharedImages.TAKE_CPU_DUMP_IMG_PATH));
        setDisabledImageDescriptor(Activator
                .getImageDescriptor(ISharedImages.DISABLED_TAKE_CPU_DUMP_IMG_PATH));
        setId(getClass().getName());
    }

    /*
     * @see AbstractJobAction#performRun(IProgressMonitor)
     */
    @Override
    protected IStatus performRun(IProgressMonitor monitor) {
        IActiveJvm jvm = section.getJvm();
        if (jvm == null) {
            return Status.CANCEL_STATUS;
        }

        IFileStore fileStore = null;
        try {
            fileStore = jvm.getCpuProfiler().dump();
        } catch (JvmCoreException e) {
            Activator.log(Messages.dumpCpuProfileDataFailedMsg, e);
            return Status.CANCEL_STATUS;
        }

        section.setPinned(true);

        OpenSnapshotAction.openEditor(fileStore);
        return Status.OK_STATUS;
    }

    /*
     * @see AbstractJobAction#getJobName()
     */
    @Override
    protected String getJobName() {
        return Messages.dumpCpuProfileDataJobLabel;
    }
}
