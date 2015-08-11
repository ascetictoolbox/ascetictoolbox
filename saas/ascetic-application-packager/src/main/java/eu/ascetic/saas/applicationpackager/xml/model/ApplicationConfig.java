package eu.ascetic.saas.applicationpackager.xml.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="application-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationConfig {

	@XmlAttribute(name="name")
	private String name;
	
	@XmlAttribute(name="mode")
	private String mode;
	
	@XmlElement(name="node")
    private ArrayList<Node> nodes;
	
	public String getName() {
		return name;
	}
 
//	@XmlAttribute
	public void setName(String name) {
		this.name = name;
	}
	
	public String getMode() {
		return mode;
	}
 
//	@XmlAttribute
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public ArrayList<Node> getNodes() {
		return nodes;
	}
	
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}
	
	public void addNode(Node node) {
		if(nodes == null) nodes = new ArrayList<Node>();
		nodes.add(node);
	}
}
