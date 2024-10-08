import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class AccountCreation extends Application
{
	@Override
	public void start(Stage stage)
	{
		stage.setTitle("Support360 Account Creation");
		
		/*
		 * PAGE LAYOUT
		 */
		
		//set up page as grid
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.getStyleClass().add("grid-pane");
		
		//username
		Label userLabel = new Label("Username:");
		userLabel.getStyleClass().add("label");
		grid.add(userLabel, 0, 1);
		TextField userField = new TextField();
		userField.getStyleClass().add("username-field");
		grid.add(userField, 1, 1);
		
		//password 1
		Label passLabel1 = new Label("Password:");
		passLabel1.getStyleClass().add("label");
		grid.add(passLabel1, 0, 2);
		PasswordField passField1 = new PasswordField();
		passField1.getStyleClass().add("password-field");
		grid.add(passField1, 1, 2);
		
		//password 2
		//Password label and entry field
		Label passLabel2 = new Label("Confirm Password:");
		passLabel2.getStyleClass().add("label");
		grid.add(passLabel2, 0, 3);
		PasswordField passField2 = new PasswordField();
		passField2.getStyleClass().add("password-field");
		grid.add(passField2, 1, 3);
		
		//password does not match label
		Label passMatch = new Label("Warning: Passwords don't match.");
		passMatch.getStyleClass().add("label");
		grid.add(passMatch, 0, 4);
		passMatch.setVisible(false);
		
		Label emptyWarn = new Label("Warning: All inputs required.");
		emptyWarn.getStyleClass().add("label");
		grid.add(emptyWarn, 0, 5);
		emptyWarn.setVisible(false);
		
		Button createActButton = new Button("Create Account");
		createActButton.getStyleClass().add("button");
		grid.add(createActButton, 0, 6);
		
		/*
		 * EVENT HANDLING
		 */
		
		createActButton.setOnAction(event ->
		{
			if(userField.getText().equals("") || passField1.getText().equals("") || passField2.getText().equals("")) {
				//error
				emptyWarn.setVisible(true);
				passMatch.setVisible(false);
			} else if (passField1.getText().equals(passField2.getText()) != true) {
				//if fields are not blank
				//and if passwords match:
				passMatch.setVisible(true);
				emptyWarn.setVisible(false);
			} else {
				//save to database with incomplete flag and otp
				LoginPage loginPage = new LoginPage();
				Stage loginStage = new Stage();
				loginPage.start(loginStage);
				stage.close();
			}
		});
		
		//create attempt
			//check if any fields are blank 
			//check passwords validation
		
		/*
		 * SCENE CREATION
		 */
		
		Scene scene = new Scene(grid, 600, 500);
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