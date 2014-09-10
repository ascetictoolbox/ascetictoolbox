package eu.ascetic.asceticarchitecture.paas.component.common.model;

public interface Interpolator {
	public void buildmodel(String appid,String vmid);
	public double estimate(long time);
}
