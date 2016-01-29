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

package es.bsc.demiurge.core.monitoring.hosts;

import es.bsc.demiurge.core.models.hosts.HostPowerButtonAction;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class HostButtonPresserRunnable implements Runnable {

	private static final int TURN_ONOFF_DELAY_SECONDS = 30;

    private final Host host;
    private final HostPowerButtonAction action;

    public HostButtonPresserRunnable(Host host, HostPowerButtonAction action) {
        this.host = host;
        this.action = action;
    }
    
    @Override
    public void run() {
        switch (action) {
            case TURN_ON:
                turnOnServer();
                break;
            case TURN_OFF:
                turnOffServer();
                break;
            default:
                break;
        }
    }
    
    private void turnOnServer() {
        try {
            Thread.sleep(TURN_ONOFF_DELAY_SECONDS*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        host.turnOn();
    }
    
    private void turnOffServer() {
        try {
            Thread.sleep(TURN_ONOFF_DELAY_SECONDS*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        host.turnOff();
    }
    
}
