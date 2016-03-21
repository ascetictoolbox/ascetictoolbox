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
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.interpolator.impl.DataInterpolator;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.PredictorInterface;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorServiceQueue;



public class EnergyModellerPredictor implements PredictorInterface {

	
	private final static Logger LOGGER = Logger.getLogger(PredictorInterface.class.getName());
	private EnergyDataAggregatorServiceQueue service;
	
	@Override
	public double estimate(String providerid, String applicationid,	String deploymentid, List<String> vmids, String eventid, Unit unit, long timelater) {
		
		// only power then from power get energy estimation
		double cur_power = 0;
		int count=0;
		for (String vmid :vmids){
			count++;
			cur_power = cur_power + estimate( providerid,  applicationid,	 deploymentid,  vmid,  eventid,  unit,  timelater) ;
		}
		
		if (count>0)return cur_power/count;
		return 0;
	}

	@Override
	public void setEnergyService(EnergyDataAggregatorServiceQueue service) {
		this.service = service;
	}

	@Override
	public double estimate(List<DataConsumption> samples, Unit unit,long timelater) {
		double estimation=0;
		Date current = new Date();
		// from one week
		long begin = current.getTime()-604800000;
		// to now
		long end = current.getTime();
		
		// add after millisec conversion the time of the forecast
		long forecasttime = end + (timelater*1000);
		LOGGER.info("Samples "+samples.size());
		//DataInterpolator interpolator;
		Attribute time = new Attribute("Time");
		//Attribute memory =  new Attribute("Memory");
		Attribute power = new Attribute("Power");
		
		FastVector fvWekaAttributes = new FastVector(2);
		fvWekaAttributes.addElement(time);
		//fvWekaAttributes.addElement(memory);
		fvWekaAttributes.addElement(power);
		
		Instances isTrainingSet = new Instances("Powermodel", fvWekaAttributes, 0);

		 isTrainingSet.setClassIndex(1);
		 
		 Instance iExample;
		 
		 double[] timestamps= new double[samples.size()];
		 double[] data=new double[samples.size()];
		
		int i=0;
		for (DataConsumption dc : samples){
			
				 iExample = new DenseInstance(1);
				 iExample.setValue((Attribute)fvWekaAttributes.elementAt(0), new Float(dc.getTime()));
				 iExample.setValue((Attribute)fvWekaAttributes.elementAt(1), new Float(dc.getVmpower()));
				 isTrainingSet.add(iExample);
				 timestamps[i]=dc.getTime();
				 data[i]=dc.getVmpower();
				 i++;
	    }
		
		
		
		iExample = new DenseInstance(1);
		iExample.setValue((Attribute)fvWekaAttributes.elementAt(0),forecasttime);
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
			
		
		return estimation;
	
	}

	@Override
	public double estimate(String providerid, String applicationid,	String deploymentid, String vm, String eventid, Unit unit, long timelater) {

		double estimation=0;
		Date current = new Date();
		// from one week
		long begin = current.getTime()-604800000;
		// to now
		long end = current.getTime();
		LOGGER.info("Forecaster now is "+end);
		// add after millisec conversion the time of the forecast
		long forecasttime = end/1000 + (timelater);
		
		// M. Fontanella - 20 Jan 2016 - begin
		LOGGER.info("Forecaster Provider "+providerid + " Application "+applicationid + " VM "+vm + " at time "+forecasttime);
		LOGGER.info("############ Forecasting STARTED FOR Provider "+providerid + " Application "+applicationid + " VM "+vm+ "############");
		// M. Fontanella - 20 Jan 2016 - end
		DataInterpolator cpuinterpolator;
		DataInterpolator meminterpolator;
		Attribute cpu = new Attribute("CPU");
		Attribute memory =  new Attribute("Memory");
		Attribute power = new Attribute("Power");
			
		FastVector fvWekaAttributes = new FastVector(3);
		fvWekaAttributes.addElement(cpu);
		fvWekaAttributes.addElement(memory);
		fvWekaAttributes.addElement(power);
		// M. Fontanella - 11 Jan 2016 - begin	
		List<DataConsumption> cpuSample = service.sampleCPU(providerid, applicationid, deploymentid, vm);
		List<DataConsumption> memSample = service.sampleMemory(providerid, applicationid, deploymentid, vm);
		List<DataConsumption> powerSample = service.samplePower(providerid, applicationid, deploymentid, vm);
		// M. Fontanella - 11 Jan 2016 - end
			
			
		LOGGER.debug("Samples for the analysis ");
		LOGGER.debug("Samples for mem "+memSample.size());
		LOGGER.debug("Samples for cpu "+cpuSample.size());
		LOGGER.debug("Samples for mem "+powerSample.size());
			
		
		Instances isTrainingSet = new Instances("Powermodel", fvWekaAttributes, 0);

		isTrainingSet.setClassIndex(2);
		 
		Instance iExample;
		 
			
			 
		int min_set = 0;
		
		// TODO recalibrate samples based on discrepancy between data sets
		
		if (cpuSample.size()<memSample.size()){
			min_set=cpuSample.size();
		}else {
			min_set=memSample.size();
		}
		if (min_set>powerSample.size()){
			min_set = powerSample.size();
		}
		
		double[] cpu_timestamps= new double[min_set];
		double[] cpu_data=new double[min_set];
		double[] mem_timestamps= new double[min_set];
		double[] mem_data=new double[min_set];
		
		 
		LOGGER.info("Samples of analysis for model will be "+min_set);
		if (min_set==0){
			LOGGER.warn("Not enought samples "+min_set);
			return 0;
		}
		for (int i = 0; i<min_set;i++) {
			 iExample = new DenseInstance(3);
			 LOGGER.info("Sample CPU "+cpuSample.get(i).getVmcpu() + " MEM " + memSample.get(i).getVmmemory() + " POWER "+ powerSample.get(i).getVmpower() + " time "+ cpuSample.get(i).getTime());
			 iExample.setValue((Attribute)fvWekaAttributes.elementAt(0), new Float(cpuSample.get(i).getVmcpu()));
			 iExample.setValue((Attribute)fvWekaAttributes.elementAt(1), new Float(memSample.get(i).getVmmemory()));
			 iExample.setValue((Attribute)fvWekaAttributes.elementAt(2), new Float(powerSample.get(i).getVmpower()));
			 isTrainingSet.add(iExample);
			 cpu_timestamps[i]=cpuSample.get(i).getTime();
			 cpu_data[i]=cpuSample.get(i).getVmcpu();
			 mem_timestamps[i]=memSample.get(i).getTime();
			 mem_data[i]=memSample.get(i).getVmmemory();
			 
		}

		cpuinterpolator = new DataInterpolator();
		cpuinterpolator.buildmodel(cpu_timestamps, cpu_data);
		 
		meminterpolator = new DataInterpolator();
		meminterpolator.buildmodel(mem_timestamps, mem_data);
		
		double valuecpuforecast =  cpuinterpolator.estimate(forecasttime);
		LOGGER.info("Forecasted CPU"+valuecpuforecast+" at time "+forecasttime);
		double valuememforecast =  meminterpolator.estimate(forecasttime);
		LOGGER.info("Forecasted MEMORY"+valuememforecast+" at time "+forecasttime);
		iExample = new DenseInstance(3);
		iExample.setValue((Attribute)fvWekaAttributes.elementAt(0),valuecpuforecast);
		iExample.setValue((Attribute)fvWekaAttributes.elementAt(1), valuememforecast);

		isTrainingSet.add(iExample);
		 
		 LinearRegression model = new LinearRegression();
		 try {
			 model.buildClassifier(isTrainingSet);
		
			 LOGGER.debug("Model "+model);
	
			 Instance ukPower = isTrainingSet.lastInstance();
			 double powerest = model.classifyInstance(ukPower);
			 LOGGER.info("Power ("+ukPower+"): "+powerest);
			 LOGGER.info("############ Forecasting TERMINATED POWER WILL BE "+powerest + " at "+forecasttime+ "############");
			 return powerest;
		 } catch (Exception e) {
				e.printStackTrace();
		 }
	
		// M. Fontanella - 20 Jan 2016 - begin
		 LOGGER.info("############ Forecasting not performed on Provider "+providerid + " Application "+applicationid + " VM "+vm+ "############");
		// M. Fontanella - 20 Jan 2016 - end
		 
		 return estimation;
		

	}

}
