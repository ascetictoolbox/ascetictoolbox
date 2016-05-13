package eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Item;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.*;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-11T09:58:20.351+02:00")
public class Measure   {
  
  private String id = null;
  private String description = null;
  private List<Item> refersTo = new ArrayList<Item>();
  private String value = null;
  private String metric = null;

  
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
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   * list of items (scope) on which the measure has been computed
   **/
  
  @ApiModelProperty(value = "list of items (scope) on which the measure has been computed")
  @JsonProperty("refersTo")
  public List<Item> getRefersTo() {
    return refersTo;
  }
  public void setRefersTo(List<Item> refersTo) {
    this.refersTo = refersTo;
  }

  
  /**
   * measure value
   **/
  
  @ApiModelProperty(value = "measure value")
  @JsonProperty("value")
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }

  
  /**
   * id of the metric
   **/
  
  @ApiModelProperty(value = "id of the metric")
  @JsonProperty("metric")
  public String getMetric() {
    return metric;
  }
  public void setMetric(String metric) {
    this.metric = metric;
  }

  

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Measure measure = (Measure) o;
    return Objects.equals(id, measure.id) &&
        Objects.equals(description, measure.description) &&
        Objects.equals(refersTo, measure.refersTo) &&
        Objects.equals(value, measure.value) &&
        Objects.equals(metric, measure.metric);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, description, refersTo, value, metric);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Measure {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    refersTo: ").append(toIndentedString(refersTo)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    metric: ").append(toIndentedString(metric)).append("\n");
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

