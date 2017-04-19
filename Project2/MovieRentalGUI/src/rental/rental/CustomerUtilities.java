package rental;
import java.sql.*;
import java.util.Calendar;
import java.util.Scanner;

public class CustomerUtilities {

	public static void loginOptions(Connection conn, String customerId){

		Scanner read = new Scanner(System.in);
		System.out.println("Please enter the number for the desired option:");
		System.out.println("1 - Movie search. \n2 - View your list of rented movies. \n3 - Renew a rented movie's due date. \n4 - View a list of recommended movies.");
		System.out.println("5 - Rent a movie which we will deliever to you.\n");
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
			BrowseMovie(conn, customerId);
			break;

		case 2: // method call to the listing of rented movies 
			ViewList(conn, customerId);
			break; 
		case 3: // method call to renew the movie
			RenewMovie(conn, customerId);
			break;
		case 4: // method call to view the recommended movies	
			Recommends(conn, customerId);
			break;
		case 5: 
			PlaceDelivery(conn, customerId);
		}
	}

	// this is the browse for employees since thats what i had to do 
	// but should be the same for customers which is what roan had to do 
	public static void BrowseMovie(Connection conn, String custID){
		// i dont think u need customer id 
		System.out.println("Please enter a title, category or any actor name");
		Scanner read = new Scanner(System.in);
		try {
			while(read.hasNext()){
			String search = read.next();
			
			String browseQuery = "SELECT title, movie_id FROM movie WHERE title =" + "'" + search + "'" ;
			PreparedStatement preparedBrowse = conn.prepareStatement(browseQuery);
			
			ResultSet rs = preparedBrowse.executeQuery();

			if(!rs.next()){
				System.out.println("hello");
				String browseCategoryQuery = "SELECT title, movie_id FROM movie WHERE category =" + "'" + search + "'" ;
				PreparedStatement preparedCategoryBrowse = conn.prepareStatement(browseCategoryQuery);
				
				ResultSet rsCategory = preparedCategoryBrowse.executeQuery();
				
				if(!rsCategory.next()){
					String browseActorQuery = "SELECT title, movie_id FROM movie WHERE category =" + "'" + search + "'" ;
					PreparedStatement preparedActorBrowse = conn.prepareStatement(browseActorQuery);
					
					ResultSet rsActor = preparedCategoryBrowse.executeQuery();
					
					if(!rsActor.next())
						System.out.println("Sorry! Nothing was found.\nPlease try again!");
				}
				
			} else {
				System.out.println(rs.getString(1));
			}
			
			}

		} catch (SQLException e){
			e.printStackTrace();
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

				Calendar today = Calendar.getInstance();
				Calendar dueDateCal = Calendar.getInstance();
				dueDateRs.next();
				// get the due date of that movie

				Date dueDate = dueDateRs.getDate("due_date");

				dueDateCal.setTime(dueDate); 

				// set that due date to be 3 weeks later						
				dueDateCal.add(dueDateCal.DAY_OF_MONTH, 21);

				System.out.println(dueDateCal.getTime());				 
				java.sql.Date newSqlDueDate = new java.sql.Date(dueDateCal.getTimeInMillis());

				// update the due date
				preparedUpdateDueDate.setDate(1, newSqlDueDate);
				preparedUpdateDueDate.executeUpdate();
				System.out.println("Your due date has been renew!");
				System.out.println("New due date:\t" + newSqlDueDate);				
			}			

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void Recommends(Connection conn, String customerId) {

		try{
			String CountSQL = "SELECT count(movie_id) FROM rental WHERE customer_id = ?";

			PreparedStatement countRentals = conn.prepareStatement(CountSQL);
			countRentals.setString(1, customerId);

			ResultSet rs = countRentals.executeQuery();
			rs.next();
			int count = rs.getInt("count(movie_id)");

			//If the customer has not rented a movie we will recommend him a movie from a category with the most stock
			if (count == 0){
				String recommendedMovies = "SELECT title FROM movie WHERE category="
						+ "(SELECT category FROM movie group by category HAVING count(category)="
						+ "(SELECT MAX(num) FROM(SELECT count(category) AS num FROM movie group by category)))";
				PreparedStatement recommends = conn.prepareStatement(recommendedMovies);
				ResultSet results = recommends.executeQuery();
				while(results.next()){
					System.out.println("The title is\t "+ results.getString("title"));
				}
			}else{
				//If you have not previously recommended a movie then it will recommend a movie that has the most stock
				//If you have rented before it will recommend a movie that you have not seen in the category you rent the most from
				String recommendedMovies = "SELECT title FROM movie WHERE category = "
						+ "(SELECT category FROM rental JOIN movie using(movie_id) group by category HAVING count(category) = "
						+ "(SELECT MAX(num) FROM (SELECT count(category) as num FROM movie JOIN rental using(movie_id) WHERE customer_id = ? group by category))) "
						+ "MINUS "
						+ "SELECT title FROM rental JOIN movie using(movie_id) WHERE category = "
						+ "(SELECT category as genre FROM rental JOIN movie using(movie_id) group by category HAVING count(category) = "
						+  "(SELECT MAX(num) FROM (SELECT count(category) as num FROM movie JOIN rental using(movie_id) WHERE customer_id = ? "
						+ "group by category)))";
				PreparedStatement recommends = conn.prepareStatement(recommendedMovies);
				recommends.setString(1, customerId);
				recommends.setString(2, customerId);
				ResultSet results = recommends.executeQuery();
				if(!results.next())
					System.out.print("There are no movies to recommend, you must be a true movie connoisseur!");
				else
					do{
						System.out.println("The title is\t "+ results.getString("title"));
					}while(results.next());
			}

		} catch(SQLException e){
			e.printStackTrace();
		}

	}

	public static void PlaceDelivery(Connection conn, String custID){

		// insert into rental table a rental
		System.out.println("Please enter the movie ID of the movie which you'd like to rent.");
		Scanner read = new Scanner(System.in);
		try {

			while(read.hasNext()){
				String movieID = read.next();
				String insertRental = "INSERT INTO rental VALUES(?,?,?,?,?,?,?)";


				PreparedStatement preparedInsert = conn.prepareStatement(insertRental);
				// get the biggest rental id 
				PreparedStatement preparedRentalID = conn.prepareStatement("SELECT MAX(rental_id) FROM rental");

				ResultSet rentalIdRS = preparedRentalID.executeQuery();
				rentalIdRS.next();
				int rentalId = rentalIdRS.getInt(1);
				// increment the rental id by one to set it for the newest rental 
				rentalId++;

				// get today's date to set it as the rental's date 
				Calendar today = Calendar.getInstance();
				java.sql.Date todaySQL = new java.sql.Date(today.getTimeInMillis());

				// add 21 days to today's date to make it the due date
				today.add(today.DAY_OF_MONTH, 21);
				java.sql.Date dueDate = new java.sql.Date(today.getTimeInMillis());

				preparedInsert.setInt(1, rentalId);
				preparedInsert.setString(2, "ONLINE");
				preparedInsert.setString(3, movieID);
				preparedInsert.setString(4, custID);
				preparedInsert.setDate(5,  dueDate);
				preparedInsert.setString(6, "0");
				preparedInsert.setDate(7, todaySQL);
				System.out.println("hello!");

				//System.out.println("rental id\t" + rentalId +"\nmovieID\t" + movieID + "\ncustomerID\t" + 
				//					custID +"\ndueDate\t" + dueDate.toString() + "\ntoday\t" + todaySQL.toString());
				
				preparedInsert.executeUpdate();
				System.out.println("hello again!");
				//  INSERT a row in the delivery table using the same rental id  
				
				String insertDelivery = "INSERT INTO delivery VALUES(?,?,?)";
				
				PreparedStatement preparedInDeliv = conn.prepareStatement(insertDelivery);
				// not sure
				PreparedStatement preparedCarID = conn.prepareStatement("SELECT MAX(car_id) FROM car");
				ResultSet carIdRS = preparedCarID.executeQuery();
				
				int carId = carIdRS.getInt(1);
				carId++;
				
				preparedInDeliv.setString(1, custID);
				preparedInDeliv.setInt(2, carId);
				preparedInDeliv.setInt(3, rentalId);
				
				preparedInDeliv.executeUpdate();
				
				System.out.println("Thank you for renting!");
				System.out.println("Your delivery will be coming shortly!");
				System.out.println("Due date for the movie:\t" + movieID + "\twill be on:\t" + dueDate.getTime());
			}


		} catch (SQLException e) {
			e.printStackTrace();
			//System.out.println(e.getMessage());
		}



	}





}