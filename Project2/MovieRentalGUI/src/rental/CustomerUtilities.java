import java.sql.*;
import java.util.Scanner;

public class CustomerUtilities {

	public static void loginOptions(String customerId){

		Scanner read = new Scanner(System.in);
		System.out.println("Please enter the number for the desired option:");
		System.out.println("1 - Movie search. \n2 - View your list of rented movies. \n3 Renew a rented movie's due date. \n4 - View a list of recommended movies.");

		while(read.nextBoolean()){
			int userChoice = read.nextInt();
			loginOptions(userChoice, customerId);
		}

	}

	public static void loginOptions(int choice, String customerId) {
		switch(choice) {
		case 1:	// call movie search method 
			break;
		case 2: // method call to the listing of rented movies 
			ViewList(customerId);
			break;
		case 3: // method call to renew the movie
			break;
		case 4: // method call to view the recommended movies	
			break;
		}
	}
	public static void ViewList(String custID) {

		Connection conn;

		try {
			conn = LogInUtils.getConnection();
			
			String CustIDSQL = "SELECT movie_id FROM rental WHERE customer_id = ? AND returned = 0";

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
				String movieTitle = movieTitleRs.getString("title");
				// not sure
				System.out.println("Movie Title:\t" + movieTitle + "\tDue Date:\t" + due_date + "\tMovie ID:\t" + movieID);
					
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void RenewMovie(String custID){
		ViewList(custID);
		System.out.println("Please enter the movie ID that you would like to renew");
		Scanner read = new Scanner(System.in);
		Connection conn;
		try {
			conn = LogInUtils.getConnection();
		
			while(read.hasNext()){
				String movieID = read.next();
				String updateDueDateSQL = "UPDATE rental SET due_date = ? WHERE movie_id = " + movieID;
				String getDueDateSQL = "SELECT due_date FROM rental WHERE movie_id = " + movieID;
				
				PreparedStatement preparedUpdateDueDate = conn.prepareStatement(updateDueDateSQL);
				PreparedStatement preparedDueDate = conn.prepareStatement(getDueDateSQL);
				
				// get the result set of the movie that the user picked
				ResultSet dueDateRs = preparedDueDate.executeQuery();
				// get the due date of that movie
				Date dueDate = dueDateRs.getDate("due_date");
				
				// set that due date to be 3 weeks later		
				//	dueDate.getDate();
				
				// update the due date
				preparedUpdateDueDate.setDate(1, dueDate);
				preparedUpdateDueDate.executeUpdate();
				
			}			
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
}