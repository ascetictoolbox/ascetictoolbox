package eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.client;

import com.sun.jersey.api.client.GenericType;

import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.ApiException;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.ApiClient;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.Configuration;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.Pair;

import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Experiment;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Snapshot;

import java.util.*;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-20T17:52:06.825+02:00")
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
   * Gets `Snapshot` objects according to a specific experiments.
   * @param expId Experiment&#39;s ID
   * @return List<Snapshot>
   */
  public List<Snapshot> snapshotsGet(String expId) throws ApiException {
    Object postBody = null;
    
     // verify the required parameter 'expId' is set
     if (expId == null) {
        throw new ApiException(400, "Missing the required parameter 'expId' when calling snapshotsGet");
     }
     
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
  
}
