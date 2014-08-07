/**
 * Copyright 2014 Athens University of Economics and Business
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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.training;

/**
 * This is the standard interface for any training model to be loaded to the
 * ASCETiC architecture.
 *
 * @author E. Agiatzidou
 */
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;
import java.util.ArrayList;

public interface EnergyModelTrainerInterface {

    /**
     * This function stores the values that are needed for the training of the
     * model and returns true when the appropriate number of values has been
     * gathered.
     * @param host The host to add the training data for
     * @param usageCPU
     * @param usageRAM
     * @param wattsUsed
     * @param numberOfValues
     * @return 
     */
    public boolean trainModel(Host host, double usageCPU, double usageRAM, double wattsUsed, int numberOfValues);

    /**
     * This function stores the values that are needed for the training of the
     * model.
     * @param host The host to add the training data for
     * @param data 
     */
    public void trainModel(Host host, ArrayList<HostEnergyCalibrationData> data);
    
    /**
     * This function calculates the coefficients of the models and returns them
     * to the caller.
     * @param host
     * @return 
     */
    public EnergyModel retrieveModel(Host host);

}
