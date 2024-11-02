package src;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Random;

/**
 * OtpPage class for the Support360 application.
 * This class generates a one-time password (OTP) for account creation.
 */
public class OtpPage extends Application
{
	/**
	 *The main entry point for the JavaFX application.
     * This method sets up the user interface for generating a one-time password
	 */
	@Override
	public void start(Stage stage)
	{
		stage.setTitle("Generate OTP");
		
		/*
         * PAGE LAYOUT
         */

        // Set up page as grid
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.getStyleClass().add("grid-pane");

        // Username label and entry field
        Label userLabel = new Label("Select Roles:");
        userLabel.getStyleClass().add("label");
        grid.add(userLabel, 0, 1);
        
        //student checkbox at 0,2
        CheckBox studentCheck = new CheckBox("Student");
        studentCheck.getStyleClass().add("checkbox");
        grid.add(studentCheck, 0, 2);
        
      //student checkbox at 0,2
        CheckBox staffCheck = new CheckBox("Instructor");
        staffCheck.getStyleClass().add("checkbox");
        grid.add(staffCheck, 0, 3);
        
        //generate button
        Button genButton = new Button("Generate OTP");
        genButton.getStyleClass().add("button");
        grid.add(genButton, 0, 4);
        
        //otp label at 0,5  
        Label otpLabel = new Label("OTP: ");
        otpLabel.getStyleClass().add("label");
        grid.add(otpLabel, 0, 5);
        //otp result at 1,5
        
        Label otpResult = new Label("");
        otpResult.getStyleClass().add("label");
        grid.add(otpResult, 1, 5);
        
        //exit button
        Button exit = new Button("Exit");
        exit.getStyleClass().add("button");
        grid.add(exit, 0, 6);
        
		/*
		 * EVENT HANDLING
		 */
		//handle gen button
		genButton.setOnAction(event ->
		{
			String roles = "";
			if (studentCheck.isSelected() == true) {
				roles += "Student.";
			}
			if(staffCheck.isSelected() == true) {
				roles+= "Instructor.";
			}
			
			DatabaseHelper dbHelper = new DatabaseHelper();
			String otpHolder = generateRandomString(7);
			
			try {
	            dbHelper.connectToDatabase();
	            dbHelper.createInvitation(otpHolder, roles);
	            otpResult.setText(otpHolder);
			} catch (SQLException e) {
	            e.printStackTrace();
	        } finally {
	            dbHelper.closeConnection();
	        }
		});
		
		//back button
		exit.setOnAction(event ->
		{
			AdminPage adminPage = new AdminPage();
			Stage adminStage= new Stage();
			adminPage.start(adminStage);
			stage.close();
		});
		
		//scene creation
		Scene scene = new Scene(grid, 600, 500);
		scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
		
		//show stage
		stage.setScene(scene);
		stage.show();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		launch(args);
	}
	
	/**
	 * @param length
	 * @return
	 */
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
