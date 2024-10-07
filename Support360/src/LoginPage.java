import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class LoginPage extends Application
{
	@Override
	public void start(Stage stage)
	{
		stage.setTitle("Support360 Login Page");
		
		/*
		 * PAGE LAYOUT
		 */
		
		//set up page as grid
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.getStyleClass().add("grid-pane");
		
		//Username label and entry field
		Label userLabel = new Label("Username:");
		userLabel.getStyleClass().add("label");
		grid.add(userLabel, 0, 1);
		TextField userField = new TextField();
		userField.getStyleClass().add("text-field");
		grid.add(userField, 1, 1);
		
		//Password label and entry field
		Label passLabel = new Label("Password:");
		passLabel.getStyleClass().add("label");
		grid.add(passLabel, 0, 2);
		PasswordField passField = new PasswordField();
		passField.getStyleClass().add("password-field");
		grid.add(passField, 1, 2);
		
		//login button
		Button loginButton = new Button("Login");
		loginButton.getStyleClass().add("button");
		grid.add(loginButton, 1, 3);
		
		//One-time code account creation
		Label otcLabel = new Label("One-time code:");
		otcLabel.getStyleClass().add("label");
		grid.add(otcLabel, 0, 4);
		TextField otcField = new TextField();
		otcField.getStyleClass().add("text-field");
		grid.add(otcField, 1, 4);
		
		//create account button
		Button createAccount = new Button("Create Account");
		createAccount.getStyleClass().add("button");
		grid.add(createAccount, 1, 5);
		
		/*
		 * EVENT HANDLING
		 */
		
		//handle login button
		loginButton.setOnAction(event ->
		{
			ChooseRolePage chooseRolePage = new ChooseRolePage();
			Stage chooseRoleStage = new Stage();
			chooseRolePage.start(chooseRoleStage);
			stage.close();
		});
		
		/*
		 * SCENE/STAGE
		 */
		
		//scene creation
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