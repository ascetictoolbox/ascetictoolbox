package eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Measure;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.VM;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.*;
import java.util.Date;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-20T17:52:06.825+02:00")
public class Snapshot   {
  
  private String id = null;
  private String experimentId = null;
  private String name = null;
  private String deplId = null;
  private String deplName = null;
  private String description = null;
  private Date date = null;
  private List<VM> vms = new ArrayList<VM>();
  private List<Measure> measures = new ArrayList<Measure>();

  
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
   * UUID of the experiment
   **/
  
  @ApiModelProperty(value = "UUID of the experiment")
  @JsonProperty("experimentId")
  public String getExperimentId() {
    return experimentId;
  }
  public void setExperimentId(String experimentId) {
    this.experimentId = experimentId;
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
  @JsonProperty("deplId")
  public String getDeplId() {
    return deplId;
  }
  public void setDeplId(String deplId) {
    this.deplId = deplId;
  }

  
  /**
   **/
  
  @ApiModelProperty(value = "")
  @JsonProperty("deplName")
  public String getDeplName() {
    return deplName;
  }
  public void setDeplName(String deplName) {
    this.deplName = deplName;
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
  @JsonProperty("date")
  public Date getDate() {
    return date;
  }
  public void setDate(Date date) {
    this.date = date;
  }

  
  /**
   * deployed Virtual machines
   **/
  
  @ApiModelProperty(value = "deployed Virtual machines")
  @JsonProperty("vms")
  public List<VM> getVms() {
    return vms;
  }
  public void setVms(List<VM> vms) {
    this.vms = vms;
  }

  
  /**
   * list of computed measures
   **/
  
  @ApiModelProperty(value = "list of computed measures")
  @JsonProperty("measures")
  public List<Measure> getMeasures() {
    return measures;
  }
  public void setMeasures(List<Measure> measures) {
    this.measures = measures;
  }

  

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Snapshot snapshot = (Snapshot) o;
    return Objects.equals(id, snapshot.id) &&
        Objects.equals(experimentId, snapshot.experimentId) &&
        Objects.equals(name, snapshot.name) &&
        Objects.equals(deplId, snapshot.deplId) &&
        Objects.equals(deplName, snapshot.deplName) &&
        Objects.equals(description, snapshot.description) &&
        Objects.equals(date, snapshot.date) &&
        Objects.equals(vms, snapshot.vms) &&
        Objects.equals(measures, snapshot.measures);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, experimentId, name, deplId, deplName, description, date, vms, measures);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Snapshot {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    experimentId: ").append(toIndentedString(experimentId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    deplId: ").append(toIndentedString(deplId)).append("\n");
    sb.append("    deplName: ").append(toIndentedString(deplName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    date: ").append(toIndentedString(date)).append("\n");
    sb.append("    vms: ").append(toIndentedString(vms)).append("\n");
    sb.append("    measures: ").append(toIndentedString(measures)).append("\n");
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

