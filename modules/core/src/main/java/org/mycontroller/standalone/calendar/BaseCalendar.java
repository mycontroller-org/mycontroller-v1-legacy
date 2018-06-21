package org.mycontroller.standalone.calendar;

import java.util.LinkedList;
import java.util.List;

import org.mycontroller.standalone.db.tables.Timer;

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
				addCalendar(new Calendar(Integer.toString(index)));
				//return index to frontend
				break;
			case "openCalendar":
				if(!getIndexByName(tokens[1]))
					System.out.println("Calendar does not exist!");
				else
					calendar = calendars.get(calendarIndex);
				break;
			case "deleteCalendar":
				if(!getIndexByName(tokens[1]))
					System.out.println("Calendar does not exist!");
				else
					removeCalendar(calendarIndex);
				break;
			case "editCalendarName":
				if(!getIndexByName(tokens[1]))
					System.out.println("Calendar does not exist!");
				else
					calendar.setName(tokens[2]);
				break;
			case "addSensor":
				int index1 = Integer.parseInt(tokens[1]);
				//get sensor from index
				calendar.addSensor(sensor);
				break;
			case "deleteSensor":
				int index1 = Integer.parseInt(tokens[1]);
				calendar.removeSensor(index1);
				break;
			case "deleteActivation":
				int index1 = Integer.parseInt(tokens[1]);
				day.removeActivation(index1);
				break;
			case "openCalendarDay":
				//arrumar
				long daytime = Long.parseLong(tokens[1]);
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
	
	private boolean getIndexByName(String name) {
		for(int index=0;index < calendars.size();index++) {
			if(calendars[index].getName() == tokens[1]) {
				this.calendarIndex = index;
				return true;
			}
		}
		return false;
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
