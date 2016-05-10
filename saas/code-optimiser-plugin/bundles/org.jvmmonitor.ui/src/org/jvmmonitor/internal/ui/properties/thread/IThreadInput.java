/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.properties.thread;

import org.jvmmonitor.core.IThreadElement;

/**
 * The thread input.
 */
public interface IThreadInput {

    /**
     * Gets the thread list elements.
     * 
     * @return The thread list elements
     */
    IThreadElement[] getThreadListElements();
}
