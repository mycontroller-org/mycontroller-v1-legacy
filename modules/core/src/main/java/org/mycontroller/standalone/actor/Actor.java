package org.mycontroller.standalone.actor;

import java.util.Scanner;
import org.mycontroller.standalone.calendar.BaseCalendar;

public class Actor {
	static BaseCalendar baseCalendar;
	private static Scanner sc;
	private static Scanner sc2;
	
	public static void main(String[] args) {
		sc = new Scanner(System.in);
		String usrInput;
		
		do {
			usrInput = sc.next();
			
			switch(usrInput) {
				case "createCalendar":
					baseCalendar.request(usrInput);
					break;
				case "removeCalendar":
				String name = usrInput.split(" ")[1];
				if(areYouSure(name))
						//name
						baseCalendar.request(usrInput);
					break;
				case "editCalendarName":
					//old name and new name
					baseCalendar.request(usrInput);
					break;
				case "openCalendar":
					//name
					baseCalendar.request(usrInput);
					break;
				case "openCalendarDay":
					//day
					baseCalendar.request(usrInput);
					break;
				case "help":
					showOptions();
					break;
			}
		}while(usrInput != "exit");
	}
	
	public void eventOperation(String event) {
		switch(event) {
			case "removeEvent":
				break;
			case "addSensor":
				break;
			case "deleteSensor":
				break;
		}
	}
	
	private static void showOptions() {
		System.out.println("Options:");
		System.out.println("createCalendar - creates new calendar");
		System.out.println("removeCalendar [name] - removes calendar of name "
				+ "[name]");
		System.out.println("editCalendarName [old name] [new name] - replaces a "
				+ "calendar's old name with a new name");
		System.out.println("openCalendar [name] - opens calendar of name [name]");
		System.out.println("openCalendarDay [name] [day] - opens visualization of "
				+ "a specific day [day] on a specific calendar of name [name]");
		System.out.println("exit - exit system");
	}
	
	private static boolean areYouSure(String name) {
		sc2 = new Scanner(System.in);
		String usrInput;
		while(true) {
			System.out.println("Are you sure you want to remove calendar "+name+"? y/n");
			usrInput = sc2.next();
			if(usrInput == "y") return true;
			else if (usrInput == "n") return false;
		}
	}
}
