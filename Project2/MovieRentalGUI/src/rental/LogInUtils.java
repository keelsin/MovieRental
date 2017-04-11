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
	
	public static void main(String[] args) {
	
	}
		
	public static Connection getConnection() throws SQLException, ClassNotFoundException {
	   	try{
	   		Class.forName("oracle.jdbc.driver.OracleDriver");
	   		String username = JOptionPane.showInputDialog("Enter your oracle username: ");
	   		String password = JOptionPane.showInputDialog("Enter your password: ");
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
	public static void newUser(String username, String password, String fullName, String phoneNum) throws SQLException, ClassNotFoundException{
		Connection conn = null;
		
			conn = getConnection();
			String salt = getSalt();
			String query = "INSERT INTO CUSTOMER VALUES(?,?,?,?,?)";
			String maxCustIDString = "";
			byte[] hashedPassword = hash(password, salt);
			
			PreparedStatement preparedCustID = conn.prepareStatement("SELECT MAX(customer_id) FROM customer");

			ResultSet rs = preparedCustID.executeQuery();
			int maxCustID = rs.getInt("customer_id");
			
			 // increment the current biggest customer ID to set it to the new customer
			maxCustID++;
			
			maxCustIDString += maxCustID;
						
			PreparedStatement user = conn.prepareStatement(query);
			
			user.setString(1, maxCustIDString);
			user.setString(2, fullName);
			user.setString(3, username);		
			user.setBytes(4, hashedPassword);
			user.setString(5, phoneNum);
			
			user.executeUpdate();
			System.out.println("New user registered!");
			conn.close();
	}			
	
	//Prompts the user to input a username and password, and creates an account for that user.
	/*
	public static void newUser() throws SQLException, ClassNotFoundException{	
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Enter your username: ");
		String username = sc.nextLine();
		//System.out.println(username);	
		
		System.out.println("Enter your password: ");
		String password = sc.nextLine();
		//System.out.println(password);	
		
		newUser(username, password);
	}
	*/
	
	//Takes a username and password returns true if they belong to a valid user
	public static boolean login(String username, String password)throws SQLException, ClassNotFoundException{
		
		Connection conn = getConnection();
		String getCustID = "SELECT customer_id FROM customer WHERE username = ?";
		String hashedPWDQuery = "SELECT salted FROM CUSTOMER WHERE customer_id = ? ";
		
		PreparedStatement saltQuery = conn.prepareStatement(hashedPWDQuery);
		PreparedStatement preparedCustID = conn.prepareStatement(getCustID);
		preparedCustID.setString(1, username);
		
		ResultSet rsCustomerID = preparedCustID.executeQuery();
		String customerID = rsCustomerID.getString("customer_id");
		
		saltQuery.setString(1, customerID);		
		ResultSet rs = saltQuery.executeQuery();
		// getting the salt code of the specified user
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
		
		conn.close();
		if(user.equals(username))
			return true;
		 
		if(user.isEmpty())
			return false;
		}
		return false;
	}
	
	//Prompts the user to input their login info, returns true if they are a valid user, false otherwise
	public static boolean login() throws SQLException, ClassNotFoundException{
		
		String username = JOptionPane.showInputDialog("Enter your username: ");
		String password = JOptionPane.showInputDialog("Enter your password: ");
		
		boolean result = login(username, password);
		
		return result;
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
