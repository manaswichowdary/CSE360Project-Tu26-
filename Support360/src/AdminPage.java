package src;
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
		
		//launch user
		Button editButton = new Button("Admin Tools");
		editButton.getStyleClass().add("button");
		grid.add(editButton, 0, 4);
		
		//logout button
		Button logoutButton = new Button("Logout");
		logoutButton.getStyleClass().add("button");
		grid.add(logoutButton, 0, 5);
		
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
	 * @param args
	 */
	public static void main(String[] args)
	{
		launch(args);
	}
}
