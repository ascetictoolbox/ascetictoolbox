package eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Metric;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.*;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-06-28T16:11:54.305+02:00")
public class KPI   {
  
  private String id = null;
  private String name = null;
  private String description = null;
  private List<Metric> metrics = new ArrayList<Metric>();

  
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
  @JsonProperty("metrics")
  public List<Metric> getMetrics() {
    return metrics;
  }
  public void setMetrics(List<Metric> metrics) {
    this.metrics = metrics;
  }

  

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KPI KPI = (KPI) o;
    return Objects.equals(id, KPI.id) &&
        Objects.equals(name, KPI.name) &&
        Objects.equals(description, KPI.description) &&
        Objects.equals(metrics, KPI.metrics);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, metrics);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KPI {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    metrics: ").append(toIndentedString(metrics)).append("\n");
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

