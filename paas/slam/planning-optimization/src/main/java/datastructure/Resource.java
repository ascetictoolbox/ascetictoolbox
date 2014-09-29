/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
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
