/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice;

import java.util.List;
import java.util.Vector;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.CpuFeatures;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.DataConsumptionHandler;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.CpuFeaturesHandler;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.AppRegistryMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.DataConsumptionMapper;

public class EnergyDataAggregatorServiceQueue {
	
	private static int MILLISEC=1000;

	private ApplicationRegistry applicationRegistry;
	private DataConsumptionHandler dataConsumptionHandler;	
	private CpuFeaturesHandler cpuFeaturesHandler;
	
	private static final Logger logger = Logger.getLogger(EnergyDataAggregatorServiceQueue.class);
	
	public void setApplicationRegistry( ApplicationRegistry applicationRegistry){
		this.applicationRegistry = applicationRegistry;
	}
	
	public void setDataRegistry(DataConsumptionHandler dataConsumptionHandler) {
		this.dataConsumptionHandler = dataConsumptionHandler;
	}
	
	public void setCpuFeaturesRegistry(CpuFeaturesHandler cpuFeaturesHandler) {
		this.cpuFeaturesHandler = cpuFeaturesHandler;
	}

	public double getEnergyFromVM(String providerid, String applicationid, String deployment, String vmid, String event, boolean enablePowerFromIaas) {
		if (vmid == null ){
			logger.info("No PaaS ID found from IaaS ID");
			return 0;
		}
		logger.info("Start computing for"+deployment+" "+ vmid);

		return integrateSamples(providerid, deployment, vmid, -1,-1, enablePowerFromIaas);
	}

	public double getMeasureInIntervalFromVM(Unit unit, String providerid, String applictionid, String deployment, String vmid, long start, long end, boolean enablePowerFromIaas) {
		
		if (vmid == null ){
			logger.info("No PaaS ID found from IaaS ID" +vmid);
			return 0;
		}
		SqlSession session = dataConsumptionHandler.getSession();
		DataConsumptionMapper dataConsumptionMapper = session.getMapper(DataConsumptionMapper.class);

		int samples = 0;

		if (enablePowerFromIaas)
			samples = dataConsumptionMapper.getSamplesBetweenTime( providerid, deployment, vmid, start/MILLISEC, end/MILLISEC);
		else
			samples = dataConsumptionMapper.getSamplesBetweenTimeVirtualPower( providerid, deployment, vmid, start/MILLISEC, end/MILLISEC);

		logger.info("Samples used for calculating "+vmid+" on this "+deployment + " value: "+samples+ " between "+start +" and "+end);
		if (samples == 0){
			logger.info("No samples available for the given interval "+start+ " to "+end+" estimating consumption from closest samples");
			
			long previoussampletime = 0L;
			long aftersampletime = 0L;
			if (enablePowerFromIaas) {
				previoussampletime = dataConsumptionMapper.getSampleTimeBefore(providerid, deployment, vmid, start/MILLISEC);
				aftersampletime = dataConsumptionMapper.getSampleTimeAfter(providerid, deployment, vmid, end/MILLISEC);
			}
			else {
				previoussampletime = dataConsumptionMapper.getSampleTimeBeforeVirtualPower(providerid, deployment, vmid, start/MILLISEC);
				aftersampletime = dataConsumptionMapper.getSampleTimeAfterVirtualPower(providerid, deployment, vmid, end/MILLISEC);
			}
			
			if (previoussampletime == 0){
				logger.info("Not enough samples - before the event interval" +start/MILLISEC);
				session.close();
				return 0;
			}
			if (aftersampletime == 0){
				logger.info("Not enough samples - after the event interval"+end/MILLISEC);
				session.close();
				return 0;
			}
			logger.info(previoussampletime);
			logger.info(aftersampletime);

			DataConsumption esfirst;
			DataConsumption eslast;
			if (enablePowerFromIaas) {
				esfirst = dataConsumptionMapper.getSampleAtTime(providerid, deployment,vmid,previoussampletime);
				eslast = dataConsumptionMapper.getSampleAtTime(providerid, deployment, vmid, aftersampletime);
			}
			else {
				esfirst = dataConsumptionMapper.getSampleAtTimeVirtualPower(providerid, deployment,vmid,previoussampletime);
				eslast = dataConsumptionMapper.getSampleAtTimeVirtualPower(providerid, deployment, vmid, aftersampletime);
			}
			
			logger.info("The lower bound at "+esfirst.getTime()+" value "+esfirst.getVmpower());
			logger.info("The upper bound at "+eslast.getTime()+" value "+eslast.getVmpower());
			
			if (unit == Unit.ENERGY){
				double avgpower = (esfirst.getVmpower()+eslast.getVmpower())/2;
				double energy = avgpower * ((end-start))/3600000;
				// TODO refine better this value 
				logger.info("This interval has consumed energy (Wh) "+energy);
				session.close();
				return energy;
			}else {
				double avgpower = (esfirst.getVmpower()+eslast.getVmpower())/2;
				logger.info("In this interval the average power (W) is "+avgpower);
				session.close();
				return avgpower;
			}
		} else{
			logger.info("Samples available for the given interval " + samples);
			if (samples ==1){
				logger.info("Only one sample available for the given interval "+start+ " to "+end+" estimating consumption from available samples");
				double avgpower = 0.0;
				if (enablePowerFromIaas)
					avgpower = dataConsumptionMapper.getPowerInIntervalForVM(providerid, deployment, vmid, start/MILLISEC,  end/MILLISEC);
				else
					avgpower = dataConsumptionMapper.getVirtualPowerInIntervalForVM(providerid, deployment, vmid, start/MILLISEC,  end/MILLISEC);
				logger.info("Power  "+avgpower);
				if (unit == Unit.ENERGY){
					double energy = avgpower * ((end-start))/3600000;
					logger.info("This interval has consumed energy Wh "+energy);
					session.close();
					return energy;
				}else {
					session.close();
					return avgpower;
				}
			}
		}
		if (unit == Unit.ENERGY){
			session.close();
			return integrateSamples(providerid, deployment, vmid, start, end, enablePowerFromIaas);
		} else {
			double result = 0.0;

			if (enablePowerFromIaas)
				result = dataConsumptionMapper.getPowerInIntervalForVM(providerid, deployment, vmid, start/MILLISEC, end/MILLISEC);
			else
				result = dataConsumptionMapper.getVirtualPowerInIntervalForVM(providerid, deployment, vmid, start/MILLISEC, end/MILLISEC);

			logger.info("######### Avg power is "+result);
			session.close();
			return result;
		}
		
	}
	
	public DataConsumption getLastPowerSampleFromVM(String providerid, String applicationid, String deployment, String vmid, boolean enablePowerFromIaas) {

		if (vmid == null ){
			logger.info("No PaaS ID found from IaaS ID");
			return null;
		}
		logger.info("Start computing Wh over a period of time");
		SqlSession session = dataConsumptionHandler.getSession();
		DataConsumptionMapper dataConsumptionMapper = session.getMapper(DataConsumptionMapper.class);
		DataConsumption dc;

		if (enablePowerFromIaas)
			dc = dataConsumptionMapper.getLastSample(providerid, deployment,vmid);
		else
			dc = dataConsumptionMapper.getLastSampleVirtualPower(providerid, deployment,vmid);

		session.close();
		return dc;
		
		
	}
	
	private double integrateSamples(String provider, String deployment, String vmid, long start, long end, boolean enablePowerFromIaas){

		List<DataConsumption> consumptionList;
		if (start>0){
			logger.info("Start computing Wh over a period of time");
			SqlSession session = dataConsumptionHandler.getSession();
			DataConsumptionMapper dataConsumptionMapper = session.getMapper(DataConsumptionMapper.class);
			if (enablePowerFromIaas)
				consumptionList = dataConsumptionMapper.getDataSamplesVM(provider, deployment, vmid, start/MILLISEC, end/MILLISEC);
			else
				consumptionList = dataConsumptionMapper.getDataSamplesVMVirtualPower(provider, deployment, vmid, start/MILLISEC, end/MILLISEC);
			session.close();
			
		}else {
			logger.info("Start computing Wh overall samples");
			SqlSession session = dataConsumptionHandler.getSession();
			DataConsumptionMapper dataConsumptionMapper = session.getMapper(DataConsumptionMapper.class);
			if (enablePowerFromIaas)
				consumptionList = dataConsumptionMapper.selectByVm(provider, deployment, vmid);
			else
				consumptionList = dataConsumptionMapper.selectByVmVirtualPower(provider, deployment, vmid);
			session.close();
		}
		DataConsumption previousSample=null;
		double accumulatedEnergy = 0;
		logger.info("Start computing Wh "+consumptionList.size());
		double partial = 0;
		for (DataConsumption sample: consumptionList){
			if (previousSample!=null){
				partial = integrate(previousSample.getVmpower(),sample.getVmpower(),previousSample.getTime(),sample.getTime());
				accumulatedEnergy = accumulatedEnergy + partial;
				
			} 
			previousSample = sample;
			
		}
		logger.info("total Wh is "+accumulatedEnergy);
		return accumulatedEnergy;
			
	}
	
	public List<DataConsumption> sampleMemory(String providerid, String applicationid, String deployment, String vmid, boolean enablePowerFromIaas){

		if (vmid == null ){
			logger.info("No PaaS ID found from IaaS ID");
			return null;
		}
		SqlSession session = dataConsumptionHandler.getSession();
		DataConsumptionMapper dataConsumptionMapper = session.getMapper(DataConsumptionMapper.class);
		List<DataConsumption> dc;
		if (enablePowerFromIaas)
			dc = dataConsumptionMapper.getMemory(providerid, deployment, vmid);
		else
			dc = dataConsumptionMapper.getVirtualMemory(providerid, deployment, vmid);

		session.close();
		return  dc;
		
		
	}

	public List<DataConsumption> sampleCPU(String providerid, String applicationid, String deployment, String vmid, boolean enablePowerFromIaas){

		if (vmid == null ){
			logger.info("No PaaS ID found from IaaS ID");
			return null;
		}
		SqlSession session = dataConsumptionHandler.getSession();
		DataConsumptionMapper dataConsumptionMapper = session.getMapper(DataConsumptionMapper.class);
		List<DataConsumption> dc;
		if (enablePowerFromIaas)
			dc = dataConsumptionMapper.getCPUs(providerid, deployment, vmid);
		else
			dc = dataConsumptionMapper.getVirtualCPUs(providerid, deployment, vmid);

		session.close();
		return  dc;
		
	}	
	
	public List<DataConsumption> samplePower(String providerid, String applicationid, String deployment, String vmid, boolean enablePowerFromIaas){

		if (vmid == null ){
			logger.info("No PaaS ID found from IaaS ID");
			return null;
		}
		SqlSession session = dataConsumptionHandler.getSession();
		DataConsumptionMapper dataConsumptionMapper = session.getMapper(DataConsumptionMapper.class);
		List<DataConsumption> dc;

		if (enablePowerFromIaas)
			dc = dataConsumptionMapper.getPower(providerid, deployment, vmid);
		else
			dc = dataConsumptionMapper.getVirtualPower(providerid, deployment, vmid);

		session.close();
		return  dc;
		
	}	
	
	public List<DataConsumption> sampleMeasurements(String providerid, String applicationid, String deployment, String vmid, long start,long end,long interval,boolean enablePowerFromIaas){

		if (vmid == null ){
			logger.info("No PaaS ID found from IaaS ID");
			return null;
		}
		SqlSession session = dataConsumptionHandler.getSession();
		DataConsumptionMapper dataConsumptionMapper = session.getMapper(DataConsumptionMapper.class);		
		List<DataConsumption>  result;

		if (enablePowerFromIaas)
			result = dataConsumptionMapper.getDataSamplesVM(providerid, deployment, vmid, start, end);
		else
			result = dataConsumptionMapper.getDataSamplesVMVirtualPower(providerid, deployment, vmid, start, end);

		session.close();
		List<DataConsumption> resampledresult = new Vector<DataConsumption>(); 
		if (result==null)return resampledresult;
		if (result.size()==0)return resampledresult;
		long currenttime = 0;
		int iteration = 0;
		int pointer=0;
		double last_powerval=0;
		double last_ts=start;
		currenttime = start;
		double aggr_energy = 0;
		while (currenttime < end){
			if (iteration == 0){
				DataConsumption as = new DataConsumption();
				as.setVmcpu(result.get(0).getVmcpu());
				as.setVmpower(result.get(0).getVmpower());
				as.setVmenergy(0);
				as.setTime(start);
				as.setVmid(vmid);
				last_ts=currenttime;
				last_powerval=result.get(0).getVmpower();
				iteration++;
				resampledresult.add(as);
			}else { 
				DataConsumption as = new DataConsumption();
				while((result.get(pointer).getTime()<currenttime)){
					pointer++;	
					if (pointer==result.size())break;
				}
				if (pointer<result.size()){
					as.setVmcpu(result.get(pointer).getVmcpu());
					as.setVmpower(result.get(pointer).getVmpower());
					as.setTime(currenttime);
					as.setProviderid(providerid);
					as.setVmid(vmid);
					as.setApplicationid(applicationid);
					double part_energy = ((last_powerval+result.get(pointer).getVmpower())/2)*(currenttime-last_ts)/3600000;
					aggr_energy = aggr_energy +part_energy;
					as.setVmenergy(aggr_energy);
					last_ts=currenttime;
					last_powerval=result.get(pointer).getVmpower();
					iteration++;
					resampledresult.add(as);
				}
			}
			currenttime = currenttime + interval*1000;
			
		}
		return resampledresult;
	}
	
	
	public double getPowerPerVM(String provider, String deployment, String vmid, boolean enablePowerFromIaas){

		if (vmid == null ){
			logger.info("No PaaS ID found from IaaS ID");
			return 0;
		}
		logger.info("Start computing Wh over a period of time");
		SqlSession session = dataConsumptionHandler.getSession();
		DataConsumptionMapper dataConsumptionMapper = session.getMapper(DataConsumptionMapper.class);
		double power = 0.0;
		
		if (enablePowerFromIaas)
			power = dataConsumptionMapper.getAvgPowerForVM(provider, deployment, vmid);
		else
			power = dataConsumptionMapper.getAvgVirtualPowerForVM(provider, deployment, vmid);
		
		session.close();
		return power;
	}
	
	// TODO to be removed, for the moment is the only way to map the IaaS VM with the PaaS VM
	public String translatePaaSFromIaasID(String providerid, String deployid, String paasvmid){

		logger.info(" I will translate for this "+deployid+" the paas id " + paasvmid);
		
		SqlSession session = applicationRegistry.getSession();
		AppRegistryMapper registryMapper = session.getMapper(AppRegistryMapper.class);
		String iaasVmId = registryMapper.selectFromIaaSID(providerid,deployid,paasvmid);
		logger.info(" I translated to " + paasvmid + " the iaas id " + iaasVmId);
		session.close();
		return iaasVmId;
	}
	
	private double integrate(double powera,double powerb, long timea,long timeb){
		
		double integral = Math.abs(timeb-timea)*(powera+powerb)*(0.5/3600);
		if 	(Math.abs(timeb-timea)>3600) {
			logger.info("Delta "+powera+" "+powerb);
			logger.info("Delta between samples greater than one hour! "+integral + " "+(powera+powerb)+ " "+Math.abs(timeb-timea));
		}
		return 	integral;
		
		
	}
	

	

}
