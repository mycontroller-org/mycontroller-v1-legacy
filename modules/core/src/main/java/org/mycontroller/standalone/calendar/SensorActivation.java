import java.util.Date;

public class SensorActivation {

	private String sensorName;

	private Date activationTime;

	SensorActivation(String newSensorName, long miliseconds)
	{
		setSensorName(newSensorName);
		setActivationTime(miliseconds);
	}

	public String getSensorName() {
		return sensorName;
	}

	public void setSensorName(String newSensorName) {
		sensorName = newSensorName;
	}

	public Date getActivationTime() {
		return activationTime;
	}

	public void setActivationTime(long miliseconds) {
		activationTime = new Date(miliseconds)
	}
}
