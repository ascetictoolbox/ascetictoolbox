package eu.ascetic.saas.experimentmanager.models;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.ascetic.saas.experimentmanager.exception.MetricDefinitionIncorrectException;
import eu.ascetic.saas.experimentmanager.exception.NoMeasureException;
import eu.ascetic.saas.experimentmanager.wslayer.RESSOURCEFORMAT;
import eu.ascetic.saas.experimentmanager.wslayer.WSBasic;
import eu.ascetic.saas.experimentmanager.wslayer.exception.IncorrectRessourceFormatException;
import eu.ascetic.saas.experimentmanager.wslayer.exception.ResponseParsingException;
import eu.ascetic.saas.experimentmanager.wslayer.exception.WSException;

public class WebserviceValue extends Metric {
	
	private String urlPattern;
	private String postPattern;
	private String query;
	private RESSOURCEFORMAT format;
	
	private static String mark=":";
	
	public WebserviceValue(){
		
	}
	
	public WebserviceValue(String id, String urlPattern, RESSOURCEFORMAT format, String query){
		this.setName(id);
		this.urlPattern = urlPattern;
		this.query = query;
		this.postPattern = null;
		this.format = format;
		this.setType("simple");
	}
	
	public WebserviceValue(String id, String urlPattern, RESSOURCEFORMAT format, String query, String postPattern){
		this.setName(id);
		this.urlPattern = urlPattern;
		this.query = query;
		this.postPattern = postPattern;
		this.format = format;
		this.setType("simple");
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
			throw new MetricDefinitionIncorrectException("only one scopable item in scope for SimpleMetric with name "+getName() + " for scope " + scope );
		}
		
		ScopableItem item = scope.getScopableItems().get(0);
		
		Map<String, Object> parametrization = item.getLocation();
		
		String url = instanciate(urlPattern,parametrization);
		String post = instanciate(postPattern,parametrization);
		String query = instanciate(this.query,parametrization);
		
		try {
			return WSBasic.getSingle(url,format,query,post);
		} catch (IncorrectRessourceFormatException e) {
			throw new MetricDefinitionIncorrectException("Incorrect resource format for metric : " + this.getName(),e);
		} catch (ResponseParsingException e) {
			throw new MetricDefinitionIncorrectException("Incorrect resource format for metric : " + this.getName(),e);
		} catch (Exception e) {
			Logger.getLogger("eu.ascetic.saas.experimentmanager.models").log(Level.WARNING,
					"Measure doesn't exist for metric : " + this.getName() + " on scope " + scope, e);
			throw new NoMeasureException("Measure doesn't exist for metric : " + this.getName() + " on scope " + scope,e);
		} 
	}
	

	public boolean havePost() {
		return postPattern != null;
	}
	

	/**
	 * @return the urlPattern
	 */
	public String getUrlPattern() {
		return urlPattern;
	}

	/**
	 * @param urlPattern the urlPattern to set
	 */
	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
	}

	/**
	 * @return the postPattern
	 */
	public String getPostPattern() {
		return postPattern;
	}

	/**
	 * @param postPattern the postPattern to set
	 */
	public void setPostPattern(String postPattern) {
		this.postPattern = postPattern;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * @return the format
	 */
	public RESSOURCEFORMAT getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(RESSOURCEFORMAT format) {
		this.format = format;
	}

	/**
	 * @return the mark
	 */
	public static String getMark() {
		return mark;
	}

	/**
	 * @param mark the mark to set
	 */
	public static void setMark(String mark) {
		WebserviceValue.mark = mark;
	}
	
}
