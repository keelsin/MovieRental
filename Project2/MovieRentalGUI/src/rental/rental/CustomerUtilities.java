package rental;
import java.sql.*;
import java.util.Calendar;
import java.util.Scanner;

public class CustomerUtilities {

	public static void loginOptions(Connection conn, String customerId){

		Scanner read = new Scanner(System.in);
		System.out.println("Please enter the number for the desired option:");
		System.out.println("1 - Movie search. \n2 - View your list of rented movies. \n3 - Renew a rented movie's due date. \n4 - View a list of recommended movies.");
		int userChoice = read.nextInt();
		loginOptions(conn, userChoice, customerId);
		if(read.hasNext()){
			 userChoice = read.nextInt();
			 loginOptions(conn, userChoice, customerId);
		}

	}

	public static void loginOptions(Connection conn, int choice, String customerId) {
		switch(choice) {
		case 1:	// call movie search method 
			break;
		case 2: // method call to the listing of rented movies 
			ViewList(conn, customerId);
			break; 
		case 3: // method call to renew the movie
			RenewMovie(conn, customerId);
			break;
		case 4: // method call to view the recommended movies	
			break;
		}
	}
	public static void ViewList(Connection conn, String custID) {

		try {

			
			String CustIDSQL = "SELECT movie_id, due_date FROM rental WHERE customer_id = ? AND returned = 0";

			PreparedStatement preparedCustID = conn.prepareStatement(CustIDSQL);
			preparedCustID.setString(1, custID);

			ResultSet moviesForCustomer = preparedCustID.executeQuery();

			while(moviesForCustomer.next()){
				String due_date = moviesForCustomer.getDate("due_date").toString();
				String movieID = moviesForCustomer.getString("movie_id");
				String movieTitleSQL = "SELECT title FROM movie WHERE movie_id = ?";

				PreparedStatement preparedMovieID = conn.prepareStatement(movieTitleSQL);
				preparedMovieID.setString(1, movieID);

				ResultSet movieTitleRs = preparedMovieID.executeQuery();
				movieTitleRs.next();
				String movieTitle = movieTitleRs.getString("title");
				// not sure
				System.out.println("Your rent list:");
				System.out.println("Movie ID:\t" + movieID+ "\nMovie Title:\t" + movieTitle +  "\nDue Date:\t" + due_date);
					
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void RenewMovie(Connection conn, String custID){
		ViewList(conn, custID);
		System.out.println("Please enter the movie ID that you would like to renew");
		Scanner read = new Scanner(System.in);
		try {
		
			while(read.hasNext()){
				String movieID = read.next();
				String updateDueDateSQL = "UPDATE rental SET due_date = ? WHERE movie_id = " + movieID;
				String getDueDateSQL = "SELECT due_date FROM rental WHERE movie_id = " + movieID;
				
				PreparedStatement preparedUpdateDueDate = conn.prepareStatement(updateDueDateSQL);
				PreparedStatement preparedDueDate = conn.prepareStatement(getDueDateSQL);
				
				// get the result set of the movie that the user picked
				ResultSet dueDateRs = preparedDueDate.executeQuery();
				
				Calendar dueDateCal = Calendar.getInstance();
				dueDateRs.next();
				// get the due date of that movie
	
				Date dueDate = dueDateRs.getDate("due_date");
		
				 dueDateCal.setTime(dueDate); 

				// set that due date to be 3 weeks later						
				// dueDateCal.add(dueDateCal.DAY_OF_MONTH, 21);
				// date.get(1) --> 2017
				// date.get(2) --> 2 (march) 
				// date.get(3) --> 16 (day of month)
				 System.out.println(dueDateCal.getTime());
				 System.out.println(dueDateCal.get(3));
				 
				// Date newDueDate = dueDateCal.getTime();
			
				// update the due date
			/*	preparedUpdateDueDate.setDate(1, newDueDate);
				preparedUpdateDueDate.executeUpdate();
				System.out.println("Your due date has been renew!");
				*/
			}			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
}