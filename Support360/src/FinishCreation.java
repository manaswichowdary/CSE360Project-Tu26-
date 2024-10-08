import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class FinishCreation extends Application
{
	@Override
	public void start(Stage stage)
	{
		stage.setTitle("Support360 Finish Creation");
		
		/*
		 * PAGE LAYOUT
		 */
		
		//set up page as grid
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.getStyleClass().add("grid-pane");
		
		//email
		Label emailLabel = new Label("E-Mail:");
		emailLabel.getStyleClass().add("label");
		grid.add(emailLabel, 0, 1);
		TextField emailField = new TextField();
		emailField.getStyleClass().add("text-field");
		grid.add(emailField, 1, 1);
		
		//first name
		Label fnLabel = new Label("First Name:");
		fnLabel.getStyleClass().add("label");
		grid.add(fnLabel, 0, 2);
		TextField fnField = new TextField();
		fnField.getStyleClass().add("text-field");
		grid.add(fnField, 1, 2);
				
		//middle 
		Label mnLabel = new Label("Middle Name:");
		mnLabel.getStyleClass().add("label");
		grid.add(mnLabel, 0, 3);
		TextField mnField = new TextField();
		mnField.getStyleClass().add("text-field");
		grid.add(mnField, 1, 3);
		
		//last name
		Label lnLabel = new Label("Last Name:");
		lnLabel.getStyleClass().add("label");
		grid.add(lnLabel, 0, 4);
		TextField lnField = new TextField();
		lnField.getStyleClass().add("text-field");
		grid.add(lnField, 1, 4);
		
		//preferred name
		Label pnLabel = new Label("Preferred Name:");
		pnLabel.getStyleClass().add("label");
		grid.add(pnLabel, 0, 5);
		TextField pnField = new TextField();
		pnField.getStyleClass().add("text-field");
		grid.add(pnField, 1, 5);
		
		//warning label
		Label emptyWarn = new Label("Warning: All inputs required.");
		emptyWarn.getStyleClass().add("label");
		grid.add(emptyWarn, 0, 6);
		emptyWarn.setVisible(false);
		
		//create account button
		Button ctAct = new Button("Create Account");
		ctAct.getStyleClass().add("button");
		grid.add(ctAct, 1, 7);
		
		/*
		 * EVENT HANDLING
		 */
		
		ctAct.setOnAction(event ->
		{
			if (emailField.getText().equals("") || fnField.getText().equals("") || mnField.getText().equals("") || lnField.getText().equals("")) {
				emptyWarn.setVisible(true);
			} else {
				//save all data, login, move to choose role page
				ChooseRolePage chooseRolePage = new ChooseRolePage();
				Stage chooseRoleStage = new Stage();
				chooseRolePage.start(chooseRoleStage);
				stage.close();
			}
		});
		//create attempt
			//check if any required fields are blank 
		
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