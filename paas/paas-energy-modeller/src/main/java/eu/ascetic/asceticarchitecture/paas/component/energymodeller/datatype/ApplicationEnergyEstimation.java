package eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

public class ApplicationEnergyEstimation {

	private String application_id;
	private String provider_id;
	private Duration period;
	private Energy totalEnergy;
	private Power avgPower;
	private Power maxPower;
	private Power minPower;
	
	public ApplicationEnergyEstimation(){};
	
	public ApplicationEnergyEstimation(String application_id,
			String provider_id, Duration period, Energy totalEnergy,
			Power avgPower, Power maxPower, Power minPower) {
		super();
		this.application_id = application_id;
		this.provider_id = provider_id;
		this.period = period;
		this.totalEnergy = totalEnergy;
		this.avgPower = avgPower;
		this.maxPower = maxPower;
		this.minPower = minPower;
	}

	public String getApplication_id() {
		return application_id;
	}

	public void setApplication_id(String application_id) {
		this.application_id = application_id;
	}

	public String getProvider_id() {
		return provider_id;
	}

	public void setProvider_id(String provider_id) {
		this.provider_id = provider_id;
	}

	public Duration getPeriod() {
		return period;
	}

	public void setPeriod(Duration period) {
		this.period = period;
	}

	public Energy getTotalEnergy() {
		return totalEnergy;
	}

	public void setTotalEnergy(Energy totalEnergy) {
		this.totalEnergy = totalEnergy;
	}

	public Power getAvgPower() {
		return avgPower;
	}

	public void setAvgPower(Power avgPower) {
		this.avgPower = avgPower;
	}

	public Power getMaxPower() {
		return maxPower;
	}

	public void setMaxPower(Power maxPower) {
		this.maxPower = maxPower;
	}

	public Power getMinPower() {
		return minPower;
	}

	public void setMinPower(Power minPower) {
		this.minPower = minPower;
	}

	@Override
	public String toString() {
		return "ApplicationEnergyMeasurement [application_id=" + application_id
				+ ", provider_id=" + provider_id + ", period=" + period
				+ ", totalEnergy=" + totalEnergy + ", avgPower=" + avgPower
				+ ", maxPower=" + maxPower + ", minPower=" + minPower + "]";
	}
	
	
}
