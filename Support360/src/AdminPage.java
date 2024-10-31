package src;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Random;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

/**
 * AdminPage class for the Support360 application.
 * This class represents the administrative interface
 */
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

        Button inviteButton = new Button("Invite");
        inviteButton.getStyleClass().add("button");
        grid.add(inviteButton, 0, 2);

        ListView<String> listView = new ListView<>();
        grid.add(listView, 0, 3);

        TextField userInput = new TextField();
        userInput.getStyleClass().add("text-field");
        grid.add(userInput, 0, 4);

        Button editButton = new Button("Delete");
        editButton.getStyleClass().add("button");
        grid.add(editButton, 0, 5);

        Button changeOtp = new Button("Generate OTP");
        changeOtp.getStyleClass().add("button");
        grid.add(changeOtp, 1, 5);

        Button changeRole = new Button("Change Roles");
        changeRole.getStyleClass().add("button");
        grid.add(changeRole, 2, 5);

        Button articlesButton = new Button("Manage Articles");
        articlesButton.getStyleClass().add("button");
        grid.add(articlesButton, 0, 6);

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("button");
        grid.add(logoutButton, 0, 7);

        Label resetLabel = new Label("OTP: ");
        resetLabel.getStyleClass().add("text-field");
        grid.add(resetLabel, 0, 8);
        
        Label resetHolder = new Label();
        resetHolder.getStyleClass().add("text-field");
        grid.add(resetHolder, 1, 8);

        inviteButton.setOnAction(event -> {
            OtpPage otpPage = new OtpPage();
            Stage otpStage = new Stage();
            otpPage.start(otpStage);
            stage.close();
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

        changeOtp.setOnAction(event -> {
            try {
                dbHelper.connectToDatabase();
                int id = dbHelper.getUserIdByUsername(userInput.getText());
                String tokenHolder = generateRandomString(7);
                Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
                dbHelper.createResetToken(id, tokenHolder, currentTimestamp);
                resetHolder.setText(tokenHolder);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                dbHelper.closeConnection();
            }
        });

        articlesButton.setOnAction(event -> {
            AdminArticlesPage articlesPage = new AdminArticlesPage();
            Stage articlesStage = new Stage();
            articlesPage.start(articlesStage);
            stage.close();
        });

        Scene scene = new Scene(grid, 600, 500);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int charactersLength = characters.length();
        Random random = new Random();

        char[] randomString = new char[length];
        for (int i = 0; i < length; i++) {
            randomString[i] = characters.charAt(random.nextInt(charactersLength));
        }
        return new String(randomString);
    }
}
