package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.impl;


import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.ApplicationSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.interpolator.DataInterpolator;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.PredictorInterface;



public class EnergyModellerPredictor implements PredictorInterface {

	
	private final static Logger LOGGER = Logger.getLogger(PredictorInterface.class.getName());
	private EnergyDataAggregatorService service;
	
	@Override
	public double estimate(String providerid, String applicationid,	List<String> vmids, String eventid, Unit unit, long timelater) {
		
		// only power then from power get energy estimation
		double estimation=0;
		Date current = new Date();
		// from one week
		long begin = current.getTime()-604800000;
		// to now
		long end = current.getTime();
		
		// add after millisec conversion the time of the forecast
		long forecasttime = end + (timelater*1000);
		
		for (String vmid :vmids){
			 LOGGER.info("Forecaster "+applicationid + " VM "+vmid);
			DataInterpolator interpolator;
			Attribute cpu = new Attribute("CPU");
			//Attribute memory =  new Attribute("Memory");
			Attribute power = new Attribute("Power");
			
			FastVector fvWekaAttributes = new FastVector(3);
			fvWekaAttributes.addElement(cpu);
			//fvWekaAttributes.addElement(memory);
			fvWekaAttributes.addElement(power);
			
			
			// now
			List<ApplicationSample> applicationSample = service.getSamplesInInterval(applicationid, null, vmid, new Timestamp(begin), new Timestamp(end));
			 LOGGER.info("Samples "+applicationSample.size());
			Instances isTrainingSet = new Instances("Powermodel", fvWekaAttributes, 0);
	
			 isTrainingSet.setClassIndex(2);
			 
			 Instance iExample;
			 
			 double[] timestamps= new double[applicationSample.size()];
			 double[] data=new double[applicationSample.size()];
			 
			 
			 System.out.println("Samples "+applicationSample.size());
			 for (int i = 0; i<applicationSample.size();i++) {
				 iExample = new DenseInstance(2);
				 iExample.setValue((Attribute)fvWekaAttributes.elementAt(0), new Float(applicationSample.get(i).getC_value()));
				 iExample.setValue((Attribute)fvWekaAttributes.elementAt(1), new Float(applicationSample.get(i).getP_value()));
				 //iExample.setValue((Attribute)fvWekaAttributes.elementAt(2), new Float(samples.get(i).getPower()));
				 isTrainingSet.add(iExample);
				 timestamps[i]=applicationSample.get(i).getTime();
				 data[i]=applicationSample.get(i).getC_value();
				 
			 }
	
			 interpolator = new DataInterpolator();
			 interpolator.buildmodel(timestamps, data);
			 
			 double valueforecast =  interpolator.estimate(forecasttime);
			 LOGGER.info("Forecasted CPU"+valueforecast+" at time "+forecasttime);
			 iExample = new DenseInstance(2);
			 iExample.setValue((Attribute)fvWekaAttributes.elementAt(0),valueforecast);
			 //iExample.setValue((Attribute)fvWekaAttributes.elementAt(1), 1024);
	
			 isTrainingSet.add(iExample);
			 
			 LinearRegression model = new LinearRegression();
			 try {
				 model.buildClassifier(isTrainingSet);
			
				 LOGGER.info("Model "+model);
		
				 Instance ukPower = isTrainingSet.lastInstance();
				 double powerest = model.classifyInstance(ukPower);
				 LOGGER.info("Power ("+ukPower+"): "+powerest);
				 return powerest;
			 } catch (Exception e) {
					e.printStackTrace();
			 }
		
			
		}
		return estimation;
	}

	@Override
	public void setEnergyService(EnergyDataAggregatorService service) {
		this.service = service;
	}

}