import java.util.HashMap;
import java.util.List;
import java.util.Date;

public class Calendar {

	private HashMap<long,CalendarDay> daysMap = new HashMap<long,CalendarDay>();

	private LinkedList<Sensor> sensors = new LinkedList<Sensor>();

	public CalendarDay getDay(Date day) {
		return daysMap.get(day.getTime());
	}

	public void createDay(Date day, CalendarDay calendarday) {
		if(daysMap.get(day.getTime()) == null)
			daysMap.put(day.getTime(),calendarday);
	}

	public void addSensor(Sensor sensor) {
		sensors.add(sensor);
	}

	public void removeSensor(int index) {
		sensors.remove(index);
	}

	public Sensor getSensor(int index) {
		return sensors.get(index);
	}

}
