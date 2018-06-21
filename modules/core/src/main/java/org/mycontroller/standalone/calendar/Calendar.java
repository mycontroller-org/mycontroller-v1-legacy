import java.util.HashMap;
import java.util.List;
import java.util.Date;

public class Calendar {

	private HashMap<String,CalendarDay> daysMap;
	private LinkedList<Timer> timers;
	private String name;
	
	Calendar(String name){
		this.name = name;
	}

	public CalendarDay getDay(Date day) {
		return null;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void createDay(Date day, CalendarDay calendarday) {

	}

	public void addTimer(Timer timer) {
		this.timers.add(timer);
	}

	public void removeTimer() {
		this.timers.remove(index);
	}

	public Sensor getAllTimersByName() {
		return ;
	}
}