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

package es.bsc.power_button_presser.config;

import es.bsc.power_button_presser.strategies.Strategy;

public class Config {
    
    private final int intervalSeconds;
    private final Strategy strategy;
    private final String vmmBaseUrl;
    private final String vmmVmsPath;
    private final String vmmHostsPath;
    private final String vmmNodePath;
    private final String vmmPowerButtonPath;

    public Config(int intervalSeconds, Strategy strategy, String vmmBaseUrl, String vmmVmsPath,
                  String vmmHostsPath, String vmmNodePath, String vmmPowerButtonPath) {
        this.intervalSeconds = intervalSeconds;
        this.strategy = strategy;
        this.vmmBaseUrl = vmmBaseUrl;
        this.vmmVmsPath = vmmVmsPath;
        this.vmmHostsPath = vmmHostsPath;
        this.vmmNodePath = vmmNodePath;
        this.vmmPowerButtonPath = vmmPowerButtonPath;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public String getVmmBaseUrl() {
        return vmmBaseUrl;
    }

    public String getVmmVmsPath() {
        return vmmVmsPath;
    }

    public String getVmmHostsPath() {
        return vmmHostsPath;
    }

    public String getVmmNodePath() {
        return vmmNodePath;
    }

    public String getVmmPowerButtonPath() {
        return vmmPowerButtonPath;
    }
    
}
