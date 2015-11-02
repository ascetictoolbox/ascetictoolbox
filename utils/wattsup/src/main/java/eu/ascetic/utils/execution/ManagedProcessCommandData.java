/**
 * Copyright 2014 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.utils.execution;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 *
 * @author Richard
 */
public class ManagedProcessCommandData {

    private int startTime = 0;
    private GregorianCalendar actualStart = new GregorianCalendar();
    private File workingDirectory = null;
    private String command = "";
    private ArrayList<String> environment = new ArrayList<>();
    private String stdIn = "";
    private String stdOut = "";
    private String stdError = "";
    private boolean outToScreen = false;

    /**
     * @return the workingDirectory
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @param workingDirectory the workingDirectory to set
     */
    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * @param command the command to set
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * @return the environment
     */
    public ArrayList<String> getEnvironment() {
        return environment;
    }

    /**
     * @param environment the environment to set
     */
    public void setEnvironment(ArrayList<String> environment) {
        this.environment = environment;
    }

    /**
     * @return the stdIn
     */
    public String getStdIn() {
        return stdIn;
    }

    /**
     * @param stdIn the stdIn to set
     */
    public void setStdIn(String stdIn) {
        this.stdIn = stdIn;
    }

    /**
     * @return the stdOut
     */
    public String getStdOut() {
        return stdOut;
    }

    /**
     * @param stdOut the stdOut to set
     */
    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    /**
     * @return the stdError
     */
    public String getStdError() {
        return stdError;
    }

    /**
     * @param stdError the stdError to set
     */
    public void setStdError(String stdError) {
        this.stdError = stdError;
    }

    /**
     * @return the startTime
     */
    public int getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the outToScreen
     */
    public boolean isOutToScreen() {
        return outToScreen;
    }

    /**
     * @param outToScreen the outToScreen to set
     */
    public void setOutToScreen(boolean outToScreen) {
        this.outToScreen = outToScreen;
    }

    /**
     * @return the actualStart
     */
    public GregorianCalendar getActualStart() {
        return actualStart;
    }

    /**
     * @param actualStart the actualStart to set
     */
    public void setActualStart(GregorianCalendar actualStart) {
        this.actualStart = actualStart;
    }

}
