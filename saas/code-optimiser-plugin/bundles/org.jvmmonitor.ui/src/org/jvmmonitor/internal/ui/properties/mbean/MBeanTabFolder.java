/*******************************************************************************
 * Copyright (c) 2010-2011 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.properties.mbean;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.PageBook;
import org.jvmmonitor.internal.ui.properties.AbstractJvmPropertySection;

/**
 * The MBean tab folder.
 */
public class MBeanTabFolder extends PageBook {

    /** The attributes tab. */
    private AttributesTab attributeTab;

    /** The operations tab. */
    private OperationsTab operationsTab;

    /** The notification tab. */
    private NotificationsTab notificationTab;

    /** The message label. */
    private Label message;

    /** The tab folder. */
    private CTabFolder folder;

    /**
     * The constructor.
     * 
     * @param sashForm
     *            The sash form
     * @param propertySection
     *            The property section
     */
    public MBeanTabFolder(SashForm sashForm,
            AbstractJvmPropertySection propertySection) {
        super(sashForm, SWT.NONE);

        folder = new CTabFolder(this, SWT.BOTTOM);
        attributeTab = new AttributesTab(folder, propertySection);
        operationsTab = new OperationsTab(folder, propertySection);
        notificationTab = new NotificationsTab(folder, propertySection);

        message = new Label(this, SWT.NONE);
        message.setText(Messages.mBeanNotSelectedMessage);
        message.setBackground(Display.getDefault().getSystemColor(
                SWT.COLOR_LIST_BACKGROUND));

        showPage(message);
    }

    /**
     * Notifies that selection has been changed.
     * 
     * @param selection
     *            The selection
     */
    public void selectionChanged(StructuredSelection selection) {
        if (isFolderSelected(selection)) {
            showPage(message);
        } else {
            showPage(folder);
            attributeTab.selectionChanged(selection);
            operationsTab.selectionChanged(selection);
            notificationTab.selectionChanged(selection);
        }
    }

    /**
     * Refreshes.
     */
    protected void refresh() {
        attributeTab.refresh();
        operationsTab.refresh();
        notificationTab.refresh();
    }

    /**
     * Invoked when section is deactivated.
     */
    protected void deactivated() {
        attributeTab.deactivated();
        operationsTab.deactivated();
        notificationTab.deactivated();
    }

    /**
     * Gets the state indicating if the given selection is a folder.
     * 
     * @param selection
     *            The selection
     * @return True if the given selection is a folder
     */
    private static boolean isFolderSelected(ISelection selection) {
        Object firstElement = ((StructuredSelection) selection)
                .getFirstElement();
        return firstElement instanceof MBeanDomain
                || firstElement instanceof MBeanFolder;
    }
}
