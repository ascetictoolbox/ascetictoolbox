/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.interpolator.old;

public interface InterpolatorService {
	public void buildmodel(String appid,String vmid);
	public double estimate(double value);
}
