package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Rojo. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmEstimate {

    private String id;
    private double powerEstimate;
    private double priceEstimate;

    public VmEstimate(String id, double powerEstimate, double priceEstimate) {
        this.id = id;
        this.powerEstimate = powerEstimate;
        this.priceEstimate = priceEstimate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPowerEstimate() {
        return powerEstimate;
    }

    public void setPowerEstimate(double powerEstimate) {
        this.powerEstimate = powerEstimate;
    }

    public double getPriceEstimate() {
        return priceEstimate;
    }

    public void setPriceEstimate(double priceEstimate) {
        this.priceEstimate = priceEstimate;
    }

}
