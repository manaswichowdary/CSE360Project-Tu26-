package src;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import java.sql.*;

/**
 * AccountCreation class for the Support360 application.
 * This class handles the account creation page, allowing users to register
 */
public class AccountCreation extends Application {

    /**
     *The main entry point for the JavaFX application.
     * This method sets up the account creation user interface and handles user input validation.
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("Support360 Account Creation");

        /*
         * PAGE LAYOUT
         */

        // Set up page as grid
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.getStyleClass().add("grid-pane");

        // Username label and entry field
        Label userLabel = new Label("Username:");
        userLabel.getStyleClass().add("label");
        grid.add(userLabel, 0, 1);
        TextField userField = new TextField();
        userField.getStyleClass().add("text-field");
        grid.add(userField, 1, 1);

        // Password label and entry field
        Label passLabel1 = new Label("Password:");
        passLabel1.getStyleClass().add("label");
        grid.add(passLabel1, 0, 2);
        PasswordField passField1 = new PasswordField();
        passField1.getStyleClass().add("password-field");
        grid.add(passField1, 1, 2);

        // Confirm password label and entry field
        Label passLabel2 = new Label("Confirm Password:");
        passLabel2.getStyleClass().add("label");
        grid.add(passLabel2, 0, 3);
        PasswordField passField2 = new PasswordField();
        passField2.getStyleClass().add("password-field");
        grid.add(passField2, 1, 3);

        // Warning label for mismatching passwords
        Label passMatch = new Label("Warning: Passwords don't match.");
        passMatch.getStyleClass().add("label");
        passMatch.setVisible(false);
        grid.add(passMatch, 0, 4);

        // Empty field warning
        Label emptyWarn = new Label("Warning: All fields are required.");
        emptyWarn.getStyleClass().add("label");
        emptyWarn.setVisible(false);
        grid.add(emptyWarn, 0, 5);

        // Create account button
        Button createActButton = new Button("Create Account");
        createActButton.getStyleClass().add("button");
        grid.add(createActButton, 1, 6);

        /*
         * EVENT HANDLING
         * 
         * Set the behavior for the "Create Account" button.
         * This includes validating the input fields, checking if passwords match,
         * and storing the user credentials in the database.
         */

        createActButton.setOnAction(event -> {
            DatabaseHelper dbHelper = new DatabaseHelper();

            String username = userField.getText();
            String password1 = passField1.getText();
            String password2 = passField2.getText();

            // Check if fields are empty
            if (username.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
                emptyWarn.setVisible(true);
                passMatch.setVisible(false);
            } else if (!password1.equals(password2)) {
                // Check if passwords match
                passMatch.setVisible(true);
                emptyWarn.setVisible(false);
            } else {
                emptyWarn.setVisible(false);
                passMatch.setVisible(false);

                try {
                    dbHelper.connectToDatabase();
                    
                    //HASH PASSWORD
                    String hashedPassword = PasswordUtil.hashPassword(password1);

                    // Check if the database is empty, and if so, register the first user as Admin
                    if (dbHelper.isDatabaseEmpty()) {
                        dbHelper.registerFirstUser(username, hashedPassword);
                        System.out.println("Admin account created!");
                    } else {
                    	dbHelper.register(username, hashedPassword, LoginPage.loggedInRoles);
                    }

                    // Redirect to the login page after account creation
                    LoginPage loginPage = new LoginPage();
                    Stage loginStage = new Stage();
                    loginPage.start(loginStage);
                    stage.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    dbHelper.closeConnection();
                }
            }
        });

        /*
         * SCENE CREATION
         */

        Scene scene = new Scene(grid, 600, 500);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        // Show stage
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Main method for launching the JavaFX application
     * @param args basic arg param
     */
    public static void main(String[] args) {
        launch(args);
    }
}
