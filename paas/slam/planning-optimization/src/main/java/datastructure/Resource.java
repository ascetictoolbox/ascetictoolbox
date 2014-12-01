/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package datastructure;

import org.apache.log4j.Logger;

/**
 * Each <code>Resource</code> could represent CPU or Memory or Harddisk.
 * 
 * @author Kuan Lu
 */
public class Resource {
    /**
     * the name of resource.
     */
    private String resourceName = "";
    /**
     * the amount of resource.
     */
    private int amount;
    /**
     * the price of resource.
     */
    private double price;

    private static final Logger LOGGER = Logger.getLogger(Resource.class);

    public Resource() {
    }

    /**
     * Sets the name of resource.
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * Sets the amount.
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Sets the price.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Gets the price.
     */
    public double getPrice() {
        if (this.price == -1) {
            LOGGER.info("This is the resource request from client, no price information available");
            return -1;
        }
        return price;
    }

    /**
     * Gets the name of resource.
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Gets the amount.
     */
    public int getAmount() {
        return amount;
    }

}
