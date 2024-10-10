package src;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class StudentPage extends Application
{
	@Override
	public void start(Stage stage)
	{
		stage.setTitle("Support360 Student");
		
		//header
		Label header = new Label("Welcome, {Username}!");
		header.getStyleClass().add("label");
		
		//search bar
		Label searchLabel = new Label("Search for help:");
		searchLabel.getStyleClass().add("label");
		TextField searchField = new TextField();
		searchField.getStyleClass().add("text-field");
		Button searchButton = new Button("Search");
		searchButton.getStyleClass().add("button");
		
		//profile button
		Button profileButton = new Button("Profile");
		profileButton.getStyleClass().add("button");
		
		//logout button
		Button logoutButton = new Button("Logout");
		logoutButton.getStyleClass().add("button");
		
		//stack features
		VBox vbox = new VBox(15);
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().addAll(header, searchLabel, searchField, searchButton, profileButton, logoutButton);
		
		/*
		 * EVENT HANDLING
		 */
		//handle logout button
		logoutButton.setOnAction(event ->
		{
			LoginPage loginPage = new LoginPage();
			Stage loginStage= new Stage();
			loginPage.start(loginStage);
			stage.close();
		});
		
		//scene creation
		Scene scene = new Scene(vbox, 600, 500);
		scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
		
		//show stage
		stage.setScene(scene);
		stage.show();
	}
	
	public static void main(String[] args)
	{
		launch(args);
	}
}
