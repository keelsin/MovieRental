package rental;

import java.sql.Connection;
import java.util.Scanner;

public class EmployeeUtilities {
	public static void loginOptions(Connection conn, String customerId){

		Scanner read = new Scanner(System.in);
		System.out.println("Please enter the number for the desired option:");
		System.out.println("1 - Movie search. \n2 - View customers with overdue movies. "
				+ "\n3 - Create a rental. \n4 - Process a return. \n5 - Add a new movie");
		int userChoice = read.nextInt();
		loginOptions(conn, userChoice, customerId);
		if(read.hasNext()){
			 userChoice = read.nextInt();
			 loginOptions(conn, userChoice, customerId);
		}

	}
	
	public static void loginOptions(Connection conn, int choice, String customerId) {
		switch(choice) {
		case 1:	
			//Movie Search method call goes here
			break;
			
		case 2: 
			//View customers with overdue movies method call here
			break; 
		case 3: 
			//Creating a new rental method call goes here
			break;
		case 4: 
			//Processing a return method call goes here
			break;
		case 5:
			//Add a new movie to the database method call goes here
			break;
		}
	}
}
