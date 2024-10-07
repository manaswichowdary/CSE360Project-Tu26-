import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class ChooseRolePage extends Application
{
	@Override
	public void start(Stage stage)
	{
		stage.setTitle("Choose Your Role");
		
		//'choose your role' text
		Label chooseLabel = new Label("Select your role for this session...");
		chooseLabel.getStyleClass().add("label");
		
		//role buttons
		Button studentButton = new Button("Student");
		studentButton.getStyleClass().add("button");
		Button adminButton = new Button("Admin");
		adminButton.getStyleClass().add("button");
		Button instructorButton = new Button("Instructor");
		instructorButton.getStyleClass().add("button");
		
		//stack buttons
		VBox vbox = new VBox(15);
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().addAll(chooseLabel, studentButton, adminButton, instructorButton);
		
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
