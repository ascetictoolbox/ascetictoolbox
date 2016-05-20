package eu.ascetic.saas.experimentmanager.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import eu.ascetic.saas.experimentmanager.wslayer.WSBasic;
import eu.ascetic.saas.experimentmanager.wslayer.exception.IncorrectRessourceFormatException;
import eu.ascetic.saas.experimentmanager.wslayer.exception.ResponseParsingException;
import eu.ascetic.saas.experimentmanager.wslayer.exception.WSBaseException;
import eu.ascetic.saas.experimentmanager.wslayer.exception.WSException;
import eu.ascetic.saas.experimentmanager.exception.MetricDefinitionIncorrectException;
import eu.ascetic.saas.experimentmanager.exception.NoMeasureException;
import eu.ascetic.saas.experimentmanager.wslayer.RESSOURCEFORMAT;

public class WebserviceValue extends Metric {
	
	private String name;
	
	private String urlPattern;
	private String postPattern;
	private String query;
	private RESSOURCEFORMAT format;
	
	private static String mark=":";
	
	public WebserviceValue(String id, String urlPattern, RESSOURCEFORMAT format, String query){
		this.name = id;
		this.urlPattern = urlPattern;
		this.query = query;
		this.postPattern = null;
		this.format = format;
	}
	
	public WebserviceValue(String id, String urlPattern, RESSOURCEFORMAT format, String query, String postPattern){
		this.name = id;
		this.urlPattern = urlPattern;
		this.query = query;
		this.postPattern = postPattern;
		this.format = format;
	}
	
	/**
	 * This method just replace a set of labels in a string by a value.
	 * 
	 * 
	 * 
	 * @param pattern the string in which replace terms
	 * @param parametrization
	 * @return
	 */
	private String instanciate(String pattern, Map<String,Object> parametrization) {
		if(pattern==null) return null;
		String instanciation = new String(pattern);
		for(Entry<String,Object> v : parametrization.entrySet()){
			if(v.getValue() instanceof String){
				instanciation = instanciation.replace(mark+v.getKey(), (String)v.getValue());
			}
		}
		return instanciation;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.ascetic.saas.experimentmanager.measureInterceptor.Metric#get(java.util.Map)
	 */
	@Override
	public String get(Scope scope) throws MetricDefinitionIncorrectException, NoMeasureException{
		if(scope.getScopableItems().size()!=1){
			throw new MetricDefinitionIncorrectException("only one scopable item in scope for SimpleMetric");
		}
		
		ScopableItem item = scope.getScopableItems().get(0);
		
		Map<String, Object> parametrization = item.getLocation();
		
		String url = instanciate(urlPattern,parametrization);
		String post = instanciate(postPattern,parametrization);
		String query = instanciate(this.query,parametrization);
		
		try {
			return WSBasic.getSingle(url,format,query,post);
		} catch (WSException e) {
			throw new NoMeasureException("Measure doesn't exist for metric : " + this.getName() + " on scope " + scope,e);
		} catch (IncorrectRessourceFormatException e) {
			throw new MetricDefinitionIncorrectException("Incorrect resource format for metric : " + this.getName(),e);
		} catch (ResponseParsingException e) {
			throw new MetricDefinitionIncorrectException("Incorrect resource format for metric : " + this.getName(),e);
		}
	}
	

	public boolean havePost() {
		return postPattern != null;
	}
	
	/* (non-Javadoc)
	 * @see eu.ascetic.saas.experimentmanager.measureInterceptor.Metric#getId()
	 */
	public String getName(){
		return name;
	}
	
}
