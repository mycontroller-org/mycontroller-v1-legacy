import java.util.List;

public class CalendarDay {

	private LinkedList<SensorActivation> sensorActivations = new LinkedList<SensorActivation>();

	public int getActivationAmount() {
		return sensorActivations.size();
	}

	public SensorActivation getActivation(int index) {
		return sensorActivations.get(index);
	}

	public void AddActivation(SensorActivation activation) {
		sensorActivations.add(activation);
	}

	public void RemoveActivation(int index) {
		sensorActivations.remove(index);
	}

}
