/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.common.model.interpolator;

import java.util.Vector;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.log4j.Logger;

public class GenericValuesInterpolator  {

	private long starttime = 0;
	private long lasttime = 0;
	SimpleRegression rs ;
	OLSMultipleLinearRegression ols;
	double[] estimatedols;
	PolynomialSplineFunction interpolator;
	private final static Logger LOGGER = Logger.getLogger(GenericValuesInterpolator.class.getName());
	

	public void buildmodel(double[] timeseries,double[] dataseries) {
		
		ols = new OLSMultipleLinearRegression();
		Vector<Double> newtimes = new Vector<Double>();
		Vector<Double> newdata = new Vector<Double>();
		newtimes.add(timeseries[0]);
		newdata.add(dataseries[0]);
		//LOGGER.info("Removing duplicate "+timeseries.length);
		for(int i=1;i<timeseries.length;i++){
			if (timeseries[i]!=timeseries[i-1]){
				newtimes.add(timeseries[i]);
				newdata.add(dataseries[i]);
			}
			
		}
		//LOGGER.info("Removed "+newtimes.size());
		timeseries = new double[newtimes.size()];
		dataseries = new double[newtimes.size()];
		for (int j=0;j<newtimes.size();j++){
			timeseries[j] = newtimes.get(j);
			dataseries[j] = newdata.get(j);
		}
		ols = new OLSMultipleLinearRegression();
		int arrsize = timeseries.length;
		int observation = timeseries.length;
		int variable = 1;
		
		
		double[] samples = new double[arrsize*2];
		
		rs = new SimpleRegression();
		
		for (int i=0;i<arrsize;i++){
			rs.addData(dataseries[i], timeseries[i]);
		}
		int j=0;
		for (int i=0;i<(arrsize);i++){
			j=i*2;
			samples[j]=dataseries[i];
			samples[j+1]=timeseries[i];
		
		}
		interpolator = new SplineInterpolator().interpolate(timeseries,dataseries);
		ols.newSampleData(samples, observation, variable);
		estimatedols = ols.estimateRegressionParameters();
	}
	

	public double estimate(double value) {
	
			
			
			double est = estimate(value,estimatedols);
			//LOGGER.info("Linear Predictor Value for "+ value + " now :"+rs.predict(value));
			//LOGGER.info("Linear Predictor Value for "+ value + " now :"+est);
			//LOGGER.info("Value from poly " +est);
			if (est<0)return 0;
			return est;

		//return interpolator.value(value);
	}
	private double estimate(double value, double[] coefficient) {
		double res = 0;
		for(int i = 0; i < coefficient.length; ++i)
			res += coefficient[i] * Math.pow(value, i);
		return res;
	}
	public long getStarttime() {
		return starttime;
	}


	public void setStarttime(long starttime) {
		this.starttime = starttime;
	}


	public long getLasttime() {
		return lasttime;
	}


	public void setLasttime(long lasttime) {
		this.lasttime = lasttime;
	}

	

}
