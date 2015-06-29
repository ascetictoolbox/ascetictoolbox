package prediction;

import java.util.List;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.text.html.HTMLDocument.Iterator;

import data.Sample;
import utility.DbUtility;
import net.sourceforge.openforecast.DataPoint;
import net.sourceforge.openforecast.DataSet;
import net.sourceforge.openforecast.Forecaster;
import net.sourceforge.openforecast.ForecastingModel;
import net.sourceforge.openforecast.Observation;

public class Predictor extends TimerTask{

	private DbUtility dbutility;
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		DataSet dataset = new DataSet();
		Observation observation;
		List<Sample> samples = dbutility.getData();
		System.out.println("Sampels "+samples.size());
		for (int i = 0; i<samples.size();i++) {
			observation = new Observation(new Float(samples.get(i).getPower()));
			observation.setIndependentValue("CPU",new Float(samples.get(i).getCpu()));
			observation.setIndependentValue("MEM",new Float(samples.get(i).getMemory()));
			System.out.println("Data is "+observation.toString());
			dataset.add(observation);
		}
	    ForecastingModel forecaster = Forecaster.getBestForecast( dataset );
	    System.out.println("Forecast model type selected: "+forecaster.getForecastType());
	    System.out.println( forecaster.toString() );
	    
	    forecaster.init(dataset);
	    
	    
	    DataSet fcDataSet = new DataSet();
	    DataPoint point = new Observation(0.0);
	    point.setIndependentValue("CPU", 0.1);
	    point.setIndependentValue("MEM", 2000);
	    
	    fcDataSet.add(point);   
	    // Create forecast data set and add these DataPoints
	    
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.2 );
	    point.setIndependentValue( "MEM", 2000 );
	    fcDataSet.add(point);   
       
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.3 );
	    point.setIndependentValue( "MEM", 2000 );
	    fcDataSet.add(point);   
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.4 );
	    point.setIndependentValue( "MEM", 2000 );
	    fcDataSet.add(point); 
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.5 );
	    point.setIndependentValue( "MEM", 2000 );
	    fcDataSet.add(point); 

	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.7 );
	    point.setIndependentValue( "MEM", 2000 );
	    fcDataSet.add(point); 
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.9 );
	    point.setIndependentValue( "MEM", 2000 );
	    fcDataSet.add(point); 
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 1 );
	    point.setIndependentValue( "MEM", 2000 );
	    fcDataSet.add(point); 
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.2 );
	    point.setIndependentValue( "MEM", 1000 );
	    fcDataSet.add(point);   
       
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.3 );
	    point.setIndependentValue( "MEM", 1000 );
	    fcDataSet.add(point);   
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.4 );
	    point.setIndependentValue( "MEM", 1000 );
	    fcDataSet.add(point); 
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.5 );
	    point.setIndependentValue( "MEM", 1000 );
	    fcDataSet.add(point); 

	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.7 );
	    point.setIndependentValue( "MEM", 1000 );
	    fcDataSet.add(point); 
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.9 );
	    point.setIndependentValue( "MEM", 1000 );
	    fcDataSet.add(point); 
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 1 );
	    point.setIndependentValue( "MEM", 1000 );
	    fcDataSet.add(point); 
	    
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.2 );
	    point.setIndependentValue( "MEM", 500 );
	    fcDataSet.add(point);   
       
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.3 );
	    point.setIndependentValue( "MEM", 500 );
	    fcDataSet.add(point);   
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.4 );
	    point.setIndependentValue( "MEM", 500 );
	    fcDataSet.add(point); 
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.5 );
	    point.setIndependentValue( "MEM", 500 );
	    fcDataSet.add(point); 

	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.7 );
	    point.setIndependentValue( "MEM", 500 );
	    fcDataSet.add(point); 
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 0.9 );
	    point.setIndependentValue( "MEM", 500 );
	    fcDataSet.add(point); 
	    
	    point = new Observation( 0.0 );
	    point.setIndependentValue( "CPU", 1 );
	    point.setIndependentValue( "MEM", 500 );
	    fcDataSet.add(point); 
	    
        // Dump data set before forecast
        System.out.println("Required data set before forecast");
        System.out.println( fcDataSet );
        
        // Use the given forecasting model to forecast values for
        //  the required (future) data points
        forecaster.forecast( fcDataSet );
        
        // Output the results
        System.out.println("Output data, forecast values");
        System.out.println( fcDataSet );
		
	}
	
	public void setDbUtility(DbUtility dbutility){
		this.dbutility = dbutility;
	}
	
}
