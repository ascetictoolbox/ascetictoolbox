package prediction;

import java.util.List;
import java.util.TimerTask;

import net.sourceforge.openforecast.Observation;
import data.Sample;
import utility.DbUtility;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class WekaMultiVarPredictor extends TimerTask{

	private DbUtility dbutility;
	

	@Override
	public void run() {
		Attribute cpu = new Attribute("CPU");
		Attribute memory =  new Attribute("Memory");
		Attribute power = new Attribute("Power");
		
		// Declare the feature vector
		 FastVector fvWekaAttributes = new FastVector(3);
		 fvWekaAttributes.addElement(cpu);
		 fvWekaAttributes.addElement(memory);
		 fvWekaAttributes.addElement(power);
		 
		// Create an empty training set
		 
		 
		 // Create the instance
		 

		 
		 List<Sample> samples = dbutility.getData();
		 
		 
		 Instances isTrainingSet = new Instances("Powermodel", fvWekaAttributes, 0);

		 isTrainingSet.setClassIndex(2);
		 
		 Instance iExample;
		 
		 System.out.println("Samples "+samples.size());
		 for (int i = 0; i<samples.size();i++) {
			 iExample = new DenseInstance(3);
			 iExample.setValue((Attribute)fvWekaAttributes.elementAt(0), new Float(samples.get(i).getCpu()));
			 iExample.setValue((Attribute)fvWekaAttributes.elementAt(1), new Float(samples.get(i).getMemory()));
			 iExample.setValue((Attribute)fvWekaAttributes.elementAt(2), new Float(samples.get(i).getPower()));
			 isTrainingSet.add(iExample);
		 }

		 iExample = new DenseInstance(3);
		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(0), 0.1);
		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(1), 1024);

		 isTrainingSet.add(iExample);
		 
//		 iExample = new DenseInstance(3);
//		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(0), 0.5);
//		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(1), 1024);
//
//		 isTrainingSet.add(iExample);
		 
		 // add the instance

		 
		 LinearRegression model = new LinearRegression();
		 try {
			 model.buildClassifier(isTrainingSet);
		
			 System.out.println(model);
	
			 Instance ukPower = isTrainingSet.lastInstance();
			 double powerest = model.classifyInstance(ukPower);
			 
			 System.out.println("Power ("+ukPower+"): "+powerest);
			 } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			 }
		
	}
	
	public void setDbUtility(DbUtility dbutility){
		this.dbutility = dbutility;
	}
	
}
