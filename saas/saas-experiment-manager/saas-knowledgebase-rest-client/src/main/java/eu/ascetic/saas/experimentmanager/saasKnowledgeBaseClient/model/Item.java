package eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-25T15:22:24.802+02:00")
public class Item   {
  
  private String category = null;
  private String reference = null;

  
  /**
   * Item category (VM, event) describing a part of a scope
   **/
  
  @ApiModelProperty(value = "Item category (VM, event) describing a part of a scope")
  @JsonProperty("category")
  public String getCategory() {
    return category;
  }
  public void setCategory(String category) {
    this.category = category;
  }

  
  /**
   * Complete item reference
   **/
  
  @ApiModelProperty(value = "Complete item reference")
  @JsonProperty("reference")
  public String getReference() {
    return reference;
  }
  public void setReference(String reference) {
    this.reference = reference;
  }

  

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Item item = (Item) o;
    return Objects.equals(category, item.category) &&
        Objects.equals(reference, item.reference);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, reference);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Item {\n");
    
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    reference: ").append(toIndentedString(reference)).append("\n");
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

