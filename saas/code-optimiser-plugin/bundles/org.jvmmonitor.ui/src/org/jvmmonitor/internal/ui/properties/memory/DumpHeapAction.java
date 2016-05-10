/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.properties.memory;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.jvmmonitor.core.IActiveJvm;
import org.jvmmonitor.core.JvmCoreException;
import org.jvmmonitor.internal.ui.properties.AbstractJvmPropertySection;
import org.jvmmonitor.internal.ui.views.OpenSnapshotAction;
import org.jvmmonitor.ui.Activator;
import org.jvmmonitor.ui.ISharedImages;

/**
 * The action to dump heap.
 */
public class DumpHeapAction extends Action {

    /** The property section. */
    AbstractJvmPropertySection section;

    /**
     * The constructor.
     * 
     * @param section
     *            The property section
     */
    public DumpHeapAction(AbstractJvmPropertySection section) {
        setText(Messages.dumpHeapLabel);
        setImageDescriptor(Activator
                .getImageDescriptor(ISharedImages.TAKE_HEAP_DUMP_IMG_PATH));
        setDisabledImageDescriptor(Activator
                .getImageDescriptor(ISharedImages.DISABLED_TAKE_HEAP_DUMP_IMG_PATH));
        setId(getClass().getName());

        this.section = section;
    }

    /*
     * @see Action#run()
     */
    @Override
    public void run() {
        new Job(Messages.dumpHeapDataJobLabel) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                IActiveJvm jvm = section.getJvm();
                if (jvm == null) {
                    return Status.CANCEL_STATUS;
                }

                IFileStore fileStore = null;
                try {
                    fileStore = jvm.getMBeanServer().dumpHeap();
                } catch (JvmCoreException e) {
                    Activator.log(Messages.dumpHeapDataFailedMsg, e);
                    return Status.CANCEL_STATUS;
                }

                section.setPinned(true);

                OpenSnapshotAction.openEditor(fileStore);
                return Status.OK_STATUS;
            }
        }.schedule();
    }
}
