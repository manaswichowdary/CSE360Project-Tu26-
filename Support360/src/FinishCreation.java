package src;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.sql.*;

public class FinishCreation extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Support360 Finish Creation");

        /*
         * PAGE LAYOUT
         */

        // Set up page as grid
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.getStyleClass().add("grid-pane");

        // Email
        Label emailLabel = new Label("E-Mail:");
        emailLabel.getStyleClass().add("label");
        grid.add(emailLabel, 0, 1);
        TextField emailField = new TextField();
        emailField.getStyleClass().add("text-field");
        grid.add(emailField, 1, 1);

        // First name
        Label fnLabel = new Label("First Name:");
        fnLabel.getStyleClass().add("label");
        grid.add(fnLabel, 0, 2);
        TextField fnField = new TextField();
        fnField.getStyleClass().add("text-field");
        grid.add(fnField, 1, 2);

        // Middle name
        Label mnLabel = new Label("Middle Name:");
        mnLabel.getStyleClass().add("label");
        grid.add(mnLabel, 0, 3);
        TextField mnField = new TextField();
        mnField.getStyleClass().add("text-field");
        grid.add(mnField, 1, 3);

        // Last name
        Label lnLabel = new Label("Last Name:");
        lnLabel.getStyleClass().add("label");
        grid.add(lnLabel, 0, 4);
        TextField lnField = new TextField();
        lnField.getStyleClass().add("text-field");
        grid.add(lnField, 1, 4);

        // Preferred name
        Label pnLabel = new Label("Preferred Name:");
        pnLabel.getStyleClass().add("label");
        grid.add(pnLabel, 0, 5);
        TextField pnField = new TextField();
        pnField.getStyleClass().add("text-field");
        grid.add(pnField, 1, 5);

        // Warning label
        Label emptyWarn = new Label("Warning: All inputs required.");
        emptyWarn.getStyleClass().add("label");
        grid.add(emptyWarn, 0, 6);
        emptyWarn.setVisible(false);

        // Create account button
        Button ctAct = new Button("Finish Account Setup");
        ctAct.getStyleClass().add("button");
        grid.add(ctAct, 1, 7);

        /*
         * EVENT HANDLING
         */

        ctAct.setOnAction(event -> {
            // Validate that no field is left empty
            if (emailField.getText().equals("") || fnField.getText().equals("") || lnField.getText().equals("")) {
                emptyWarn.setVisible(true);
            } else {
                // Connect to the database and finish account setup
                DatabaseHelper dbHelper = new DatabaseHelper();
                String loggedInUsername = LoginPage.loggedInUsername;  // Fetch the username from LoginPage
                
                try {
                    dbHelper.connectToDatabase();

                    // Query to get the user ID from the username
                    String query = "SELECT id FROM cse360users WHERE username = ?";
                    int userId = -1;

                    try (PreparedStatement pstmt = dbHelper.getConnection().prepareStatement(query)) {
                        pstmt.setString(1, loggedInUsername);
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            userId = rs.getInt("id");  // Get the user ID based on the username
                        }
                    }

                    // Now, finish account setup by updating the user details
                    dbHelper.finishAccountSetup(
                            userId, fnField.getText(), mnField.getText(), lnField.getText(), pnField.getText()
                    );

                    // Move to the Choose Role Page after successful account setup
                    ChooseRolePage chooseRolePage = new ChooseRolePage();
                    Stage chooseRoleStage = new Stage();
                    chooseRolePage.start(chooseRoleStage);
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

    public static void main(String[] args) {
        launch(args);
    }
}
