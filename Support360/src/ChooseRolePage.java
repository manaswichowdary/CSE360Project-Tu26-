package src;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.sql.*;
import java.util.List;

/**
 * ChooseRolePage class for the Support360 application.
 * This class allows a user to select their role
 */
public class ChooseRolePage extends Application {

    /**
     *The main entry point for the JavaFX application.
     * This method sets up the role selection user interface
     * 
     * @param stage The primary stage for this application
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("Choose Your Role");

        //'choose your role' text
        Label chooseLabel = new Label("Select your role for this session...");
        chooseLabel.getStyleClass().add("label");

        // Role buttons
        Button studentButton = new Button("Student");
        studentButton.getStyleClass().add("button");
        Button adminButton = new Button("Admin");
        adminButton.getStyleClass().add("button");
        Button instructorButton = new Button("Instructor");
        instructorButton.getStyleClass().add("button");

        // Stack buttons
        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(chooseLabel, studentButton, adminButton, instructorButton);

        // Retrieve roles for the logged-in user
        DatabaseHelper dbHelper = new DatabaseHelper();
        String loggedInUsername = LoginPage.loggedInUsername;  // Fetch the username from LoginPage
        System.out.println("Username: " + loggedInUsername);

        try {
            dbHelper.connectToDatabase();

            // Query to get the user ID from the username
            String query = String.format("SELECT role FROM cse360users WHERE username = '%s'", LoginPage.loggedInUsername);

            String roles = dbHelper.runSQLQuery(query);

            // Now, fetch the roles for this userId
            
            System.out.println(roles);

            // Enable buttons only for the roles that the user has
            //to be implemented in PHASE 2
            if (roles.contains("Admin")) {
                adminButton.setDisable(false);
                adminButton.setOnAction(event -> {
                    AdminPage adminPage = new AdminPage();
                    Stage adminStage = new Stage();
                    adminPage.start(adminStage);
                    stage.close();
                });
            } else {
                adminButton.setDisable(true); // Disable the Admin button if the user is not an admin
            }

            if (roles.contains("Student")) {
                studentButton.setDisable(false);
                studentButton.setOnAction(event -> {
                    StudentPage studentPage = new StudentPage();
                    Stage studentStage = new Stage();
                    studentPage.start(studentStage);
                    stage.close();
                });
            } else {
                studentButton.setDisable(true); // Disable the Student button if the user is not a student
            }

            if (roles.contains("Instructor")) {
                instructorButton.setDisable(false);
                instructorButton.setOnAction(event -> {
                    InstructorPage instructorPage = new InstructorPage();
                    Stage instructorStage = new Stage();
                    instructorPage.start(instructorStage);
                    stage.close();
                });
            } else {
                instructorButton.setDisable(true); // Disable the Instructor button if the user is not an instructor
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbHelper.closeConnection();
        }
        
        /*
         * EVENT HANDLING
         */
        
        adminButton.setOnAction(event ->
		{
			AdminPage adminPage = new AdminPage();
			Stage adminStage= new Stage();
			adminPage.start(adminStage);
			stage.close();
		});

        /*
         * SCENE CREATION
         */

        // Scene creation
        Scene scene = new Scene(vbox, 600, 500);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        // Show stage
        stage.setScene(scene);
        stage.show();
    }

    /**
     *  main method for launching the JavaFX application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
