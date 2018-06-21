import java.util.Scanner;
import org.mycontroller.standalone.calendar.BaseCalendar;

public class Actor {
	BaseCalendar baseCalendar;
	
	public static void main(String[] args) {
		Actor actor;
		
		Scanner sc = new Scanner(System.in);
		String usrInput;
		
		do {
			usrInput = sc.next();
			
			switch(usrInput) {
				case "createCalendar":
					this.baseCalendar.request(usrInput);
					break;
				case "removeCalendar":
					if(this.areYouSure(name))
						//name
						this.baseCalendar.request(usrInput);
					break;
				case "editCalendarName":
					//old name and new name
					this.baseCalendar.request(usrInput);
					break;
				case "openCalendar":
					//name
					this.baseCalendar.request(usrInput);
					break;
				case "openCalendarDay":
					//day
					this.baseCalendar.request(usrInput);
					break;
				case "help":
					this.showOptions();
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
	
	private void showOptions() {
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
	
	private boolean areYouSure(String name) {
		Scanner sc = new Scanner(System.in);
		String usrInput;
		while(1) {
			System.out.println("Are you sure you want to remove calendar "+name+"? y/n");
			usrInput = sc.next();
			if(usrInput == "y") return true;
			else if (usrInput == "n") return false;
		}
	}
}
