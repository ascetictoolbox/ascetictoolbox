package eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.client;

import com.sun.jersey.api.client.GenericType;

import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.ApiException;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.ApiClient;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.Configuration;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.Pair;

import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Experiment;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Event;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.KPI;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Snapshot;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.SnapshotLess;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.EventWithMeasure;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Measure;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.VM;

import java.util.*;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-06-29T13:08:58.029+02:00")
public class DefaultApi {
  private ApiClient apiClient;

  public DefaultApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DefaultApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  
  /**
   * 
   * Gets `Experiments` objects.
   * @return List<Experiment>
   */
  public List<Experiment> experimentsGet() throws ApiException {
    Object postBody = null;
    
    // create path and map variables
    String path = "/experiments".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    

    

    

    final String[] accepts = {
      "application/json", "application/xml"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    GenericType<List<Experiment>> returnType = new GenericType<List<Experiment>>() {};
    return apiClient.invokeAPI(path, "GET", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    
  }
  
  /**
   * 
   * Push an `experiment` object
   * @param body experiment object to be pushed
   * @return Experiment
   */
  public Experiment experimentsPost(Experiment body) throws ApiException {
    Object postBody = body;
    
     // verify the required parameter 'body' is set
     if (body == null) {
        throw new ApiException(400, "Missing the required parameter 'body' when calling experimentsPost");
     }
     
    // create path and map variables
    String path = "/experiments".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    

    

    

    final String[] accepts = {
      "application/json", "application/xml"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      "application/json", "application/xml"
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    GenericType<Experiment> returnType = new GenericType<Experiment>() {};
    return apiClient.invokeAPI(path, "POST", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    
  }
  
  /**
   * 
   * Gets `Experiments` object.
   * @param expid id of the experiment to retrieve
   * @return Experiment
   */
  public Experiment experimentGet(String expid) throws ApiException {
    Object postBody = null;
    
     // verify the required parameter 'expid' is set
     if (expid == null) {
        throw new ApiException(400, "Missing the required parameter 'expid' when calling experimentGet");
     }
     
    // create path and map variables
    String path = "/experiments/{expid}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "expid" + "\\}", apiClient.escapeString(expid.toString()));

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    

    

    

    final String[] accepts = {
      "application/json", "application/xml"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    GenericType<Experiment> returnType = new GenericType<Experiment>() {};
    return apiClient.invokeAPI(path, "GET", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    
  }
  
  /**
   * 
   * Gets the list of event for a specific experiment
   * @param expid id of the experiment to retrieve
   * @return List<Event>
   */
  public List<Event> eventGet(String expid) throws ApiException {
    Object postBody = null;
    
     // verify the required parameter 'expid' is set
     if (expid == null) {
        throw new ApiException(400, "Missing the required parameter 'expid' when calling eventGet");
     }
     
    // create path and map variables
    String path = "/experiments/{expid}/events".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "expid" + "\\}", apiClient.escapeString(expid.toString()));

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    

    

    

    final String[] accepts = {
      "application/json", "application/xml"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    GenericType<List<Event>> returnType = new GenericType<List<Event>>() {};
    return apiClient.invokeAPI(path, "GET", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    
  }
  
  /**
   * 
   * Gets the list of kpis for a specific experiment
   * @param expid id of the experiment to retrieve
   * @return List<KPI>
   */
  public List<KPI> experimentKPISGet(String expid) throws ApiException {
    Object postBody = null;
    
     // verify the required parameter 'expid' is set
     if (expid == null) {
        throw new ApiException(400, "Missing the required parameter 'expid' when calling experimentKPISGet");
     }
     
    // create path and map variables
    String path = "/experiments/{expid}/kpis".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "expid" + "\\}", apiClient.escapeString(expid.toString()));

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    

    

    

    final String[] accepts = {
      "application/json", "application/xml"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    GenericType<List<KPI>> returnType = new GenericType<List<KPI>>() {};
    return apiClient.invokeAPI(path, "GET", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    
  }
  
  /**
   * 
   * Gets the list of event for a specific experiment
   * @param expid id of the experiment to retrieve
   * @return List<Snapshot>
   */
  public List<Snapshot> experimentSnapshotGet(String expid) throws ApiException {
    Object postBody = null;
    
     // verify the required parameter 'expid' is set
     if (expid == null) {
        throw new ApiException(400, "Missing the required parameter 'expid' when calling experimentSnapshotGet");
     }
     
    // create path and map variables
    String path = "/experiments/{expid}/snapshots".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "expid" + "\\}", apiClient.escapeString(expid.toString()));

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    

    

    

    final String[] accepts = {
      "application/json", "application/xml"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    GenericType<List<Snapshot>> returnType = new GenericType<List<Snapshot>>() {};
    return apiClient.invokeAPI(path, "GET", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    
  }
  
  /**
   * 
   * Gets `Snapshot` objects according to a specific experiments.
   * @param expId Experiment&#39;s ID
   * @return List<Snapshot>
   */
  public List<Snapshot> snapshotsGet(String expId) throws ApiException {
    Object postBody = null;
    
    // create path and map variables
    String path = "/snapshots".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    
    queryParams.addAll(apiClient.parameterToPairs("", "expId", expId));
    

    

    

    final String[] accepts = {
      "application/json", "application/xml"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    GenericType<List<Snapshot>> returnType = new GenericType<List<Snapshot>>() {};
    return apiClient.invokeAPI(path, "GET", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    
  }
  
  /**
   * 
   * Push an `Snapshot` object
   * @param body Snapshot object to be pushed
   * @return Snapshot
   */
  public Snapshot snapshotsPost(Snapshot body) throws ApiException {
    Object postBody = body;
    
     // verify the required parameter 'body' is set
     if (body == null) {
        throw new ApiException(400, "Missing the required parameter 'body' when calling snapshotsPost");
     }
     
    // create path and map variables
    String path = "/snapshots".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    

    

    

    final String[] accepts = {
      "application/json", "application/xml"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      "application/json", "application/xml"
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    GenericType<Snapshot> returnType = new GenericType<Snapshot>() {};
    return apiClient.invokeAPI(path, "POST", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    
  }
  
  /**
   * 
   * Get `Snapshot` object according.
   * @param snapid snapshotId
   * @return SnapshotLess
   */
  public SnapshotLess snapshotGet(String snapid) throws ApiException {
    Object postBody = null;
    
     // verify the required parameter 'snapid' is set
     if (snapid == null) {
        throw new ApiException(400, "Missing the required parameter 'snapid' when calling snapshotGet");
     }
     
    // create path and map variables
    String path = "/snapshots/{snapid}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "snapid" + "\\}", apiClient.escapeString(snapid.toString()));

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    

    

    

    final String[] accepts = {
      "application/json", "application/xml"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    GenericType<SnapshotLess> returnType = new GenericType<SnapshotLess>() {};
    return apiClient.invokeAPI(path, "GET", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    
  }
  
  /**
   * 
   * Get `Snapshot` object according.
   * @param snapid snapshotId
   * @return List<EventWithMeasure>
   */
  public List<EventWithMeasure> snapshotMeasureByEventGet(String snapid) throws ApiException {
    Object postBody = null;
    
     // verify the required parameter 'snapid' is set
     if (snapid == null) {
        throw new ApiException(400, "Missing the required parameter 'snapid' when calling snapshotMeasureByEventGet");
     }
     
    // create path and map variables
    String path = "/snapshots/{snapid}/measurebyevent".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "snapid" + "\\}", apiClient.escapeString(snapid.toString()));

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    

    

    

    final String[] accepts = {
      "application/json", "application/xml"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    GenericType<List<EventWithMeasure>> returnType = new GenericType<List<EventWithMeasure>>() {};
    return apiClient.invokeAPI(path, "GET", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    
  }
  
  /**
   * 
   * Get `Snapshot` object according.
   * @param snapid snapshotId
   * @return List<Measure>
   */
  public List<Measure> snapshotMeasuresGet(String snapid) throws ApiException {
    Object postBody = null;
    
     // verify the required parameter 'snapid' is set
     if (snapid == null) {
        throw new ApiException(400, "Missing the required parameter 'snapid' when calling snapshotMeasuresGet");
     }
     
    // create path and map variables
    String path = "/snapshots/{snapid}/measures".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "snapid" + "\\}", apiClient.escapeString(snapid.toString()));

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    

    

    

    final String[] accepts = {
      "application/json", "application/xml"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    GenericType<List<Measure>> returnType = new GenericType<List<Measure>>() {};
    return apiClient.invokeAPI(path, "GET", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    
  }
  
  /**
   * 
   * Get `Snapshot` object according.
   * @param snapid snapshotId
   * @return List<VM>
   */
  public List<VM> snapshotVMsGet(String snapid) throws ApiException {
    Object postBody = null;
    
     // verify the required parameter 'snapid' is set
     if (snapid == null) {
        throw new ApiException(400, "Missing the required parameter 'snapid' when calling snapshotVMsGet");
     }
     
    // create path and map variables
    String path = "/snapshots/{snapid}/vms".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "snapid" + "\\}", apiClient.escapeString(snapid.toString()));

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    

    

    

    final String[] accepts = {
      "application/json", "application/xml"
    };
    final String accept = apiClient.selectHeaderAccept(accepts);

    final String[] contentTypes = {
      
    };
    final String contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {  };

    
    GenericType<List<VM>> returnType = new GenericType<List<VM>>() {};
    return apiClient.invokeAPI(path, "GET", queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    
  }
  
}
