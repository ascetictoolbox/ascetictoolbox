package data;

public class Sample {

	private long timestamp;
	private String power;
	private String cpu;
	private String memory;
	
	public Sample(long timestamp, String power, String cpu, String memory) {
		super();
		this.timestamp = timestamp;
		this.power = power;
		this.cpu = cpu;
		this.memory = memory;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getPower() {
		return power;
	}
	public void setPower(String power) {
		this.power = power;
	}
	public String getCpu() {
		return cpu;
	}
	public void setCpu(String cpu) {
		this.cpu = cpu;
	}
	public String getMemory() {
		return memory;
	}
	public void setMemory(String memory) {
		this.memory = memory;
	}
	
	
	
}
