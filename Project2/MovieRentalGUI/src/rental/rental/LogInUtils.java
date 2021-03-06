package rental;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.math.BigInteger;
import java.sql.*;
import java.util.Scanner;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.JOptionPane;

public class LogInUtils {

	private static SecureRandom random = new SecureRandom();
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		askForLogIn();
	}
	
	public static void askForLogIn(){   		
   		try {
			Connection conn = null;
			conn = getConnection("A1537595", "database2");
			if(conn != null){	
				
				String option = JOptionPane.showInputDialog("Welcome to the movie rentals!\nWould you like to log in or register?\n" +
															"Enter l - for login\nEnter r - to register");
				if(option.equals("l")){
				// for user login
				String customerUserName = JOptionPane.showInputDialog("Enter your movie rental username: ");
		   		String customerPassword = JOptionPane.showInputDialog("Enter your password: ");
		   		login(conn, customerUserName, customerPassword);			
				
				} else if (option.equals("r")) {
				// new user
				String customerName = JOptionPane.showInputDialog("Enter your full name: ");
				String customerAddress = JOptionPane.showInputDialog("Enter your home address:\n(Don't worry, just for delivery purposes!) ");
				String customerPhone = JOptionPane.showInputDialog("Enter your phone number: ");
				String customerUserName = JOptionPane.showInputDialog("Pick a movie rental username: ");
		   		String customerPassword = JOptionPane.showInputDialog("Enter your password: ");		   		
				
				newUser(conn, customerUserName, customerPassword, customerName, customerPhone, customerAddress);
				
				}
			}
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	
	public static Connection getConnection(String username, String password) throws SQLException, ClassNotFoundException {
	   	try{
	   		Class.forName("oracle.jdbc.driver.OracleDriver");
	 
	   		Connection connection = DriverManager.getConnection(
	   			"jdbc:oracle:thin:@198.168.52.73:1521:orad11g", username, password);
	   		// ask the user for their username and password in this method so we dont have it written there
	    System.out.println("Connected to database");
	    return connection;
	   	} catch (SQLException e){
	   		throw new SQLException(e.getMessage());
	   	
	  	} catch (ClassNotFoundException cnf) {
	  		//cnf.printStackTrace();
	   		throw new ClassNotFoundException(cnf.getMessage());
	   	}
	}

	//Takes a username and password and creates and account for that user
	// works as long as we dont initially populate the table from oracle!
	public static void newUser(Connection conn, String username, String password, String fullName, String phoneNum, String customerAddress) throws SQLException, ClassNotFoundException{	
			String salt = getSalt();
			String query = "INSERT INTO CUSTOMER VALUES(?,?,?,?,?,?)";
			byte[] hashedPassword = hash(password, salt);
			
			PreparedStatement preparedCustID = conn.prepareStatement("SELECT MAX(customer_id) FROM customer");

			ResultSet rs = preparedCustID.executeQuery();
			rs.next();
			int maxCustID = rs.getInt(1);
			 // increment the current biggest customer ID to set it to the new customer
			maxCustID++;
			
			PreparedStatement user = conn.prepareStatement(query);
			PreparedStatement address = conn.prepareStatement("INSERT INTO cust_address VALUES(?,?)");
			
			user.setInt(1, maxCustID);
			user.setString(2, fullName);
			user.setString(3, username);		
			user.setBytes(4, hashedPassword);
			user.setString(5,  salt);
			user.setString(6, phoneNum);
			
			address.setInt(1, maxCustID);
			address.setString(2, customerAddress);
			
			user.executeUpdate();
			address.executeQuery();
			
			System.out.println("New user registered!");
			conn.close();
	}			
	
	//Takes a username and password returns true if they belong to a valid user
	public static void login(Connection conn, String username, String password)throws SQLException, ClassNotFoundException{
		try {
			String getCustID = "SELECT customer_id FROM customer WHERE username = ?";
			String hashedPWDQuery = "SELECT salted FROM CUSTOMER WHERE customer_id = ? ";
			
			PreparedStatement saltQuery = conn.prepareStatement(hashedPWDQuery);
			PreparedStatement preparedCustID = conn.prepareStatement(getCustID);
			preparedCustID.setString(1, username);
			
			ResultSet rsCustomerID = preparedCustID.executeQuery();
			rsCustomerID.next();
			String customerID = rsCustomerID.getString("customer_id");		
			saltQuery.setString(1, customerID);			
			
			ResultSet rs = saltQuery.executeQuery();
			// getting the salt code of the specified user
			rs.next();
			String salt = rs.getString("salted");		
			// hashing the password using the same salt code
			byte[] hashed = hash(password, salt);
			
			// select user with the matching hashed password
			String checkQuery = "SELECT username FROM customer WHERE pswrd = ? ";
			
			PreparedStatement hashQuery = conn.prepareStatement(checkQuery);
			hashQuery.setBytes(1, hashed);
			ResultSet hashRS = hashQuery.executeQuery();
			while(hashRS.next()){	
				String user = hashRS.getString("username");
			
				if(user.equals(username)) 
					CustomerUtilities.loginOptions(conn, customerID);
				
				else if(user.isEmpty()) 
					System.out.println("Log in failed! Incorrect username or password");
				}
			} catch (SQLException e){
				if(e.getMessage().equals("Exhausted Resultset"))
					System.out.println("Cannot find username!");						
			}
	}
	
	
	//Helper Functions below:
	//getConnection() - obtains a connection
	//getSalt() - creates a randomly generated string 
	//hash() - takes a password and a salt as input and then computes their hash
		
	//Creates a randomly generated String
	public static String getSalt(){
		return new BigInteger(140, random).toString(32);
	}
	
	//Takes a password and a salt a performs a one way hashing on them, returning an array of bytes.
	public static byte[] hash(String password, String salt){
		try{
			SecretKeyFactory skf = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA512" );
	        
			/*When defining the keyspec, in addition to passing in the password and salt, we also pass in
			a number of iterations (1024) and a key size (256). The number of iterations, 1024, is the
			number of times we perform our hashing function on the input. Normally, you could increase security
			further by using a different number of iterations for each user (in the same way you use a different
			salt for each user) and storing that number of iterations. Here, we just use a constant number of
			iterations. The key size is the number of bits we want in the output hash*/ 
			PBEKeySpec spec = new PBEKeySpec( password.toCharArray(), salt.getBytes(), 1024, 256 );

			SecretKey key = skf.generateSecret( spec );
	        byte[] hash = key.getEncoded();
	        return hash;
        }catch( NoSuchAlgorithmException | InvalidKeySpecException e ) {
            throw new RuntimeException(e);
        }
	}	
}
