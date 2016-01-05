/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.interpolator.impl;

import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * 
 * @author sommacam
 * this utility class is used to interpolate vvalues from a series of data, is used as very basic predicotr alternative to the one based on Weka
 */
public class DataInterpolator  {
	OLSMultipleLinearRegression ols;
	double[] estimatedols;
	PolynomialSplineFunction interpolator;
	SimpleRegression rs ;
	
	
	public void buildmodel(double timedata[],double data[]) {	
		rs = new SimpleRegression();
		for (int i=0;i<timedata.length;i++){
			rs.addData(timedata[i], data[i]);
		}
	}
	
	public double estimate(double value) {
		return rs.predict(value);
	}
	
}
