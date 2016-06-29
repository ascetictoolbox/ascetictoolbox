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
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.impl;

import java.util.List;
// M. Fontanella - 30 Mar 2016 - Begin
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Iterator;
import java.util.TreeMap;
// import java.sql.Timestamp;
// import java.io.BufferedReader;
// import java.io.BufferedWriter;
// import java.io.FileReader;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.util.LinkedList;

import org.apache.log4j.Logger;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.SupervisedLearning;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.MomentumBackpropagation;
// M. Fontanella - 30 Mar 2016 - End


import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorServiceQueue;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.PredictorInterface;

public class EMNeuralPredictor implements PredictorInterface{

	// M. Fontanella - 30 Mar 2016 - Begin
	private final static Logger LOGGER = Logger.getLogger(PredictorInterface.class.getName());
	private EnergyDataAggregatorServiceQueue service;
	
	private long lastSampleTimestamp = 0;
	private long sampleIntervalAverage = 0;
	private double maxCPU = 0;
	private double minCPU = Double.MAX_VALUE;
	private double maxMemory = 0;
	private double minMemory = Double.MAX_VALUE;
	private double maxPower = 0;
	private double minPower = Double.MAX_VALUE;
	
	
	public enum PredictorObject {
	CPU,MEMORY,POWER
	}
	// M. Fontanella - 30 Mar 2016 - End
	
	// M. Fontanella - 26 Apr 2016 - begin
	// M. Fontanella - 26 Apr 2016 - begin
	@Override
	public double estimate(String providerid, String applicationid,	String deploymentid, List<String> vmids, String eventid, Unit unit, long forecasttime, boolean enablePowerFromIaas) {
	// public double estimate(String providerid, String applicationid,	String deploymentid, List<String> vmids, String eventid, Unit unit, long timelater, boolean enablePowerFromIaas) {
	// M. Fontanella - 23 Jun 2016 - end
		// M. Fontanella - 26 Apr 2016 - end
		// M. Fontanella - 30 Mar 2016 - Begin
		// only power then from power get energy estimation
		double cur_power = 0;
		int count=0;
		for (String vmid :vmids){
			count++;
			// M. Fontanella - 26 Apr 2016 - begin
			// M. Fontanella - 26 Apr 2016 - begin
			cur_power = cur_power + estimate( providerid,  applicationid,	 deploymentid,  vmid,  eventid,  unit,  forecasttime, enablePowerFromIaas) ;
			// cur_power = cur_power + estimate( providerid,  applicationid,	 deploymentid,  vmid,  eventid,  unit,  timelater, enablePowerFromIaas) ;
			// M. Fontanella - 23 Jun 2016 - end
			// M. Fontanella - 26 Apr 2016 - end
		}
		
		if (count>0)return cur_power/count;
		// M. Fontanella - 30 Mar 2016 - End
		return 0;
	}

	@Override
	public void setEnergyService(EnergyDataAggregatorServiceQueue service) {
		// M. Fontanella - 30 Mar 2016 - Begin
		this.service = service;
		// M. Fontanella - 30 Mar 2016 - End
		
	}

	// M. Fontanella - 26 Apr 2016 - begin
	// M. Fontanella - 23 Jun 2016 - begin	
	@Override
	public double estimate(List<DataConsumption> samples, Unit unit, long forecasttime, boolean enablePowerFromIaas) {
	// public double estimate(List<DataConsumption> samples, Unit unit, long timelater, boolean enablePowerFromIaas) {
	// M. Fontanella - 23 Jun 2016 - end
	// M. Fontanella - 26 Apr 2016 - end
		// TODO Auto-generated method stub
		return 0;
	}

	// M. Fontanella - 26 Apr 2016 - begin
	// M. Fontanella - 23 Jun 2016 - begin	
	@Override
	public double estimate(String providerid, String applicationid,	String deploymentid, String vm, String eventid, Unit unit, long forecasttime, boolean enablePowerFromIaas) {
	// public double estimate(String providerid, String applicationid,	String deploymentid, String vm, String eventid, Unit unit, long timelater, boolean enablePowerFromIaas) {
	// M. Fontanella - 23 Jun 2016 - end
	// M. Fontanella - 26 Apr 2016 - end
		// M. Fontanella - 30 Mar 2016 - Begin
		
		// Neuroph parameters
		int maxIterations = 1000;
		double maxError = 0.003;// (0.00001) or (0.001)
        double learningRate = 0.1;// (0.5) or (0.7)
        int slidingWindowSize = 9; // = 4 * 3(CPU, Memory, Power)
		double estimation=0;
		// M. Fontanella - 23 Jun 2016 - begin
		/*
		Date current = new Date();
				
		long end = current.getTime();
		// M. Fontanella - 26 May 2016 - begin
		//long end = 1459787827000L; //test #1
		//long end = 1459960847000L; //test #2
		//long end = 1461739092000L; //test #3
		// M. Fontanella - 26 May 2016 - end		
		LOGGER.info("Forecaster now is "+end);
		// add after millisec conversion the time of the forecast
		long forecasttime = end/1000 + (timelater);
		*/
		forecasttime = forecasttime/1000;
		// M. Fontanella - 23 Jun 2016 - end
		
		LOGGER.info("Forecaster Provider "+providerid + " Application "+applicationid + " VM "+vm + " at time "+forecasttime);
		LOGGER.info("############ Forecasting STARTED FOR Provider "+providerid + " Application "+applicationid + " VM "+vm+ "############");
		
		// M. Fontanella - 26 Apr 2016 - begin
		double[][] valuesRow = dataAggregation(providerid, applicationid, deploymentid, vm, eventid, maxIterations, enablePowerFromIaas);
		// M. Fontanella - 26 Apr 2016 - end
				
		maxIterations = valuesRow.length;
		LOGGER.info("Samples of analysis for model will be " + valuesRow.length);
		// M. Fontanella - 16 Jun 2016 - begin
		// if (valuesRow.length == 0){
		if (valuesRow.length <= 1){
		// M. Fontanella - 16 Jun 2016 - end
			LOGGER.warn("Not enought samples " + valuesRow.length);
			LOGGER.info("############ Forecasting not performed on Provider "+providerid + " Application "+applicationid + " VM "+vm+ "############");
			return 0;
		}
		
		if (this.sampleIntervalAverage == 0){
			LOGGER.warn("Invalid sample interval average (0)");
			return 0;
		}
		
		defineNormalizationParam(valuesRow);
		LOGGER.debug("minCPU=" + this.minCPU + " maxCPU=" + this.maxCPU + " minMemory=" + this.minMemory + " maxMemory=" + this.maxMemory + " minPower=" + this.minPower + " maxPower=" + this.maxPower);
		
		// Cycles Number
		int cyclesNumber = (int )((forecasttime - this.lastSampleTimestamp) / this.sampleIntervalAverage);
		int cyclesNumberReminder = (int )((forecasttime - this.lastSampleTimestamp) % this.sampleIntervalAverage);
		if (cyclesNumberReminder > 0)
				cyclesNumber++;
		LOGGER.info("Cycles Number : " + cyclesNumber);
		
		int counter, counterInput;
				
		// CPU, Memory, Power: Training and setup for first estimations
		NeuralNetwork<BackPropagation> neuralNetwork = new MultiLayerPerceptron(slidingWindowSize, 2 * slidingWindowSize + 1, 3);
		SupervisedLearning learningRule = neuralNetwork.getLearningRule();
		learningRule.setMaxError(maxError);
		learningRule.setLearningRate(learningRate);
		learningRule.setMaxIterations(maxIterations);
		learningRule.addListener(new LearningEventListener() {			
			public void handleLearningEvent(LearningEvent learningEvent) {
				SupervisedLearning rule = (SupervisedLearning) learningEvent.getSource();
				LOGGER.debug("CPU, Memory, Power - Network error for interation " + rule.getCurrentIteration() + ": " + rule.getTotalNetworkError());
			}
		});
				
		DataSet trainingSet = trainingImportObjectFromMatrix(valuesRow, slidingWindowSize, 3);
		neuralNetwork.learn(trainingSet);
		// neuralNetwork.save(neuralNetworkModelFilePath);
		// trainingSet.saveAsTxt("/temp/CPU_Memory_Power.txt","|");
		
		double [] input = new double[slidingWindowSize];
		double [] inputDenorm = new double[slidingWindowSize];
		double prevOutputCPU = 0.0;
		double currOutputCPU = 0.0;
		double calcOutputCPU = 0.0;
		double prevOutputMemory = 0.0;
		double currOutputMemory = 0.0;
		double calcOutputMemory = 0.0;
		double prevOutputPower = 0.0;
		double currOutputPower = 0.0;
		double calcOutputPower = 0.0;
						
		inputDenorm = lastTrainingRow(valuesRow, slidingWindowSize, 3);
		for (counterInput = 0; counterInput < slidingWindowSize; counterInput++) {
			switch (counterInput % 3) {
				case 0: //CPU
					input[counterInput] = normalizeValue(inputDenorm[counterInput], PredictorObject.CPU);
					break;
				case 1: //Memory
					input[counterInput] = normalizeValue(inputDenorm[counterInput], PredictorObject.MEMORY);
					break;
				case 2: //Power
					input[counterInput] = normalizeValue(inputDenorm[counterInput], PredictorObject.POWER);
					break;
			}
		}
		
		currOutputCPU    = normalizeValue(inputDenorm[slidingWindowSize-3], PredictorObject.CPU);
		currOutputMemory = normalizeValue(inputDenorm[slidingWindowSize-2], PredictorObject.MEMORY);
		currOutputPower  = normalizeValue(inputDenorm[slidingWindowSize-1], PredictorObject.POWER);
		
		LOGGER.info("LAST SAMPLE - Timestamp: " + this.lastSampleTimestamp +
					" CPU:"    + inputDenorm[slidingWindowSize-3] +
					" Memory:" + inputDenorm[slidingWindowSize-2] +
					" Power:"  + inputDenorm[slidingWindowSize-1]);
        
		double[] networkOutput;
		
		for (counter = 0; counter < cyclesNumber; counter++) {
			
			// CPU, Memory, Power: estimation
			// NeuralNetwork neuralNetGlobalPower = NeuralNetwork.createFromFile(neuralNetGlobalPowerModelFilePath);
			prevOutputCPU    = currOutputCPU;
			prevOutputMemory = currOutputMemory;
			prevOutputPower  = currOutputPower;
			neuralNetwork.setInput(input);
			neuralNetwork.calculate();
			networkOutput = neuralNetwork.getOutput();
			currOutputCPU    = networkOutput[0];
			currOutputMemory = networkOutput[1];
			currOutputPower  = networkOutput[2];			
			
			LOGGER.debug("FORECAST - Timestamp: " + (this.lastSampleTimestamp + (this.sampleIntervalAverage * (counter + 1))) +
						" CPU:"    + deNormalizeValue(currOutputCPU, PredictorObject.CPU) +
						" Memory:" + deNormalizeValue(currOutputMemory, PredictorObject.MEMORY) +
						" Power:"  + deNormalizeValue(currOutputPower, PredictorObject.POWER));					
								
			// CPU, Memory, Power: setup for next estimations
			// input array update: 
			// e.g.
			// from: "valCPU1, valMemory1, ValPower1, valCPU2, valMemory2, ValPower2, ..., valCPUX, valMemoryX, ValPowerX" 
			// to:   "valCPU2, valMemory2, ValPower2, ..., valCPUX, valMemoryX, ValPowerX, ,currOutputGlobalCPU, currOutputGlobalMemory, currOutputGlobalPower"			
				
			for (counterInput = 0; counterInput < (slidingWindowSize-3); counterInput++) {				
				input[counterInput] = input[counterInput+3];
				inputDenorm[counterInput] = inputDenorm[counterInput+3];				
			}
			input[slidingWindowSize-3] = currOutputCPU;
			input[slidingWindowSize-2] = currOutputMemory;
			input[slidingWindowSize-1] = currOutputPower;
			inputDenorm[slidingWindowSize-3] = deNormalizeValue(currOutputCPU,    PredictorObject.CPU);
			inputDenorm[slidingWindowSize-2] = deNormalizeValue(currOutputMemory, PredictorObject.MEMORY);
			inputDenorm[slidingWindowSize-1] = deNormalizeValue(currOutputPower,  PredictorObject.POWER);
		}
				
		// adjusting value if timelater is not a multiple of the sampling period
		if (cyclesNumberReminder != 0) {
			
			calcOutputCPU    = prevOutputCPU    + ((currOutputCPU    - prevOutputCPU)    * ((double )cyclesNumberReminder / (double )this.sampleIntervalAverage));
			calcOutputMemory = prevOutputMemory + ((currOutputMemory - prevOutputMemory) * ((double )cyclesNumberReminder / (double )this.sampleIntervalAverage));
			calcOutputPower  = prevOutputPower  + ((currOutputPower  - prevOutputPower)  * ((double )cyclesNumberReminder / (double )this.sampleIntervalAverage));
			estimation = deNormalizeValue(calcOutputPower, PredictorObject.POWER);
			LOGGER.debug("FORECAST Adjusting - Timestamp: " + forecasttime +
						" CPU:"    + deNormalizeValue(calcOutputCPU, PredictorObject.CPU) +
						" Memory:" + deNormalizeValue(calcOutputMemory, PredictorObject.MEMORY) +
						" Power:"  + deNormalizeValue(calcOutputPower, PredictorObject.POWER));	
		} else {
			estimation = deNormalizeValue(currOutputPower, PredictorObject.POWER);
			
			/* MAXIM
			LOGGER.info("FORECAST NoAdjusting - Timestamp: " + forecasttime +
					" CPU:"    + deNormalizeValue(currOutputCPU, PredictorObject.CPU) +
					" Memory:" + deNormalizeValue(currOutputMemory, PredictorObject.MEMORY) +
					" Power:"  + deNormalizeValue(currOutputPower, PredictorObject.POWER));	//MAXIM
			MAXIM */
		}
		
		LOGGER.info("############ Forecasting TERMINATED POWER WILL BE "+estimation + " at "+forecasttime+ "############");
		
		return estimation;
		
	}

	
	// M. Fontanella - 26 Apr 2016 - begin
	public double[][] dataAggregation(String providerid, String applicationid, String deploymentid, String vm, String eventid, int maxIterations, boolean enablePowerFromIaas) {
	// M. Fontanella - 26 Apr 2016 - end
		
		int counter;
		boolean isnull = false;
		double value;
		long timestamp;
		int counterALL = 0;
		
		// M. Fontanella - 20 Jun 2016 - begin	
		List<DataConsumption> cpuSample = service.sampleCPU(providerid, applicationid, deploymentid, vm, enablePowerFromIaas);
		List<DataConsumption> memSample = service.sampleMemory(providerid, applicationid, deploymentid, vm, enablePowerFromIaas);
		// M. Fontanella - 20 Jun 2016 - end
		// M. Fontanella - 26 Apr 2016 - begin
		List<DataConsumption> powerSample = service.samplePower(providerid, applicationid, deploymentid, vm, enablePowerFromIaas);
		// M. Fontanella - 26 Apr 2016 - end
		
		LOGGER.debug("Samples for the analysis ");
		
		if (memSample == null) {
			isnull = true;
			LOGGER.debug("Samples for mem 0");
			
		}
		else
			LOGGER.info("Samples for mem "+memSample.size());
		
		if (cpuSample == null) {
			isnull = true;
			LOGGER.debug("Samples for cpu 0");			
		}
		else
			LOGGER.info("Samples for cpu "+cpuSample.size());		
				
		if (powerSample == null) {
			isnull = true;
			LOGGER.debug("Samples for power 0");
			
		}
		else
			LOGGER.info("Samples for power "+powerSample.size());
		
		if (isnull == true) {
			double [][] valuesRow = new double[0][4];
			return(valuesRow);
		}		
		
		// CPU
		HashMap<Long, Double> hmapCPU = new HashMap<Long, Double>();
				
		for (counter = 0; counter<cpuSample.size();counter++) {
			
			value = cpuSample.get(counter).getVmcpu();
			timestamp=cpuSample.get(counter).getTime();
			hmapCPU.put(timestamp, value);
		}
		
		LOGGER.debug("CPU Before Sorting: ");
		Set<Map.Entry<Long, Double>> setCPUBeforeSort = hmapCPU.entrySet();
		Iterator<Entry<Long, Double>> iteratorCPUBeforeSort = setCPUBeforeSort.iterator();
		counter = 0;
		while(iteratorCPUBeforeSort.hasNext()) {
			Map.Entry<Long, Double> meCPUBeforeSort = (Map.Entry<Long, Double>)iteratorCPUBeforeSort.next();
			timestamp = (long) meCPUBeforeSort.getKey();
            value = (double) meCPUBeforeSort.getValue();
            if (counter < 10)
            	LOGGER.debug("Key " + timestamp + ": " + value);
			counter++;			
        }
        
        
        Map<Long, Double> mapCPU = new TreeMap<Long, Double>(hmapCPU);
        
        Set<Map.Entry<Long, Double>> setCPUSort = mapCPU.entrySet();
        Iterator<Entry<Long, Double>> iteratorCPUSort = setCPUSort.iterator();
        
        LOGGER.debug("CPU After Sorting: ");
        
        long [] timestampsCPU = new long[counter];
		double [] valuesCPU = new double[counter];
		int counterCPU = 0;
        while(iteratorCPUSort.hasNext()) {
            Map.Entry<Long, Double> meCPUSort = (Map.Entry<Long, Double>)iteratorCPUSort.next();
            timestamp = (long) meCPUSort.getKey();
            value = (double) meCPUSort.getValue();
            timestampsCPU[counterCPU] = timestamp;
			valuesCPU[counterCPU] = value;
			if (counterCPU < 10)
				LOGGER.debug("Key " + timestamp + ": " + value);
			counterCPU++;
		}
                
 
		// Memory
		HashMap<Long, Double> hmapMemory = new HashMap<Long, Double>();
						
		for (counter = 0; counter<memSample.size();counter++) {
			
			value = memSample.get(counter).getVmmemory();
			timestamp=memSample.get(counter).getTime();
			hmapMemory.put(timestamp, value);
		}
		
		LOGGER.debug("Memory Before Sorting: ");
		Set<Map.Entry<Long, Double>> setMemoryBeforeSort = hmapMemory.entrySet();
		Iterator<Entry<Long, Double>> iteratorMemoryBeforeSort = setMemoryBeforeSort.iterator();
		counter = 0;
		while(iteratorMemoryBeforeSort.hasNext()) {
			Map.Entry<Long, Double> meMemoryBeforeSort = (Map.Entry<Long, Double>)iteratorMemoryBeforeSort.next();
			timestamp = (long) meMemoryBeforeSort.getKey();
            value = (double) meMemoryBeforeSort.getValue();
            if (counter < 10)
            	LOGGER.debug("Key " + timestamp + ": " + value);
			counter++;			
        }
        
        
        Map<Long, Double> mapMemory = new TreeMap<Long, Double>(hmapMemory);
        
        Set<Entry<Long, Double>> setMemorySort = mapMemory.entrySet();
        Iterator<Entry<Long, Double>> iteratorMemorySort = setMemorySort.iterator();
        
        LOGGER.debug("Memory After Sorting: ");
        
        long [] timestampsMemory = new long[counter];
		double [] valuesMemory = new double[counter];
		int counterMemory = 0;
        while(iteratorMemorySort.hasNext()) {
            Map.Entry<Long, Double> meMemorySort = (Map.Entry<Long, Double>)iteratorMemorySort.next();
            timestamp = (long) meMemorySort.getKey();
            value = (double) meMemorySort.getValue();
			timestampsMemory[counterMemory] = timestamp;
			valuesMemory[counterMemory] = value;			
			if (counterMemory < 10)
				LOGGER.debug("Key " + timestamp + ": " + value);
			counterMemory++;
		}

		
		// Power
		HashMap<Long, Double> hmapPower = new HashMap<Long, Double>();
				
		for (counter = 0; counter<powerSample.size();counter++) {
			
			value = powerSample.get(counter).getVmpower();
			timestamp=powerSample.get(counter).getTime();
			hmapPower.put(timestamp, value);
		}
		
		LOGGER.debug("Power Before Sorting: ");
		Set<Map.Entry<Long, Double>> setPowerBeforeSort = hmapPower.entrySet();
		Iterator<Entry<Long, Double>> iteratorPowerBeforeSort = setPowerBeforeSort.iterator();
		counter = 0;
		while(iteratorPowerBeforeSort.hasNext()) {
			Map.Entry<Long, Double> mePowerBeforeSort = (Map.Entry<Long, Double>)iteratorPowerBeforeSort.next();
			timestamp = (long) mePowerBeforeSort.getKey();
            value = (double) mePowerBeforeSort.getValue();
            if (counter < 10)
            	LOGGER.debug("Key " + timestamp + ": " + value);
			counter++;			
        }
        
        
        Map<Long, Double> mapPower = new TreeMap<Long, Double>(hmapPower);
        
        Set<Entry<Long, Double>> setPowerSort = mapPower.entrySet();
        Iterator<Entry<Long, Double>> iteratorPowerSort = setPowerSort.iterator();
        
        LOGGER.debug("Power After Sorting: ");
        
		long [] timestampsPower = new long[counter];
		double [] valuesPower = new double[counter];
		int counterPower = 0;
        while(iteratorPowerSort.hasNext()) {
            Map.Entry<Long, Double> mePowerSort = (Map.Entry<Long, Double>)iteratorPowerSort.next();
            timestamp = (long) mePowerSort.getKey();
            value = (double) mePowerSort.getValue();
			timestampsPower[counterPower] = timestamp;
			valuesPower[counterPower] = value;			
			if (counterPower < 10)
				LOGGER.debug("Key " + timestamp + ": " + value);
			counterPower++;
		}
		
		
		// Aggregation
		int counterTmp;
		if (counterCPU > counterMemory) 
			if (counterCPU > counterPower)
				counterTmp = counterCPU;
			else
				counterTmp = counterPower;
		else
			if (counterMemory > counterPower)
				counterTmp = counterMemory;
			else
				counterTmp = counterPower;		
		
		double [][] valuesRowTmp = new double[counterTmp][4];   
		
		counter=0;
		int counter1=0;
		int counter2=0;
		int counter3=0;
		
		/*
		for (counter1=0; counter1<counterCPU; counter1++) {
			
			for (; counter2<counterMemory && timestampsMemory[counter2] <=timestampsCPU[counter1] ; counter2++) {
				
				if (timestampsMemory[counter2] == timestampsCPU[counter1]) {
					
					for (; counter3<counterPower && timestampsPower[counter3] <=timestampsCPU[counter1] ; counter3++) {
						
						if (timestampsPower[counter3] == timestampsCPU[counter1]) {
							valuesRowTmp[counter][0] = (double )timestampsCPU[counter1];
							valuesRowTmp[counter][1] = valuesCPU[counter1]; 
							valuesRowTmp[counter][2] = valuesMemory[counter2]; 
							valuesRowTmp[counter][3] = valuesPower[counter3];
							counter++;
						}
					}
				}
			}
			
		} 
		 */
		
		long intervalStartCPU = 0L;
		long intervalStopCPU = 0L;
				
		for (counter1=0; counter1 < counterCPU; counter1++) {
			
			intervalStartCPU = timestampsCPU[counter1];
			if (counter1 < (counterCPU-1))
				intervalStopCPU = timestampsCPU[counter1+1];
			else
				intervalStopCPU = timestampsCPU[counter1] + timestampsCPU[counter1] - timestampsCPU[counter1-1];
			
			for (; counter2 < counterMemory && timestampsMemory[counter2] < intervalStopCPU; counter2++) {				
								
				if (timestampsMemory[counter2] >= intervalStartCPU) {
					
					for (; counter3<counterPower && timestampsPower[counter3] < intervalStopCPU ; counter3++) {
						
						if (timestampsPower[counter3] >= intervalStartCPU) {
							valuesRowTmp[counter][0] = (double )intervalStartCPU;
							valuesRowTmp[counter][1] = valuesCPU[counter1]; 
							valuesRowTmp[counter][2] = valuesMemory[counter2]; 
							valuesRowTmp[counter][3] = valuesPower[counter3];
							counter++;
							// M. Fontanella - 14 Jun 2016 - begin
							break;
							// M. Fontanella - 14 Jun 2016 - end
						}
					}
					
					// M. Fontanella - 14 Jun 2016 - begin
					break;
					// M. Fontanella - 14 Jun 2016 - end
				}
			}
			
		}
				
		int firstRow = 0;
		
		if (counter >= maxIterations) {
			counterALL = maxIterations;
			firstRow = counter - maxIterations;
		}
		else {
			counterALL = counter;
			firstRow = 0;
		}
		
		double [][] valuesRow = new double[counterALL][4];
		LOGGER.debug("Aggregated data: ");
		
		for (counter1=0; counter1<counterALL; counter1++) {
			
			valuesRow[counter1][0] = valuesRowTmp[counter1 + firstRow][0];
			valuesRow[counter1][1] = valuesRowTmp[counter1 + firstRow][1]; 
			valuesRow[counter1][2] = valuesRowTmp[counter1 + firstRow][2]; 
			valuesRow[counter1][3] = valuesRowTmp[counter1 + firstRow][3];
				
			// valuesRow[counter1] = valuesRowTmp[counter1 + firstRow];
			if (counter1 < 10)
				LOGGER.debug("#" + (counter1) + ": " + ((long )valuesRow[counter1][0]) + " "+ valuesRow[counter1][1] + " "+ valuesRow[counter1][2] + " "+ valuesRow[counter1][3]);				
		}
		
		
		LOGGER.debug("Number of aggregated rows: " + counterALL);
		
		if (counterALL > 0) {		
			
			this.lastSampleTimestamp = (long )valuesRow[counterALL-1][0];
			// M. Fontanella - 16 Jun 2016 - begin
			//this.sampleIntervalAverage = this.lastSampleTimestamp - (long )valuesRow[counterALL-2][0];
			if (counterALL > 1)
				this.sampleIntervalAverage = (this.lastSampleTimestamp - (long )valuesRow[0][0])/(counterALL-1);
			else
				this.sampleIntervalAverage = 0;
			// M. Fontanella - 16 Jun 2016 - end
		
			LOGGER.info("Last Sample Timestamp / Average Interval: " + this.lastSampleTimestamp + " / " + this.sampleIntervalAverage);

		}
				
        return(valuesRow);

	}
	
	public void defineNormalizationParam(double [][] valueRow) {
		
		int counter;
		double valueCPU, valueMemory, valuePower;
		
		for (counter=0; counter < valueRow.length; counter++) {
			
			valueCPU = valueRow[counter][1];
			valueMemory = valueRow[counter][2];
			valuePower = valueRow[counter][3];
			
			if (valueCPU > this.maxCPU)
				this.maxCPU = valueCPU;
			if (valueCPU < this.minCPU)
				this.minCPU = valueCPU;
			
			if (valueMemory > this.maxMemory)
				this.maxMemory = valueMemory;
			if (valueMemory < this.minMemory)
				this.minMemory = valueMemory;
			
			if (valuePower > this.maxPower)
				this.maxPower = valuePower;
			if (valuePower < this.minPower)
				this.minPower = valuePower;			
		}
	}

	

	public DataSet trainingImportObjectFromMatrix(double[][] valuesRow, int inputsCount, int outputsCount) {		
		
		int inputsTriple = inputsCount / 3;
		int outputsTriple = outputsCount / 3;
		
        DataSet trainingSet = new DataSet(inputsCount, outputsCount);
		
        LOGGER.debug("Global Training Set: ");
        
        for (int i = 0; i < valuesRow.length - inputsTriple; i++) {
            ArrayList<Double> inputs = new ArrayList<Double>();
            for (int j = i; j < i + inputsTriple; j++) {
            	if (i < 10)
    				LOGGER.debug("Row #" + i + " Input #" + (j-i+1) + ": CPU=" + valuesRow[j][1] + " Memory=" + valuesRow[j][2] + " Power=" + valuesRow[j][3]); 
            	inputs.add(normalizeValue(valuesRow[j][1],PredictorObject.CPU));
            	inputs.add(normalizeValue(valuesRow[j][2],PredictorObject.MEMORY));
            	inputs.add(normalizeValue(valuesRow[j][3],PredictorObject.POWER));
            }
            ArrayList<Double> outputs = new ArrayList<Double>();
            if (outputsTriple > 0 && i + inputsTriple + outputsTriple <= valuesRow.length) {
                for (int j = i + inputsTriple; j < i + inputsTriple + outputsTriple; j++) {
                	if (i < 10) {
        				LOGGER.debug("Row #" + i + " Output #" + (j-(i+inputsTriple)+1) + ": CPU=" + valuesRow[j][1] + " Memory=" + valuesRow[j][2] + " Power=" + valuesRow[j][3]);
                	}
                	outputs.add(normalizeValue(valuesRow[j][1],PredictorObject.CPU));
                	outputs.add(normalizeValue(valuesRow[j][2],PredictorObject.MEMORY));
                	outputs.add(normalizeValue(valuesRow[j][3],PredictorObject.POWER));
                }
                if (outputsCount > 0) {
                    trainingSet.addRow(new DataSetRow(inputs, outputs));
                } else {
                    trainingSet.addRow(new DataSetRow(inputs));
                }
            }
        }
        return trainingSet;
    }
	
	
	public double[] lastTrainingRow(double[][] valuesRow, int inputsCount, int outputsCount) {		
		
		int inputsTriple = inputsCount / 3;
		int outputsTriple = outputsCount / 3;
		
		double [] inputsRow = new double[inputsCount];
		double [] outputsRow = new double[outputsCount];
		double [] returnRow = new double[inputsCount];		
	
		for (int i = 0; i < valuesRow.length - inputsTriple; i++) {		                
			for (int j = i; j < i + inputsTriple; j++) {           	
            	for (int x = 0; x < 3; x++) {            		
            		inputsRow[((j-i)*3)+x] = valuesRow[j][x+1];
               	}
            }
            
			if (outputsTriple > 0 && i + inputsTriple + outputsTriple <= valuesRow.length) {
                for (int j = i + inputsTriple; j < i + inputsTriple + outputsTriple; j++) {                        
                    for (int x = 0; x < 3; x++) {                		
                    	outputsRow[((j-(i+ inputsTriple))*3)+x] = valuesRow[j][x+1];
                   	}
                }
            }
        }
		
        LOGGER.debug("Last Global Training Row Set: ");
        
        for (int i = 0; i < inputsCount-outputsCount; i++) {
			returnRow[i] = inputsRow[i+outputsCount];
			LOGGER.debug("#" + (i+1) + ": " + returnRow[i]);
		}
		for (int i = inputsCount-outputsCount; i < inputsCount; i++) {
			returnRow[i] = outputsRow[i-(inputsCount-outputsCount)];
			LOGGER.debug("#" + (i+1) + ": " + returnRow[i]);
		}
		
        return returnRow;
    }
	

	double normalizeValue(double input,	PredictorObject objectType) {
		
		double min = 0.0;
		double max = 0.0;
		double value = 0.0;
		
		switch (objectType) {
			case CPU:
				min = this.minCPU;
				max = this.maxCPU;
				break;
			case MEMORY:
				min = this.minMemory;
				max = this.maxMemory;
				break;
			case POWER:
				min = this.minPower;
				max = this.maxPower;
				break;
		}
		
		// method #1: 
		// value = (input - min) / (max - min) * 0.8 + 0.1;
		// if (input < min)
		//	LOGGER.debug("Normalize " + objectType + ": Input=" + input + "Output=" + value);
		
		// method #2:
		value = (input - (0.8 * min)) / ((1.2 * max) - (0.8 * min));
		
		if (input < (0.8 * min))
			LOGGER.debug("Normalize " + objectType + ": Input=" + input + "Output=" + value);
		
		return value;
	}

	double deNormalizeValue(double input, PredictorObject objectType) {
		
		double min = 0.0;
		double max = 0.0;
		double value = 0.0;
		
		switch (objectType) {
			case CPU:
				min = this.minCPU;
				max = this.maxCPU;
				break;
			case MEMORY:
				min = this.minMemory;
				max = this.maxMemory;
				break;
			case POWER:
				min = this.minPower;
				max = this.maxPower;
				break;
		}
		
		// method #1:
		// value =  min + (input - 0.1) * (max - min) / 0.8;
		// if (value < 0.0)
		//	LOGGER.debug("Denormalize " + objectType + ": Input=" + input + "Output=" + value);
		
		// method #2:
		value =  (0.8 * min) + (input * ((1.2 * max) - (0.8 * min)));
		
		if (value < 0.0)
			LOGGER.debug("Denormalize " + objectType + ": Input=" + input + "Output=" + value);
		
		return value;
	}
	
}