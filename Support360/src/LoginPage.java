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

public class LoginPage extends Application {

    // Static variable to store the username of the logged-in user
    public static String loggedInUsername;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Support360 Login Page");

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
        Label passLabel = new Label("Password:");
        passLabel.getStyleClass().add("label");
        grid.add(passLabel, 0, 2);
        PasswordField passField = new PasswordField();
        passField.getStyleClass().add("password-field");
        grid.add(passField, 1, 2);

        // One-time code account creation
        Label otcLabel = new Label("One-time code:");
        otcLabel.getStyleClass().add("label");
        grid.add(otcLabel, 0, 4);
        TextField otcField = new TextField();
        otcField.getStyleClass().add("text-field");
        grid.add(otcField, 1, 4);

        // Login button
        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("button");
        grid.add(loginButton, 1, 5);

        // Create account button
        Button createAccount = new Button("Create Account");
        createAccount.getStyleClass().add("button");
        grid.add(createAccount, 1, 6);

        /*
         * EVENT HANDLING
         */

        // Handle login button
        loginButton.setOnAction(event -> {
            DatabaseHelper dbHelper = new DatabaseHelper();

            try {
                dbHelper.connectToDatabase();

                // Get the user’s role based on their username and password
                String role = dbHelper.getRole(userField.getText(), passField.getText());
                // statement to see contents of the table
                System.out.println(dbHelper.runSQLQuery("SELECT * FROM cse360users"));

                if (role != null) {
                    if (role.equals("Admin")) {
                        // Admin login - skip OTP validation and proceed to finish account setup
                        loggedInUsername = userField.getText();
                        // finish account setup for admin only if not done yet
                        String checkLastName = dbHelper.runSQLQuery("SELECT last_name FROM cse360users WHERE role = 'Admin'");
//                        System.out.println("Last Name: " + checkLastName);  // Debugging line

                        // Handle cases where last_name is NULL, the string "null", or an empty string
                        if (checkLastName == "null" || checkLastName.equalsIgnoreCase("null") || checkLastName.trim().isEmpty()) {
//                            System.out.println("Hi 1");
                            FinishCreation finishAccountSetup = new FinishCreation();
                            Stage finishAccountStage = new Stage();
                            finishAccountSetup.start(finishAccountStage);
                            stage.close();
//                            System.out.println("Hi 2");
                        }

                        
                    } else {
                        // Non-admin (e.g., Student/Instructor) - Get the user ID and validate OTP
                        int userId = dbHelper.getUserIdByUsername(userField.getText());
                        if (dbHelper.validateOTP(userId, otcField.getText())) {
                            loggedInUsername = userField.getText();
                            ChooseRolePage chooseRolePage = new ChooseRolePage();
                            Stage chooseRoleStage = new Stage();
                            chooseRolePage.start(chooseRoleStage);
                            stage.close();
                        } else {
                            System.out.println("Invalid OTP.");
                        }
                    }
                } else {
                    System.out.println("Invalid login credentials.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                dbHelper.closeConnection();
            }
        });

        // Handle create account button
        createAccount.setOnAction(event -> {
            AccountCreation accountCreation = new AccountCreation();
            Stage accountCreationStage = new Stage();
            accountCreation.start(accountCreationStage);
            stage.close();
        });

        /*
         * SCENE/STAGE
         */

        // Scene creation
        Scene scene = new Scene(grid, 600, 500);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        // Show stage
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
