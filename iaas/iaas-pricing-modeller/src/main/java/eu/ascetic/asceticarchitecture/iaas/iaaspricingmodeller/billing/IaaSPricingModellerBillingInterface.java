/**
 *  Copyright 2014 Athens University of Economics and Business
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.billing;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.pricingschemesrepository.IaaSPricingModellerPricingScheme;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.*;

/**
 * This is the standard interface for any pricing module to be loaded into
 * the ASCETiC architecture. This interface includes all the methods that another component
 * from the architecture may call. 
 * @author E. Agiatzidou
 */

public interface IaaSPricingModellerBillingInterface{
	void registerVM(VMstate vm);
	void unregisterVM(VMstate vm);
	void stopChargingVM(VMstate vm);
	double getVMCharges(String VMid);


	
}