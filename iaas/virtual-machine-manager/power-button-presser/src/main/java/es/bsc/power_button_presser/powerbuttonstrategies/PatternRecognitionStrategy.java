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

package es.bsc.power_button_presser.powerbuttonstrategies;

import es.bsc.power_button_presser.models.ClusterState;
import es.bsc.power_button_presser.utils.RscriptWrapper;
import es.bsc.power_button_presser.vmm.VmmClient;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;

public class PatternRecognitionStrategy implements PowerButtonStrategy {
    
    private final VmmClient vmmClient;
    private final static String HOLT_WINTERS_R_SCRIPT_PATH = "power-button-presser/src/main/resources/holtWinters.R";
    private final static String CPUS_DEMAND_HISTORY_CSV_PATH = "cpus_demand_history.csv";

    public PatternRecognitionStrategy(VmmClient vmmClient) {
        this.vmmClient = vmmClient;
    }

    @Override
    public void applyStrategy(ClusterState clusterState) {
        // TODO recibir histórico de donde
        int[] v = {4,4,4,4,4,5,5,5,5,3};
        HoltWintersForecast forecast = getHoltWintersForecast(v);
        // TODO decidir en funcion del forecast
    }
 
    private HoltWintersForecast getHoltWintersForecast(int[] cpusDemandHistory) {
        writeCpusDemandToCsv(cpusDemandHistory);
        return parseRscriptOutput(RscriptWrapper.runRscript(
                HOLT_WINTERS_R_SCRIPT_PATH, CPUS_DEMAND_HISTORY_CSV_PATH));
    }
    
    // Returns the absolute path of the CSV file
    private void writeCpusDemandToCsv(int[] cpusDemandHistory) {
        try (PrintWriter writer = new PrintWriter("cpus_demand_history.csv", "UTF-8")) {
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
}
