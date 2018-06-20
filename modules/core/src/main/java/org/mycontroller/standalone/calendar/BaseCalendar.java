import java.util.List;

public class BaseCalendar {

	private LinkedList<Calendar> calendars;

	public int request(String request) {
		String[] tokens = request.split(" ");
		switch(tokens[0])
		{
			case "newCalendar":
				int index = calendars.size();
				calendars.add(new Calendar());
				//return index to frontend
				break;
			case "openCalendarDay":
				int index = Integet.parseInt(tokens[1]);
				long daytime = Long.parseInt(tokens[2]);
				Calendar calendar = calendars.get(index);
				calendar.getDay(daytime);
				//int activationSize = calendar

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

	}

}
