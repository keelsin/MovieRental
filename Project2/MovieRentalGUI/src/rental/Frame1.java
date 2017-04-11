package rental;
import java.awt.EventQueue;
import java.sql.*;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.swing.JPasswordField;

public class Frame1 {
 
	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame1 window = new Frame1();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	Connection con = null;
	private JPasswordField passwordField;
	private JPasswordField passwordField_1;
	/**
	 * Create the application.
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public Frame1() throws ClassNotFoundException, SQLException {
		initialize();
		con = LogInUtils.getConnection();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblMovieRentals = new JLabel("Movie Rentals");
		lblMovieRentals.setBounds(166, 11, 87, 32);
		frame.getContentPane().add(lblMovieRentals);
		
		JButton btnSignUp = new JButton("Sign up");
		btnSignUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Here is where we will call the Method that signs a user up
			}
		});
		btnSignUp.setBounds(154, 54, 89, 23);
		frame.getContentPane().add(btnSignUp);
		
		JTextArea textArea = new JTextArea();
		textArea.setBounds(233, 141, 96, 23);
		frame.getContentPane().add(textArea);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(242, 199, -83, 23);
		frame.getContentPane().add(passwordField);
		
		passwordField_1 = new JPasswordField();
		passwordField_1.setBounds(230, 175, 99, 20);
		frame.getContentPane().add(passwordField_1);
		
		JLabel lblNewLabel = new JLabel("Username");
		lblNewLabel.setBounds(143, 146, 80, 14);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setBounds(143, 178, 69, 14);
		frame.getContentPane().add(lblPassword);
		
		JLabel lblLogIn = new JLabel("Log in");
		lblLogIn.setBounds(173, 104, 80, 14);
		frame.getContentPane().add(lblLogIn);
	}
}
