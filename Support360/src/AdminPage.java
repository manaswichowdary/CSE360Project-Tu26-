package src;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class AdminPage extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Support360 Admin");
        
        DatabaseHelper dbHelper = new DatabaseHelper();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.getStyleClass().add("grid-pane");

        Label header = new Label("Welcome, " + LoginPage.loggedInUsername + "!");
        header.getStyleClass().add("label");
        grid.add(header, 0, 1);

        //replaced invite button

        ListView<String> listView = new ListView<>();
        grid.add(listView, 0, 3);

        TextField userInput = new TextField();
        userInput.getStyleClass().add("text-field");
        grid.add(userInput, 0, 4);

        Button editButton = new Button("Delete");
        editButton.getStyleClass().add("button");
        grid.add(editButton, 0, 5);

        // Create checkboxes for roles
        CheckBox studentCheck = new CheckBox("Student");
        CheckBox instructorCheck = new CheckBox("Instructor");
        CheckBox adminCheck = new CheckBox("Admin");

        // horizontal role select box
        HBox rolesBox = new HBox(10); // 10 is the spacing between elements
        rolesBox.getChildren().addAll(studentCheck, instructorCheck, adminCheck);
        grid.add(rolesBox, 1, 5);

        Button changeOtp = new Button("Generate OTP");
        changeOtp.getStyleClass().add("button");
        grid.add(changeOtp, 0, 6);

        Button changeRole = new Button("Change Roles");
        changeRole.getStyleClass().add("button");
        grid.add(changeRole, 2, 5);

        Button articlesButton = new Button("Manage Articles");
        articlesButton.getStyleClass().add("button");
        grid.add(articlesButton, 0, 7);

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("button");
        grid.add(logoutButton, 0, 8);

        Label resetLabel = new Label("OTP: ");
        resetLabel.getStyleClass().add("text-field");
        grid.add(resetLabel, 0, 9);
        
        Label resetHolder = new Label();
        resetHolder.getStyleClass().add("text-field");
        grid.add(resetHolder, 1, 9);

        changeOtp.setOnAction(event -> {
            try {
                dbHelper.connectToDatabase();
                
                // build roles based off checkboxes
                List<String> selectedRoles = new ArrayList<>();
                if (studentCheck.isSelected()) selectedRoles.add("Student");
                if (instructorCheck.isSelected()) selectedRoles.add("Instructor");
                if (adminCheck.isSelected()) selectedRoles.add("Admin");
                
                if (selectedRoles.isEmpty()) {
                    resetHolder.setText("Please select at least one role");
                    return;
                }
                
                // join w/ commas
                String roles = String.join(",", selectedRoles);
                
                // store the otp
                String otp = dbHelper.generateOTP();
                dbHelper.createInvitation(otp, roles);
                
                resetHolder.setText(otp);
                System.out.println("Generated OTP with roles: " + roles);
                
            } catch (SQLException e) {
                System.err.println("Error generating invitation code: " + e.getMessage());
                e.printStackTrace();
                resetHolder.setText("Error generating code");
            } finally {
                dbHelper.closeConnection();
            }
        });


        logoutButton.setOnAction(event -> {
            LoginPage loginPage = new LoginPage();
            Stage loginStage = new Stage();
            loginPage.start(loginStage);
            LoginPage.loggedInUsername = null;
            stage.close();
        });

        editButton.setOnAction(event -> {
            try {
                dbHelper.connectToDatabase();
                int id = dbHelper.getUserIdByUsername(userInput.getText());
                dbHelper.deleteUser(id);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                dbHelper.closeConnection();
            }
            userInput.setText("");
        });

        articlesButton.setOnAction(event -> {
            AdminArticlesPage articlesPage = new AdminArticlesPage();
            Stage articlesStage = new Stage();
            articlesPage.start(articlesStage);
            stage.close();
        });

        Scene scene = new Scene(grid, 800, 500);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}