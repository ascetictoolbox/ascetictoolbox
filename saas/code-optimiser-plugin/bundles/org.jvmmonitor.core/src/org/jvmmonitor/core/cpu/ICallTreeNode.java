/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.core.cpu;

/**
 * The call tree node.
 */
public interface ICallTreeNode extends IMethodNode {

    /**
     * Gets the total invocation time in percentage.
     * 
     * @return the total invocation time in percentage
     */
    double getTotalTimeInPercentage();

    /**
     * Gets the total invocation time.
     * 
     * @return the total invocation time
     */
    long getTotalTime();
    
    /**
     * This gets the average power consumed during invocation of this thread.
     * @return The average power consumed invoking this thread
     */    
    double getAveragePower();
    
    /**
     * This gets the total energy consumed during invocation of this thread.
     * @return The total energy consumed invoking this thread
     */
    double getTotalEnergy();
    
}
