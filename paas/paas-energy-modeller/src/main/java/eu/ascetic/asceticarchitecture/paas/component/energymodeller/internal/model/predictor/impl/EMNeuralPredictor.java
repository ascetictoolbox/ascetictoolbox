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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Iterator;
import java.util.TreeMap;
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
import org.neuroph.nnet.learning.ResilientPropagation;
import org.neuroph.util.TransferFunctionType;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorServiceQueue;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.PredictorInterface;

public class EMNeuralPredictor implements PredictorInterface{

	private final static Logger LOGGER = Logger.getLogger(PredictorInterface.class.getName());
	private EnergyDataAggregatorServiceQueue service;
	
	private long lastSampleTimestamp = 0;
	private long sampleIntervalAverage = 0;
	private double maxPower = 0;
	private double minPower = Double.MAX_VALUE;
	
	
	public enum PredictorObject {
	CPU,MEMORY,POWER
	}

	@Override
	public double estimate(String providerid, String applicationid,	String deploymentid, List<String> vmids, String eventid, Unit unit, long forecasttime, boolean enablePowerFromIaas) {
	
		// only power then from power get energy estimation
		double cur_power = 0;
		int count=0;
		for (String vmid :vmids){
			count++;
			cur_power = cur_power + estimate( providerid,  applicationid,	 deploymentid,  vmid,  eventid,  unit,  forecasttime, enablePowerFromIaas) ;
		}
		
		if (count>0)return cur_power/count;

		return 0;
	}

	@Override
	public void setEnergyService(EnergyDataAggregatorServiceQueue service) {

		this.service = service;
	}

	@Override
	public double estimate(List<DataConsumption> samples, Unit unit, long forecasttime, boolean enablePowerFromIaas) {

		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double estimate(String providerid, String applicationid,	String deploymentid, String vm, String eventid, Unit unit, long forecasttime, boolean enablePowerFromIaas) {
		
		// Neuroph parameters
		double maxError = 0.001;	// Resilient = 0.001,	BackPropagation=0.007
        double learningRate = 0.01;	// Resilient = 0.01,		BackPropagation=0.1
        int maxIterations = 500;	// Resilient = 500
        int	inputNeurons = 10;		// Resilient = 10
        int	hiddenNeurons = 23;		// Resilient = 23
        int	outputNeurons = 1;		// Do not change !
        
		double estimation=0;
		forecasttime = forecasttime/1000;
		
		LOGGER.info("Forecaster Provider "+providerid + " Application "+applicationid + " VM "+vm + " at time "+forecasttime);
		LOGGER.info("############ Forecasting STARTED FOR Provider "+providerid + " Application "+applicationid + " VM "+vm+ "############");
		
		double[][] valuesRow = dataAggregation(providerid, applicationid, deploymentid, vm, eventid, maxIterations, enablePowerFromIaas);
				
		maxIterations = valuesRow.length;
		LOGGER.info("Samples of analysis for model will be " + valuesRow.length);
		if (valuesRow.length <= 1){
			LOGGER.warn("Not enought samples " + valuesRow.length);
			LOGGER.info("############ Forecasting not performed on Provider "+providerid + " Application "+applicationid + " VM "+vm+ "############");
			return 0;
		}
		
		if (this.sampleIntervalAverage == 0){
			LOGGER.warn("Invalid sample interval average (0)");
			return 0;
		}
		
		defineNormalizationParam(valuesRow);
		LOGGER.debug("minPower=" + this.minPower + " maxPower=" + this.maxPower);
		
		// Cycles Number
		int cyclesNumber = (int )((forecasttime - this.lastSampleTimestamp) / this.sampleIntervalAverage);
		int cyclesNumberReminder = (int )((forecasttime - this.lastSampleTimestamp) % this.sampleIntervalAverage);
		if (cyclesNumberReminder > 0)
				cyclesNumber++;
		LOGGER.info("Cycles Number : " + cyclesNumber);
		
		int counter, counterInput;
		
		/* Begin 
		// Power: Training and setup for first estimations
		NeuralNetwork<BackPropagation> neuralNetwork = new MultiLayerPerceptron(inputNeurons, hiddenNeurons, outputNeurons);
		SupervisedLearning learningRule = neuralNetwork.getLearningRule();
		learningRule.setMaxError(maxError);
		learningRule.setLearningRate(learningRate);
		learningRule.setMaxIterations(maxIterations);
		learningRule.addListener(new LearningEventListener() {			
			public void handleLearningEvent(LearningEvent learningEvent) {
				SupervisedLearning rule = (SupervisedLearning) learningEvent.getSource();
				LOGGER.debug("Power - Network error for interation " + rule.getCurrentIteration() + ": " + rule.getTotalNetworkError());
			}
		});
		   End */
		
		
		/* Begin */
		MultiLayerPerceptron neuralNetwork = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, inputNeurons, hiddenNeurons, outputNeurons);

	    ResilientPropagation learningRule = new ResilientPropagation();
	    learningRule.setNeuralNetwork(neuralNetwork);
	    neuralNetwork.setLearningRule(learningRule);

	    learningRule.setMaxError(maxError);
	    learningRule.setMaxIterations(maxIterations);
	    learningRule.setLearningRate(learningRate);	    
	    learningRule.addListener(new LearningEventListener() {			
			public void handleLearningEvent(LearningEvent learningEvent) {
				SupervisedLearning rule = (SupervisedLearning) learningEvent.getSource();
				LOGGER.debug("Power - Network error for interation " + rule.getCurrentIteration() + ": " + rule.getTotalNetworkError());
			}
		});			    
	    /* End */
		
		DataSet trainingSet = trainingImportObjectFromMatrix(valuesRow, inputNeurons, outputNeurons);
		// LOGGER.info("BEFORE TRAINING"); //MAXIM
		neuralNetwork.learn(trainingSet);
		// LOGGER.info("AFTER  TRAINING"); //MAXIM
		LOGGER.info("Power - Network iteration =" + learningRule.getCurrentIteration());
		// neuralNetwork.save(neuralNetworkModelFilePath);
		// trainingSet.saveAsTxt("/temp/CPU_Memory_Power.txt","|");
		
		double [] input = new double[inputNeurons];
		double [] inputDenorm = new double[inputNeurons];		
		double prevOutputPower = 0.0;
		double currOutputPower = 0.0;
		double calcOutputPower = 0.0;
						
		inputDenorm = lastTrainingRow(valuesRow, inputNeurons, outputNeurons);
		for (counterInput = 0; counterInput < inputNeurons; counterInput++)
				input[counterInput] = normalizeValue(inputDenorm[counterInput]);
		
		currOutputPower  = normalizeValue(inputDenorm[inputNeurons-1]);
		
		LOGGER.info("LAST SAMPLE - Timestamp: " + this.lastSampleTimestamp +
					" Power:"  + inputDenorm[inputNeurons-1]);
        
		double[] networkOutput;
		
		for (counter = 0; counter < cyclesNumber; counter++) {
			
			// Power: estimation
			// NeuralNetwork neuralNetGlobalPower = NeuralNetwork.createFromFile(neuralNetGlobalPowerModelFilePath);
			prevOutputPower  = currOutputPower;
			neuralNetwork.setInput(input);
			neuralNetwork.calculate();
			networkOutput = neuralNetwork.getOutput();
			currOutputPower  = networkOutput[0];			
			
			LOGGER.debug("FORECAST - Timestamp: " + (this.lastSampleTimestamp + (this.sampleIntervalAverage * (counter + 1))) +
						" Power:"  + deNormalizeValue(currOutputPower));			
								
			// Power: setup for next estimations
			// input array update: 
			// e.g.
			// from: "ValPower1, ValPower2, ..., ValPowerX" 
			// to:   "ValPower2, ..., ValPowerX, currOutputGlobalPower"			
				
			for (counterInput = 0; counterInput < (inputNeurons-1); counterInput++) {		
				input[counterInput] = input[counterInput+1];
				inputDenorm[counterInput] = inputDenorm[counterInput+1];				
			}
			input[inputNeurons-1] = currOutputPower;			
			inputDenorm[inputNeurons-1] = deNormalizeValue(currOutputPower);
		}
				
		// adjusting value if timelater is not a multiple of the sampling period
		if (cyclesNumberReminder != 0) {
			
			calcOutputPower  = prevOutputPower  + ((currOutputPower  - prevOutputPower)  * ((double )cyclesNumberReminder / (double )this.sampleIntervalAverage));
			estimation = deNormalizeValue(calcOutputPower);
			LOGGER.debug("FORECAST Adjusting - Timestamp: " + forecasttime +						
						" Power:"  + deNormalizeValue(calcOutputPower));
		} else {
			estimation = deNormalizeValue(currOutputPower);
			
			/* MAXIM
			LOGGER.info("FORECAST NoAdjusting - Timestamp: " + forecasttime +					
					" Power:"  + deNormalizeValue(currOutputPower));	
			   MAXIM */			
		}
		
		LOGGER.info("############ Forecasting TERMINATED POWER WILL BE "+estimation + " at "+forecasttime+ "############");
		
		return estimation;
		
	}

	
	public double[][] dataAggregation(String providerid, String applicationid, String deploymentid, String vm, String eventid, int maxIterations, boolean enablePowerFromIaas) {
		
		int counter;
		double value;
		long timestamp;
		int counterALL = 0;
		
		List<DataConsumption> powerSample = service.samplePower(providerid, applicationid, deploymentid, vm, enablePowerFromIaas);
		
		if (powerSample == null) {			
			LOGGER.info("Power samples for the analysis 0");
			double [][] valuesRow = new double[0][2];
			return(valuesRow);			
		}
		else {
			LOGGER.info("Power samples for the analysis "+powerSample.size());
			if (powerSample.size() < 2) {
				double [][] valuesRow = new double[0][2];
				return(valuesRow);
			}
		}
		
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
		
		double [][] valuesRowTmp = new double[counter][2];	
		int counterPower = 0;
        while(iteratorPowerSort.hasNext()) {
            Map.Entry<Long, Double> mePowerSort = (Map.Entry<Long, Double>)iteratorPowerSort.next();
            timestamp = (long) mePowerSort.getKey();
            value = (double) mePowerSort.getValue();			
			valuesRowTmp[counterPower][0] = (double )timestamp;			
			valuesRowTmp[counterPower][1] = value;			
			if (counterPower < 10)
				LOGGER.debug("Key " + timestamp + ": " + value); 
			counterPower++;
		}		
			
		int firstRow = 0;
		
		if (counterPower >= maxIterations) {
			counterALL = maxIterations;
			firstRow = counterPower - maxIterations;
		}
		else {
			counterALL = counterPower;
			firstRow = 0;
		}
		
		double [][] valuesRow = new double[counterALL][4];
		LOGGER.debug("Used data: ");
		
		for (counter=0; counter<counterALL; counter++) {
			
			valuesRow[counter][0] = valuesRowTmp[counter + firstRow][0];
			valuesRow[counter][1] = valuesRowTmp[counter + firstRow][1]; 
							
			if (counter < 10)
				LOGGER.debug("#" + (counter) + ": " + ((long )valuesRow[counter][0]) + " "+ valuesRow[counter][1]);
			
			/* MAXIM 
			if (counter < 10000) //MAXIM
				LOGGER.info((counter) + "|" + ((long )valuesRow[counter][0]) + "|"+ valuesRow[counter][1]); //MAXIM
			   MAXIM */
		}
		
		
		LOGGER.debug("Number of used rows: " + counterALL);
		
		if (counterALL > 0) {		
			
			this.lastSampleTimestamp = (long )valuesRow[counterALL-1][0];
			if (counterALL > 1)
				this.sampleIntervalAverage = (this.lastSampleTimestamp - (long )valuesRow[0][0])/(counterALL-1);
			else
				this.sampleIntervalAverage = 0;
		
			LOGGER.info("Last Sample Timestamp / Average Interval: " + this.lastSampleTimestamp + " / " + this.sampleIntervalAverage);

		}
				
        return(valuesRow);

	}
	
	
	public void defineNormalizationParam(double [][] valueRow) {
		
		int counter;
		double valuePower;
		
		for (counter=0; counter < valueRow.length; counter++) {			
			
			valuePower = valueRow[counter][1];			
			
			if (valuePower > this.maxPower)
				this.maxPower = valuePower;
			if (valuePower < this.minPower)
				this.minPower = valuePower;			
		}	
		
	}
	

	public DataSet trainingImportObjectFromMatrix(double[][] valuesRow, int inputsCount, int outputsCount) {		
		
		int	TrainingRows = 0;
		
        DataSet trainingSet = new DataSet(inputsCount, outputsCount);
		
        LOGGER.debug("Global Training Set: ");
        
        for (int i = 0; i < valuesRow.length - inputsCount; i++) {
            ArrayList<Double> inputs = new ArrayList<Double>();
            for (int j = i; j < i + inputsCount; j++) {
            	
            	if (i < 100)
    				LOGGER.debug("Row #" + i + " Input #" + (j-i+1) + " Power=" + valuesRow[j][1] + " Normalize=" + normalizeValue(valuesRow[j][1]));
            	
            	inputs.add(normalizeValue(valuesRow[j][1]));
            }
            
            ArrayList<Double> outputs = new ArrayList<Double>();
            if (outputsCount > 0 && i + inputsCount + outputsCount <= valuesRow.length) {
                for (int j = i + inputsCount; j < i + inputsCount + outputsCount; j++) {
                
                	if (i < 100)
        				LOGGER.debug("Row #" + i + " Output #" + (j-(i+inputsCount)+1) + " Power=" + valuesRow[j][1] + " Normalize=" + normalizeValue(valuesRow[j][1]));                	     	
                	
                	outputs.add(normalizeValue(valuesRow[j][1]));
                }
                
                if (outputsCount > 0) {
                	TrainingRows++;
                    trainingSet.addRow(new DataSetRow(inputs, outputs));
                } else {
                    trainingSet.addRow(new DataSetRow(inputs)); 
                }
            }
        }
        LOGGER.debug("Number of Global Training Rows: "+TrainingRows);
        return trainingSet;
    }
	
	
	public double[] lastTrainingRow(double[][] valuesRow, int inputsCount, int outputsCount) {		
		
		double [] inputsRow = new double[inputsCount];
		double [] outputsRow = new double[outputsCount];
		double [] returnRow = new double[inputsCount];		
	
		for (int i = 0; i < valuesRow.length - inputsCount; i++) {		                
			for (int j = i; j < i + inputsCount; j++) {           	
            	           		
            		inputsRow[j-i] = valuesRow[j][1];               	
            }
            
			if (outputsCount > 0 && i + inputsCount + outputsCount <= valuesRow.length) {
                for (int j = i + inputsCount; j < i + inputsCount + outputsCount; j++) {                        
                                  		
                    	outputsRow[j-(i+inputsCount)] = valuesRow[j][1];                   	
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
	

	double normalizeValue(double input) {
				
		double value = 0.0;
		
		/* method #1: 
		value = (input - this.minPower) / (this.maxPower - this.minPower) * 0.8 + 0.1;
		if (input < this.minPower)
			LOGGER.debug("ERROR in Normalize: Input=" + input + "Output=" + value);
		*/
		
		/* method #2: */
		value = (input - (0.8 * this.minPower)) / ((1.2 * this.maxPower) - (0.8 * this.minPower));
		if (input < (0.8 * this.minPower))
			LOGGER.debug("**********MIN Normalize: Input=" + input + "Output=" + value);
		/**/
		
		/* method #3:
		double inputPlus1 = input + 1.0;
		double minPowerPlus1 = this.minPower + 1.0;
		double maxPowerPlus1 = this.maxPower + 1.0;
		value = (inputPlus1 - (0.8 * minPowerPlus1)) / ((1.2 * maxPowerPlus1) - (0.8 * minPowerPlus1));
		
		if (inputPlus1 < (0.8 * minPowerPlus1))
			LOGGER.debug("**********MIN Normalize: Input=" + input + "Output=" + value);	
		*/
		
		/* method #4:
		double inputFor10 = input * 10.0;
		double minPowerFor10 = this.minPower * 10.0;
		double maxPowerFor10 = this.maxPower * 10.0;
		value = (inputFor10 - (0.8 * minPowerFor10)) / ((1.2 * maxPowerFor10) - (0.8 * minPowerFor10));
		
		if (inputFor10 < (0.8 * minPowerFor10))
			LOGGER.debug("**********MIN Normalize: Input=" + input + "Output=" + value);	
		*/
		
		return value;
	}

	double deNormalizeValue(double input) {
		
		double value = 0.0;
		
		/* method #1:
		value =  this.minPower + (input - 0.1) * (this.maxPower - this.minPower) / 0.8;
		if (value < 0.0)
			LOGGER.debug("ERROR in Denormalize: Input=" + input + "Output=" + value);
		*/
		
		/* method #2: */ 
		value =  (0.8 * this.minPower) + (input * ((1.2 * this.maxPower) - (0.8 * this.minPower)));
		if (value < 0.0)
			LOGGER.debug("Denormalize: Input=" + input + "Output=" + value);
		/* */
		
		/* method #3:
		double minPowerPlus1 = this.minPower + 1.0;
		double maxPowerPlus1 = this.maxPower + 1.0;
		value =  (0.8 * minPowerPlus1) + (input * ((1.2 * maxPowerPlus1) - (0.8 * minPowerPlus1))) - 1.0;
		if (value < 0.0)
			LOGGER.debug("Denormalize: Input=" + input + "Output=" + value);
		*/
		
		/* method #4:
		double minPowerFor10 = this.minPower * 10.0;
		double maxPowerFor10 = this.maxPower * 10.0;
		value =  ((0.8 * minPowerFor10) + (input * ((1.2 * maxPowerFor10) - (0.8 * minPowerFor10)))) / 10.0;
		if (value < 0.0)
			LOGGER.debug("Denormalize: Input=" + input + "Output=" + value);
		*/
		
		return value;
	}
	
}