/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.core.selfadaptation;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class PeriodicSelfAdaptationRunnable implements Runnable {

    // Check if the self-adaptation options have changed every X minutes
    private static final int INTERVAL_CHECK_NEW_CONFIG_MIN = 1;

    private final SelfAdaptationManager selfAdaptationManager;

    public PeriodicSelfAdaptationRunnable(SelfAdaptationManager selfAdaptationManager) {
        this.selfAdaptationManager = selfAdaptationManager;
    }

    // This thread executes the periodic self adaptation.
    // The time interval is defined in the self adaptation options
    // If the periodic self adaptation is not active, then this thread checks every INTERVAL_CHECK_NEW_CONFIG_MIN
    // minutes whether the periodic self adaptation configuration has changed
    @Override
    public void run() {
        boolean applySelfAdaptation = selfAdaptationOptionIsActive();
        int threadSleepMinutes = getThreadSleepMinutes(applySelfAdaptation);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(threadSleepMinutes*60*1000); // minutes to milliseconds
                if (applySelfAdaptation) {
                    selfAdaptationManager.applyPeriodicSelfAdaptation();
                }
                applySelfAdaptation = selfAdaptationOptionIsActive();
                threadSleepMinutes = getThreadSleepMinutes(applySelfAdaptation);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean selfAdaptationOptionIsActive() {
        return getIntervalMinutes() != 0;
    }

    private int getIntervalMinutes() {
        return selfAdaptationManager
                .getSelfAdaptationOptions()
                .getPeriodicSelfAdaptationOps()
                .getTimeIntervalMinutes();
    }

    private int getThreadSleepMinutes(boolean applySelfAdaptation) {
        return applySelfAdaptation ? getIntervalMinutes() : INTERVAL_CHECK_NEW_CONFIG_MIN;
    }

}
