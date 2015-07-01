/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.interpolator;

import java.util.Vector;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.DataConsumptionDAO;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.InterpolatorService;

public class EnergyInterpolator implements InterpolatorService {

	DataConsumptionDAO dao;
	PolynomialSplineFunction interpolator;
	private final static Logger LOGGER = Logger.getLogger(EnergyInterpolator.class.getName());
	
	@Override
	public void buildmodel(String appid,String vmid) {
		
//		double[] timedata = dao.getTimeDataVM(appid, vmid);
//		double[] endata = dao.getConsumptionByTimeVM(appid, vmid);
//		Vector<Double> newtimes = new Vector<Double>();
//		Vector<Double> newdata = new Vector<Double>();
//		newtimes.add(timedata[0]);
//		newdata.add(endata[0]);
//		LOGGER.info("Removing duplicate "+timedata.length);
//		for(int i=1;i<timedata.length;i++){
//			if (timedata[i]!=timedata[i-1]){
//				newtimes.add(timedata[i]);
//				newdata.add(endata[i]);
//			}
//			
//		}
//		LOGGER.info("Removed "+newtimes.size());
//		timedata = new double[newtimes.size()];
//		endata = new double[newtimes.size()];
//		for (int j=0;j<newtimes.size();j++){
//			timedata[j] = newtimes.get(j);
//			endata[j] = newdata.get(j);
//		}
//		
//		interpolator = new SplineInterpolator().interpolate(timedata,endata);
	}
	
	

	@Override
	public double estimate(double value) {
		return interpolator.value(value);
	}
	
	public void providedata(DataConsumptionDAO dao){
		this.dao = dao;
	}

}
