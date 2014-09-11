package eu.ascetic.asceticarchitecture.paas.component.common.model;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.DataConsumptionDAO;

public class WorkLoadEnergyInterpolator implements Interpolator {
	OLSMultipleLinearRegression ols;
	double[] estimatedols;
	DataConsumptionDAO dao;
	PolynomialSplineFunction interpolator;
	SimpleRegression rs ;
	
	@Override
	public void buildmodel(String appid,String vmid) {	
		double[] cpudata = dao.getCpuDataVM(appid, vmid);
		double[] endata = dao.getConsumptionDataVM(appid, vmid);
		interpolator = new SplineInterpolator().interpolate(cpudata,endata);
		ols = new OLSMultipleLinearRegression();
		int arrsize = cpudata.length;
		int observation = cpudata.length;
		int variable = 1;
		
		double[] samples = new double[arrsize*2];
		
		rs = new SimpleRegression();
		
		for (int i=0;i<arrsize;i++){
			rs.addData(cpudata[i], endata[i]);
		}
		int j=0;
		for (int i=0;i<(arrsize);i++){
			j=i*2;
			samples[j]=endata[i];
			samples[j+1]=cpudata[i];
		
		}
		
		ols.newSampleData(samples, observation, variable);
		estimatedols = ols.estimateRegressionParameters();
		for (double d : estimatedols){
			System.out.println("coeff "+d);
		}
	}

	
	
	@Override
	public double estimate(double value) {
		System.out.println("Prediction 10%" +rs.predict(0.1));
		System.out.println("Prediction 30%" +rs.predict(0.3));
		System.out.println("Prediction 60%" +rs.predict(0.6));
		System.out.println("Prediction 100%" +rs.predict(1));
		System.out.println("Value " +rs.predict(value));
		System.out.println("Prediction 10%" +estimate(0.1,estimatedols));
		System.out.println("Prediction 30%" +estimate(0.3,estimatedols));
		System.out.println("Prediction 60%" +estimate(0.6,estimatedols));
		System.out.println("Prediction 100%" +estimate(1,estimatedols));
		System.out.println("Value " +estimate(value,estimatedols));
		return estimate(value,estimatedols);
		
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
