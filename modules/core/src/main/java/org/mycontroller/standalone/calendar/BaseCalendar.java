import java.util.List;

public class BaseCalendar {

	private LinkedList<Calendar> calendars;
	
	private int calendarIndex;
	private Calendar calendar;
	private CalendarDay day;

	public int request(String request) {
		String[] tokens = request.split(" ");
		switch(tokens[0])
		{
			case "newCalendar":
				int index = calendars.size();
				addCalendar(new Calendar());
				//return index to frontend
				break;
			case "openCalendar":
				calendarIndex = Integer.parseInt(tokens[1]);
				calendar = calendars.get(calendarIndex);
				break;
			case "deleteCalendar":
				removeCalendar(calendarIndex);
				break;
			case "addSensor":
				int index = Integer.parseInt(tokens[1]);
				//get sensor from index
				calendar.addSensor(sensor);
				break;
			case "deleteSensor":
				int index = Integer.parseInt(tokens[1]);
				calendar.removeSensor(index);
				break;
			case "deleteActivation":
				int index = Integer.parseInt(tokens[1]);
				day.removeActivation(index);
				break;
			case "openCalendarDay":
				long daytime = Long.parseInt(tokens[1]);
				day = calendar.getDay(daytime);
				int activationSize = day.getActivationAmount();
				//return activationSize to frontend
				for(int i = 0; i < activationSize; i++)
				{
					SensorActivation activation = day.getActivation(i);
					//return activation to frontend
				}
				break;
		}
		return 0;
	}

	public void addCalendar(Calendar cal) {
		calendars.add(cal);
	}

	public void removeCalendar(int index) {
		calendars.remove(index);
	}

	public void onTimerActivate(Timer timer) {
		//timer activation call this function
	}

}
