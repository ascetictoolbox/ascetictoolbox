/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.PredictorInterface;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.impl.EMNeuralPredictor;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.impl.EnergyModellerPredictor;

/**
 * 
 * @author sommacam
 * generic class that builds a predictor based and return it as a generic predictorinterface class, if not options are privided it builds a predictor based on the Weka library
 */
public class PredictorBuilder {

	
	private static PredictorInterface predictor;
	
	public static PredictorInterface getPredictor(){
		if (predictor == null) predictor = (PredictorInterface) new EnergyModellerPredictor();
		return (PredictorInterface)predictor ;
	}
	
	public static PredictorInterface getPredictor(String type){
		// M. Fontanella - 07 Apr 2016 - begin
		//if (predictor == null) predictor = new EMNeuralPredictor();
		//return (PredictorInterface)predictor ;
		if (type.equals("basic")) {
			if (predictor == null) predictor = (PredictorInterface) new EnergyModellerPredictor();
			return (PredictorInterface)predictor ;
		}
		else {
			if (predictor == null) predictor = new EMNeuralPredictor();
			return (PredictorInterface)predictor ;			
		}
		// M. Fontanella - 07 Apr 2016 - end

	}

	
}
