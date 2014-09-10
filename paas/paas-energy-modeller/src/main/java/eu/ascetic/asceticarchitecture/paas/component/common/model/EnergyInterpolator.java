package eu.ascetic.asceticarchitecture.paas.component.common.model;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.DataConsumptionDAO;

public class EnergyInterpolator implements Interpolator {

	DataConsumptionDAO dao;
	PolynomialSplineFunction interpolator;
	
	
	@Override
	public void buildmodel(String appid,String vmid) {	
		interpolator = new SplineInterpolator().interpolate(dao.getConsumptionDataVM(appid, vmid),dao.getTimeDataVM(appid, vmid));
	}

	@Override
	public double estimate(long time) {
		return interpolator.value(time);
	}
	
	public void providedata(DataConsumptionDAO dao){
		this.dao = dao;
	}

}
