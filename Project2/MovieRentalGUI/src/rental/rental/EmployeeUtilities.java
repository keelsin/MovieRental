package rental;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
			BrowseMovie(conn);
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
	
	// this is the browse for employees since thats what i had to do 
		// but should be the same for customers which is what roan had to do 
		public static void BrowseMovie(Connection conn){
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
				if(e.getMessage().equals("Exhausted Resultset")){
					System.out.println("Sorry! Nothing was found.\nPlease try again!" + "\n");
				}
			}


		}
	
}
