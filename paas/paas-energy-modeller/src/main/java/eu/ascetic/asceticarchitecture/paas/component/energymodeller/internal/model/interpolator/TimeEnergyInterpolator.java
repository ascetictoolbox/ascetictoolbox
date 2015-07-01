/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.interpolator;

import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.DataConsumptionDAO;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.InterpolatorService;

public class TimeEnergyInterpolator implements InterpolatorService {
	
	private final static Logger LOGGER = Logger.getLogger(TimeEnergyInterpolator.class.getName());
	
	OLSMultipleLinearRegression ols;
	double[] estimatedols;
	DataConsumptionDAO dao;
	PolynomialSplineFunction interpolator;
	SimpleRegression rs ;
	
	@Override
	public void buildmodel(String appid,String vmid) {	
//		double[] timedata = dao.getTimeDataVM(appid, vmid);
//		double[] endata = dao.getConsumptionByTimeVM(appid, vmid);
//		Vector<Double> newtimes = new Vector<Double>();
//		Vector<Double> newdata = new Vector<Double>();
//		newtimes.add(timedata[0]);
//		newdata.add(endata[0]);
//		//LOGGER.info("Removing duplicate "+timedata.length);
//		for(int i=1;i<timedata.length;i++){
//			if (timedata[i]!=timedata[i-1]){
//				newtimes.add(timedata[i]);
//				newdata.add(endata[i]);
//			}
//			
//		}
//		//LOGGER.info("Removed "+newtimes.size());
//		timedata = new double[newtimes.size()];
//		endata = new double[newtimes.size()];
//		for (int j=0;j<newtimes.size();j++){
//			timedata[j] = newtimes.get(j);
//			endata[j] = newdata.get(j);
//		}
//		
//		ols = new OLSMultipleLinearRegression();
//		int arrsize = timedata.length;
//		int observation = timedata.length;
//		int variable = 1;
//		
//		double[] samples = new double[arrsize*2];
//		
//		rs = new SimpleRegression();
//		
//		for (int i=0;i<arrsize;i++){
//			rs.addData(timedata[i], endata[i]);
//		}
//		int j=0;
//		for (int i=0;i<(arrsize);i++){
//			j=i*2;
//			samples[j]=endata[i];
//			samples[j+1]=timedata[i];
//		
//		}
//		interpolator = new SplineInterpolator().interpolate(timedata,endata);
//		ols.newSampleData(samples, observation, variable);
//		estimatedols = ols.estimateRegressionParameters();
		//for (double d : estimatedols){
			//LOGGER.info("coeff "+d);
		//}
	}

	
	
	@Override
	public double estimate(double value) {

		
		//LOGGER.info("Linear Predictor Value " +rs.predict(value));
		double est = estimate(value,estimatedols);
		//LOGGER.info("Value from poly " +est);
		return est;
		
	}
	
	public void providedata(DataConsumptionDAO dao){
		this.dao = dao;
	}
	private double estimate(double value, double[] coefficient) {
		double res = 0;
		for(int i = 0; i < coefficient.length; ++i)
			res += coefficient[i] * Math.pow(value, i);
		return res;
	}

	
}
