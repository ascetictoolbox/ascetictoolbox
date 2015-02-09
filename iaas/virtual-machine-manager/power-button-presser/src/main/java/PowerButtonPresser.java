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

import config.Config;
import httpClient.HttpClient;
import models.ClusterState;
import powerbuttonstrategies.*;
import vmm.VmmClient;

public class PowerButtonPresser {
    
    private static Config config = new Config(
            60, Strategy.JUST_IN_TIME, "http://0.0.0.0:34372/vmmanager/", "vms/", "nodes/",
            "node/", "powerButton/");
    
    private static VmmClient vmmClient = new VmmClient(
            new HttpClient(), config.getVmmBaseUrl(), config.getVmmVmsPath(), config.getVmmHostsPath(), 
            config.getVmmNodePath(), config.getVmmPowerButtonPath());
    
    private static PowerButtonStrategy powerButtonStrategy = getStrategy();
    
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        while (true) {
            powerButtonStrategy.applyStrategy(new ClusterState(vmmClient.getVms(), vmmClient.getHosts()));
            try {
                Thread.sleep(config.getIntervalSeconds()*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static PowerButtonStrategy getStrategy() { // Turn into factory?
        switch (config.getStrategy()) {
            case ALL_SERVERS_ON:
                return new AllServersOnStrategy(vmmClient);
            case JUST_IN_TIME:
                return new JustInTimeStrategy(vmmClient);
            case N_BACKUP_HOSTS:
                return new NBackupHostsStrategy(vmmClient);
            case PATTERN_RECOGNITION:
                return new PatternRecognitionStrategy(vmmClient);
            default:
                return null; // Throw exception here
        }
    }
    
}
