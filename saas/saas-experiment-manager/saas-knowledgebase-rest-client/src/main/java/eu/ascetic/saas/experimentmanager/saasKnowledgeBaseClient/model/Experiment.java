package eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Event;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.KPI;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.*;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-06-29T13:08:58.029+02:00")
public class Experiment   {
  
  private String id = null;
  private String name = null;
  private String description = null;
  private String appId = null;
  private List<KPI> kpis = new ArrayList<KPI>();
  private List<Event> events = new ArrayList<Event>();

  
  /**
   * UUID
   **/
  
  @ApiModelProperty(value = "UUID")
  @JsonProperty("_id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   **/
  
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  
  @ApiModelProperty(value = "")
  @JsonProperty("appId")
  public String getAppId() {
    return appId;
  }
  public void setAppId(String appId) {
    this.appId = appId;
  }

  
  /**
   * the list of KPIs used to analyze this experiment
   **/
  
  @ApiModelProperty(value = "the list of KPIs used to analyze this experiment")
  @JsonProperty("kpis")
  public List<KPI> getKpis() {
    return kpis;
  }
  public void setKpis(List<KPI> kpis) {
    this.kpis = kpis;
  }

  
  /**
   * the list of events used to measure the current deployment
   **/
  
  @ApiModelProperty(value = "the list of events used to measure the current deployment")
  @JsonProperty("events")
  public List<Event> getEvents() {
    return events;
  }
  public void setEvents(List<Event> events) {
    this.events = events;
  }

  

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Experiment experiment = (Experiment) o;
    return Objects.equals(id, experiment.id) &&
        Objects.equals(name, experiment.name) &&
        Objects.equals(description, experiment.description) &&
        Objects.equals(appId, experiment.appId) &&
        Objects.equals(kpis, experiment.kpis) &&
        Objects.equals(events, experiment.events);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, appId, kpis, events);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Experiment {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
    sb.append("    kpis: ").append(toIndentedString(kpis)).append("\n");
    sb.append("    events: ").append(toIndentedString(events)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

