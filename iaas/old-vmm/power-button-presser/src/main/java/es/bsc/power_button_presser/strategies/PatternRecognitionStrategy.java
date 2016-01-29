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

package es.bsc.power_button_presser.strategies;

import es.bsc.power_button_presser.historicaldata.HistoricalCpuDemand;
import es.bsc.power_button_presser.hostselectors.HostSelector;
import es.bsc.power_button_presser.models.ClusterState;
import es.bsc.power_button_presser.models.Host;
import es.bsc.power_button_presser.utils.RscriptWrapper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class PatternRecognitionStrategy implements PowerButtonStrategy {
    
    private final HostSelector hostSelector;
    private final HistoricalCpuDemand historicalCpuDemand;
    private final static String HOLT_WINTERS_R_SCRIPT_PATH = "power-button-presser/src/main/resources/holtWinters.R";
    private final static String CPUS_DEMAND_HISTORY_CSV_PATH = "cpus_demand_history.csv";
    private final static int MINIMUM_HISTORICAL_CPU_DEMAND_POINTS = 10;

    public PatternRecognitionStrategy(HostSelector hostSelector, HistoricalCpuDemand historicalCpuDemand) {
        this.hostSelector = hostSelector;
        this.historicalCpuDemand = historicalCpuDemand;
    }

    @Override
    public List<Host> getPowerButtonsToPress(ClusterState clusterState) {
        saveCurrentCpuDemand(clusterState);
        int[] cpuDemandHistory = ArrayUtils.toPrimitive(historicalCpuDemand.getCpuDemandValues()
                .toArray(new Integer[historicalCpuDemand.getCpuDemandValues().size()]));
        if (cpuDemandHistory.length >= MINIMUM_HISTORICAL_CPU_DEMAND_POINTS) {
            return hostToBeTurnedOnOrOffAccordingToForecast(
                    clusterState, getHoltWintersForecast(cpuDemandHistory));
        }
        return new ArrayList<>();
    }
 
    private List<Host> hostToBeTurnedOnOrOffAccordingToForecast(ClusterState clusterState, 
                                                                HoltWintersForecast forecast) {
        if (clusterState.getTotalNumberOfCpusInOnServers() > forecast.getHigh95()) {
            return hostsToBeTurnedOnToBeInTheRangeOfCpusNeeded(clusterState, forecast);
        }
        else if (clusterState.getTotalNumberOfCpusInOnServers() < forecast.getLow95()) {
            return hostsToBeTurnedOffToBeInTheRangeOfCpusNeeded(clusterState, forecast);
        }
        return new ArrayList<>();
    }
    
    private List<Host> hostsToBeTurnedOnToBeInTheRangeOfCpusNeeded(ClusterState clusterState, 
                                                                   HoltWintersForecast forecast) {
        // First version using 95% confidence interval
        return hostSelector.selectHostsToBeTurnedOff(
                clusterState.getHostsWithoutVmsAndSwitchedOn(),
                (int) Math.round(forecast.getLow95()) - clusterState.getTotalNumberOfCpusInOnServers(),
                (int) Math.round(forecast.getHigh95()) - clusterState.getTotalNumberOfCpusInOnServers());
    }
    
    private List<Host> hostsToBeTurnedOffToBeInTheRangeOfCpusNeeded(ClusterState clusterState, 
                                                                    HoltWintersForecast forecast) {
        // First version using 95% confidence interval
        return hostSelector.selectHostsToBeTurnedOn(
                clusterState.getTurnedOffHosts(),
                clusterState.getTotalNumberOfCpusInOnServers() - (int) Math.round(forecast.getHigh95()),
                clusterState.getTotalNumberOfCpusInOnServers() - (int) Math.round(forecast.getLow95()));
    }
    
    private HoltWintersForecast getHoltWintersForecast(int[] cpusDemandHistory) {
        writeCpusDemandToCsv(cpusDemandHistory);
        return parseRscriptOutput(RscriptWrapper.runRscript(
                HOLT_WINTERS_R_SCRIPT_PATH, CPUS_DEMAND_HISTORY_CSV_PATH));
    }

    private void writeCpusDemandToCsv(int[] cpusDemandHistory) {
        try (PrintWriter writer = new PrintWriter(CPUS_DEMAND_HISTORY_CSV_PATH, "UTF-8")) {
            writer.println(StringUtils.join(cpusDemandHistory, ','));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private HoltWintersForecast parseRscriptOutput(String rScriptOutput) {
        String[] outputFields = rScriptOutput.split("\\r?\\n")[1].split("\\s+");
        return new HoltWintersForecast(Double.parseDouble(outputFields[1]),
                Double.parseDouble(outputFields[2]),
                Double.parseDouble(outputFields[3]),
                Double.parseDouble(outputFields[4]),
                Double.parseDouble(outputFields[5]));
    }
    
    private void saveCurrentCpuDemand(ClusterState clusterState) {
        historicalCpuDemand.addCpuDemandPoint(clusterState.getTotalNumberOfCpusInOnServers());
    }
    
}
