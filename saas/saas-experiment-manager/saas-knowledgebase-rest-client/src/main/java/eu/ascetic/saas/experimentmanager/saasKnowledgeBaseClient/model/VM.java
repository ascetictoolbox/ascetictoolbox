package eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.*;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-06-28T16:11:54.305+02:00")
public class VM   {
  
  private String id = null;
  private String vmId = null;
  private String description = null;
  private List<String> events = new ArrayList<String>();

  
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
  @JsonProperty("vmId")
  public String getVmId() {
    return vmId;
  }
  public void setVmId(String vmId) {
    this.vmId = vmId;
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
   * List of event ID passing trought this vms
   **/
  
  @ApiModelProperty(value = "List of event ID passing trought this vms")
  @JsonProperty("events")
  public List<String> getEvents() {
    return events;
  }
  public void setEvents(List<String> events) {
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
    VM VM = (VM) o;
    return Objects.equals(id, VM.id) &&
        Objects.equals(vmId, VM.vmId) &&
        Objects.equals(description, VM.description) &&
        Objects.equals(events, VM.events);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, vmId, description, events);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VM {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    vmId: ").append(toIndentedString(vmId)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

