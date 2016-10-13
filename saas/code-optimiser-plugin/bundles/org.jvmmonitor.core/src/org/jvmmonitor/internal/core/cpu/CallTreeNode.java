/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.core.cpu;

import java.util.ArrayList;
import java.util.List;

import org.jvmmonitor.core.cpu.ICallTreeNode;
import org.jvmmonitor.core.cpu.ICpuModel;
import org.jvmmonitor.core.cpu.ITreeNode;

/**
 * The call tree node.
 */
public class CallTreeNode extends AbstractMethodNode implements ICallTreeNode {

    /** The child nodes. */
    private List<CallTreeNode> frames;

    /** The total invocation time. */
    private long totalTime;

    /** The parent frame node. */
    private CallTreeNode parentFrameNode;

    /** The CPU usage in percentage. */
    private double cpuUsage;
    
    /**
     * The constructor.
     * 
     * @param cpuModel
     *            the cpuModel
     * @param name
     *            the qualified method name
     * @param time
     *            the invocation time
     * @param count
     *            the invocation count
     * @param parent
     *            the parent frame node
     * @param thread
     *            the thread node
     * @param energy
     *          the energy consumed
     */
    public CallTreeNode(ICpuModel cpuModel, String name, long time, int count, CallTreeNode parent,
            ThreadNode<CallTreeNode> thread, double energy) {
        this(cpuModel, name, time, count, thread, energy);
        parentFrameNode = parent;
    }

    /**
     * The constructor for root frame node.
     * 
     * @param cpuModel
     *            the cpuModel
     * @param name
     *            the qualified method name
     * @param time
     *            the invocation time
     * @param count
     *            the invocation count
     * @param thread
     *            the thread node
     */
    public CallTreeNode(ICpuModel cpuModel, String name, long time, int count, ThreadNode<CallTreeNode> thread, double energy) {
        super(cpuModel, name, thread, energy);

        totalTime = time;
        invocationCount = count;
        totalEnergy = energy;

        frames = new ArrayList<CallTreeNode>();
    }

    /*
     * @see ICallTreeNode#getChildren()
     */
    @Override
    public List<CallTreeNode> getChildren() {
        return frames;
    }

    /*
     * @see ITreeNode#getChild(String)
     */
    @Override
    public CallTreeNode getChild(String name) {
        for (CallTreeNode frameNode : frames) {
            if (frameNode.getName().equals(name)) {
                return frameNode;
            }
        }
        return null;
    }

    /*
     * @see ICallTreeNode#hasChildren()
     */
    @Override
    public boolean hasChildren() {
        return frames.size() > 0;
    }

    /*
     * @see ITreeNode#getParent()
     */
    @Override
    public ITreeNode getParent() {
        return (parentFrameNode != null) ? parentFrameNode : threadNode;
    }

    /*
     * @see AbstractMethodNode#clear()
     */
    @Override
    public void clear() {
        for (CallTreeNode frameNode : frames) {
            selfTime = 0;
            totalTime = 0;
            invocationCount = 0;
            frameNode.clear();
        }
    }

    /*
     * @see ICallTreeNode#getTimeInPercentage()
     */
    @Override
    public double getTotalTimeInPercentage() {
        double rootTotalTime = getRootTotalTime();
        if (rootTotalTime == 0) {
            return 0;
        }
        return totalTime / rootTotalTime * 100;
    }

    /*
     * @see ICallTreeNode#getTotalTime()
     */
    @Override
    public long getTotalTime() {
        return totalTime;
    }

    /*
     * @see IThreadNode#getAveragePower()
     */
    @Override
    public double getAveragePower() {
        if (selfTime == 0 || totalEnergy == 0) {
            return 0;
        }   
        return totalEnergy / ((double)selfTime);
    }

    /*
     * @see IThreadNode#getTotalEnergy()
     */
    @Override
    public double getTotalEnergy() {
        return totalEnergy;
    }
    
    /*
     * @see IThreadNode#getSelfTotalEnergy()
     */
    @Override
    public double getSelfTotalEnergy() {
        if (selfTime == 0 || totalEnergy == 0 || getRootTotalTime() == 0) {
            return 0;
        }           
        return (totalEnergy / totalTime) * selfTime;
    }    
    
    @Override
    public double getAverageCpuUsage() {
        return cpuUsage;
    }

    /*
     * @see AbstractMethodNode#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /*
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CallTreeNode)) {
            return false;
        }
        CallTreeNode frameNode = (CallTreeNode) obj;

        if (frameNode.getName().equals(qualifiedMethodName) && frameNode.getSelfTime() == selfTime
                && frameNode.getTotalTime() == totalTime && frameNode.getInvocationCount() == invocationCount
                && frameNode.getIndentation().length() == getIndentation().length()) {
            return true;
        }

        return false;
    }

    /*
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getIndentation()).append(getName()).append('\t');
        buffer.append(getTotalTime()).append('\t');
        buffer.append(getTotalTimeInPercentage()).append('\t');
        buffer.append(getSelfTime()).append('\t');
        buffer.append(getSelfTimeInPercentage()).append('\t');
        buffer.append(getInvocationCount());
        return buffer.toString();
    }

    /**
     * Adds the child node.
     * 
     * @param node
     *            The child node
     */
    public void addChild(CallTreeNode node) {
        frames.add(node);
    }

    /**
     * Sets the total invocation time.
     * 
     * @param time
     *            the total invocation time
     */
    public void setTotalTime(long time) {
        totalTime = time;
    }
    
    /**
     * Sets the total energy consumed.
     * 
     * @param energy
     *            the total energy consumed
     */
    public void setTotalEnergy(double energy) {
        if (energy < 0) {
            return;
        }
        totalEnergy = energy;
    }    

    /**
     * Sets the self invocation time.
     * 
     * @param time
     *            the self invocation time
     */
    public void setSelfTime(long time) {
        selfTime = time;
    }

    /**
     * Sets the invocation count.
     * 
     * @param count
     *            The invocation count
     */
    public void setInvocationCount(int count) {
        invocationCount = count;
    }

    /**
     * Dumps the profile data.
     * 
     * @param buffer
     *            The string buffer
     * @param nest
     *            The nest count
     */
    public void dump(StringBuffer buffer, int nest) {
        for (int i = 0; i < nest; i++) {
            buffer.append('\t');
        }

        String method = qualifiedMethodName.replaceAll("<", "&lt;").replaceAll( //$NON-NLS-1$ //$NON-NLS-2$
                ">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
        buffer.append("<frame name=\"").append(method).append("\" cnt=\"") //$NON-NLS-1$ //$NON-NLS-2$
                .append(invocationCount).append("\" time=\"").append(totalTime) //$NON-NLS-1$
                .append("\" energy=\"") //$NON-NLS-1$ //$NON-NLS-2$
                .append(totalEnergy)
                .append("\""); //$NON-NLS-1$
        if (frames.size() > 0) {
            buffer.append(">\n"); //$NON-NLS-1$
            for (CallTreeNode frameNode : frames) {
                frameNode.dump(buffer, nest + 1);
            }
            for (int i = 0; i < nest; i++) {
                buffer.append('\t');
            }
            buffer.append("</frame>\n"); //$NON-NLS-1$
        } else {
            buffer.append("/>\n"); //$NON-NLS-1$
        }
    }

    /**
     * Gets the indentation that indicates the depth of tree.
     * 
     * @return The indentation
     */
    private String getIndentation() {
        StringBuffer buffer = new StringBuffer();

        ITreeNode parent = getParent();
        if (parent instanceof CallTreeNode) {
            CallTreeNode callTreeNode = (CallTreeNode) parent;
            buffer.append(callTreeNode.getIndentation()).append(' ');
        }

        return buffer.toString();
    }
    
}
