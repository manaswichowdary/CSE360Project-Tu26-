package src;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Random;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * AdminPage class for the Support360 application.
 * This class represents the administrative interface
 */
public class AdminPage extends Application
{
	/**
	 *The main entry point for the JavaFX application.
     * This method sets up the administrative user interface
     * 
     * @param stage The primary stage for this application
	 */
	@Override
	public void start(Stage stage)
	{
		stage.setTitle("Support360 Admin");
		
		DatabaseHelper dbHelper = new DatabaseHelper();
        

        
		
		// Set up page as grid
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.getStyleClass().add("grid-pane");
		
		//header
		Label header = new Label("Welcome, " + LoginPage.loggedInUsername + "!");
		header.getStyleClass().add("label");
		grid.add(header, 0, 1);
		
		//search bar
//		Label searchLabel = new Label("Search for help:");
//		searchLabel.getStyleClass().add("label");
//		TextField searchField = new TextField();
//		searchField.getStyleClass().add("text-field");
//		Button searchButton = new Button("Search");
//		searchButton.getStyleClass().add("button");
		
		//profile button
//		Button profileButton = new Button("Profile");
//		profileButton.getStyleClass().add("button");
		
		// create invite
		Button inviteButton = new Button("Invite");
		inviteButton.getStyleClass().add("button");
		grid.add(inviteButton, 0, 2);
		
		// list all accounts
		ListView listView = new ListView();
		grid.add(listView, 0, 3);
		//populate listview with list of users
			// reset account
		
			// delete account
		
			// change roles?
		
		//textbox
		TextField userInput = new TextField();
		userInput.getStyleClass().add("text-field");
        grid.add(userInput, 0, 4);
		
		//launch user
		Button editButton = new Button("Delete");
		editButton.getStyleClass().add("button");
		grid.add(editButton, 0, 5);
		
		Button changeOtp = new Button("Generate OTP");
		changeOtp.getStyleClass().add("button");
		grid.add(changeOtp, 1, 5);
		
		Button changeRole = new Button("Change Roles");
		changeRole.getStyleClass().add("button");
		grid.add(changeRole, 2, 5);
		
		//logout button
		Button logoutButton = new Button("Logout");
		logoutButton.getStyleClass().add("button");
		grid.add(logoutButton, 0, 6);
		
		//otp pin
		Label resetLabel = new Label("OTP: ");
		resetLabel.getStyleClass().add("text-field");
        grid.add(resetLabel, 0, 7);
        
        Label resetHolder = new Label();
        resetHolder.getStyleClass().add("text-field");
        grid.add(resetHolder, 1,7);
		
		//stack features
//		VBox vbox = new VBox(15);
//		vbox.setAlignment(Pos.CENTER);
//		vbox.getChildren().addAll(header, searchLabel, searchField, searchButton, profileButton, logoutButton);
		
		/*
		 * EVENT HANDLING
		 */
		//handle OTP button
		inviteButton.setOnAction(event ->
		{
			OtpPage otpPage = new OtpPage();
			Stage otpStage= new Stage();
			otpPage.start(otpStage);
			stage.close();
		});
				
		//handle logout button
		logoutButton.setOnAction(event ->
		{
			LoginPage loginPage = new LoginPage();
			Stage loginStage= new Stage();
			loginPage.start(loginStage);
			LoginPage.loggedInUsername = null;
			stage.close();
		});
		
		editButton.setOnAction(event ->
		{
			try {
	            dbHelper.connectToDatabase();
	            int id = dbHelper.getUserIdByUsername(userInput.getText());
	            dbHelper.deleteUser(id);
			}catch (SQLException e) {
	            e.printStackTrace();
	        } finally {
	            dbHelper.closeConnection();
	        }
			
			userInput.setText("");
		});
		
		changeOtp.setOnAction(event ->
		{
			try {
	            dbHelper.connectToDatabase();
	            int id = dbHelper.getUserIdByUsername(userInput.getText());
	            String tokenHolder = generateRandomString(7);
	            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
	            dbHelper.createResetToken(id, tokenHolder, currentTimestamp);
	            resetHolder.setText(tokenHolder);
			}catch (SQLException e) {
	            e.printStackTrace();
	        } finally {
	            dbHelper.closeConnection();
	        }
			
			
		});
		
//		changeRole.setOnAction(event ->
//		{
//			try {
//	            dbHelper.connectToDatabase();
//	            int id = dbHelper.getUserIdByUsername(userInput.getText());
//	            dbHelper.deleteUser(id);
//			}catch (SQLException e) {
//	            e.printStackTrace();
//	        } finally {
//	            dbHelper.closeConnection();
//	        }
//			
//			userInput.setText("");
//		});
		
		//scene creation
		Scene scene = new Scene(grid, 600, 500);
		scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
		
		//show stage
		stage.setScene(scene);
		stage.show();
	}
	
	/**
	 * The main method for launching the JavaFX application
	 * 
	 * @param args default args parameter
	 */
	public static void main(String[] args)
	{
		launch(args);
	}
	
	public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int charactersLength = characters.length();
        Random random = new Random(); // Create a new Random instance
        
        // Create a char array to store the random string
        char[] randomString = new char[length];
        
        // Generate random string
        for (int i = 0; i < length; i++) {
            randomString[i] = characters.charAt(random.nextInt(charactersLength));
        }
        
        // Return the random string as a new String object
        return new String(randomString);
    }
}
