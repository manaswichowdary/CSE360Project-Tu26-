package src;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import java.sql.SQLException;
import java.sql.SQLException;

public class ResetPage extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Reset Password");

        // Set up page as grid
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        // Username label and entry field
        Label userLabel = new Label("Username:");
        grid.add(userLabel, 0, 1);
        TextField userField = new TextField();
        grid.add(userField, 1, 1);

        // OTP label and entry field
        Label otpLabel = new Label("OTP:");
        grid.add(otpLabel, 0, 2);
        TextField otpField = new TextField();
        grid.add(otpField, 1, 2);

        // New password label and entry field
        Label passLabel = new Label("New Password:");
        grid.add(passLabel, 0, 3);
        PasswordField passField = new PasswordField();
        grid.add(passField, 1, 3);

        // Reset Password button
        Button resetPasswordButton = new Button("Reset Password");
        grid.add(resetPasswordButton, 1, 4);

        // Event handler for reset password
        resetPasswordButton.setOnAction(event -> {
            String username = userField.getText();
            String otp = otpField.getText();
            String newPassword = passField.getText();

            try {
                DatabaseHelper dbHelper = new DatabaseHelper();
                dbHelper.connectToDatabase();

                // Get user ID based on the username
                int userId = dbHelper.getUserIdByUsername(username);
                if (userId != -1) {
                    // Validate OTP
                    boolean validOTP = dbHelper.validateOTP(userId, otp);
                    if (validOTP) {
                        // If OTP is valid, reset the password
                        dbHelper.updateUserPassword(userId, newPassword);
                        System.out.println("Password reset successfully.");
                    } else {
                        System.out.println("Invalid OTP.");
                    }
                } else {
                    System.out.println("User not found.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        // Scene creation
        Scene scene = new Scene(grid, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}