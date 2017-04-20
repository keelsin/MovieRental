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
		String title;
		String movieID;
		int quantity;
		
		System.out.println("Please enter a title, category or any actor's full name");
		Scanner read = new Scanner(System.in);		
		try {
			while(read.hasNext()){
				String search = read.nextLine();

				// always upper case the user input just to make sure
				search = search.toUpperCase();
				String browseQuery = "SELECT title, movie_id, available_quantity FROM movie WHERE title = " + "'" + search + "'" ;
				PreparedStatement preparedBrowse = conn.prepareStatement(browseQuery);

				ResultSet rs = preparedBrowse.executeQuery();
				// using the user input as a title, if theres no result 
				// it will check whether the user input is a category 
				if(!rs.isBeforeFirst()){
					String browseCategoryQuery = "SELECT title, movie_id, available_quantity FROM movie WHERE category = " + "'" + search + "'" ;
					PreparedStatement preparedCategoryBrowse = conn.prepareStatement(browseCategoryQuery);

					ResultSet rsCategory = preparedCategoryBrowse.executeQuery();
					// if not a category, then check if its a movie actor
					if(!rsCategory.isBeforeFirst()){
						String browseActorQuery = "SELECT m.title, m.movie_id, m.available_quantity FROM movie m "
												+ "LEFT JOIN movie_actor ma ON m.movie_id = ma.movie_id "
												+ "LEFT JOIN actor a ON ma.actor_id = a.actor_id "
												+ "WHERE a.name =" + "'" + search + "'" ;
						
						PreparedStatement preparedActorBrowse = conn.prepareStatement(browseActorQuery);
					
						ResultSet rsActor = preparedActorBrowse.executeQuery();
						rsActor.next();
						System.out.println(rsActor.getString("title"));
						if(!rsActor.isBeforeFirst()) {
							System.out.println("Sorry! Nothing was found.\nPlease try again!" + "\n");

						} else {	
							while(rsActor.next()){
							title = rsActor.getString("title");
							movieID = rsActor.getString("movie_id");
							quantity = rsActor.getInt("available_quantity");
							System.out.println("Movie Title:\t" + title + "\nMovie ID:\t" + movieID + "\nAvailable Quantity:\t" + quantity);
							}
						}
					} else {
						while(rsCategory.next()){
						title = rsCategory.getString("title");
						movieID = rsCategory.getString("movie_id");
						quantity = rsCategory.getInt("available_quantity");
						System.out.println("Movie Title:\t" + title + "\nMovie ID:\t" + movieID + "\nAvailable Quantity:\t" + quantity);
						}
						
					}		
				} else {
					while(rs.next()){
					title = rs.getString("title");
					movieID = rs.getString("movie_id");
					quantity = rs.getInt("available_quantity");
					System.out.println("Movie Title:\t" + title + "\nMovie ID:\t" + movieID + "\nAvailable Quantity:\t" + quantity);
					}
				}

			}

		} catch (SQLException e){
			if(e.getMessage().equals("Exhausted Resultset"))
				System.out.println("Sorry! Nothing was found.\nPlease try again!" + "\n");
			
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
			if(e.getMessage().equals("Exhausted Resultset"))
				System.out.println("You have no rentals.");
				
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
			if(e.getMessage().equals("Exhausted Resultset"))
				System.out.println("Cannot find movie with that ID! \nPlease try again!" + "\n");
			
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
				preparedInsert.setInt(2, 0);
				preparedInsert.setString(3, movieID);
				preparedInsert.setString(4, custID);
				preparedInsert.setDate(5,  dueDate);
				preparedInsert.setString(6, "0");
				preparedInsert.setDate(7, todaySQL);

				preparedInsert.executeUpdate();

				//  INSERT a row in the delivery table using the same rental id  

				String insertDelivery = "INSERT INTO delivery VALUES(?,?,?)";

				PreparedStatement preparedInDeliv = conn.prepareStatement(insertDelivery);
				// not sure
				PreparedStatement preparedCarID = conn.prepareStatement("SELECT MAX(plate_number) FROM cars");
				ResultSet carIdRS = preparedCarID.executeQuery();
				
				carIdRS.next();
				int carId = carIdRS.getInt(1);
			
				preparedInDeliv.setString(1, custID);
				preparedInDeliv.setInt(2, carId);
				preparedInDeliv.setInt(3, rentalId);

				preparedInDeliv.executeUpdate();

				System.out.println("Thank you for renting!");
				System.out.println("Your delivery will be coming shortly!");
				System.out.println("Due date for movie:\t" + movieID + "\twill be on:\t" + dueDate.toString());	
			}


		} catch (SQLException e) {
			if(e.getMessage().equals("Exhausted Resultset"))
				System.out.println("Cannot find movie with that ID! \nPlease try again!" + "\n");
			
			e.printStackTrace();
		}



	}





}