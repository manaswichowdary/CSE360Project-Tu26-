package src;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminPage extends Application {
    private DatabaseHelper dbHelper;
    private ArticleDatabaseHelper articleDbHelper;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Support360 Admin");
        dbHelper = new DatabaseHelper();
        articleDbHelper = new ArticleDatabaseHelper();

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #A15757;");

        Tab userManagementTab = new Tab("User Management");
        userManagementTab.setClosable(false);
        GridPane userGrid = new GridPane();
        userGrid.setAlignment(Pos.CENTER);
        userGrid.getStyleClass().add("grid-pane");
        userGrid.setStyle("-fx-background-color: #A15757;");
        setupUserManagement(userGrid, stage);
        userManagementTab.setContent(new ScrollPane(userGrid));

        Tab articleTab = new Tab("Article Management");
        articleTab.setClosable(false);
        AdminArticlesPage articlesPage = new AdminArticlesPage();
        VBox articleContent = new VBox();
        articleContent.setStyle("-fx-background-color: #A15757;");
        articlesPage.setupArticleManagement(articleContent);
        articleTab.setContent(new ScrollPane(articleContent));

        Tab groupsTab = new Tab("Access Management");
        groupsTab.setClosable(false);
        VBox groupsLayout = setupGroupManagement();
        groupsLayout.setStyle("-fx-background-color: #A15757;");
        groupsTab.setContent(new ScrollPane(groupsLayout));

        Tab messagesTab = new Tab("Student Messages");
        messagesTab.setClosable(false);
        StudentMessagesPanel messagesPanel = new StudentMessagesPanel();
        messagesPanel.setStyle("-fx-background-color: #A15757;");
        messagesTab.setContent(messagesPanel);

        tabPane.getTabs().addAll(userManagementTab, articleTab, groupsTab, messagesTab);


        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        Button backButton = new Button("Back to Role Selection");
        backButton.getStyleClass().add("button");
        backButton.setStyle("-fx-background-color: #FFB300;");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("button");
        logoutButton.setStyle("-fx-background-color: #FFB300;");

        buttonBox.getChildren().addAll(backButton, logoutButton);

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));
        mainLayout.setStyle("-fx-background-color: #A15757;");
        mainLayout.getChildren().addAll(tabPane, buttonBox);

        backButton.setOnAction(event -> {
            ChooseRolePage chooseRole = new ChooseRolePage();
            Stage chooseRoleStage = new Stage();
            chooseRole.start(chooseRoleStage);
            stage.close();
        });

        logoutButton.setOnAction(event -> {
            LoginPage loginPage = new LoginPage();
            Stage loginStage = new Stage();
            loginPage.start(loginStage);
            LoginPage.loggedInUsername = null;
            stage.close();
        });

        Scene scene = new Scene(mainLayout, 800, 700);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void setupUserManagement(GridPane grid, Stage parentStage) {
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        Label header = new Label("Welcome, " + LoginPage.loggedInUsername + "!");
        header.getStyleClass().add("label");
        header.setStyle("-fx-text-fill: white;");
        grid.add(header, 0, 0, 2, 1);

        TextField userInput = new TextField();
        userInput.setPromptText("Enter username");
        userInput.getStyleClass().add("text-field");
        grid.add(userInput, 0, 1, 2, 1);

        HBox rolesBox = new HBox(10);
        CheckBox studentCheck = new CheckBox("Student");
        CheckBox instructorCheck = new CheckBox("Instructor");
        CheckBox adminCheck = new CheckBox("Admin");

        Arrays.asList(studentCheck, instructorCheck, adminCheck).forEach(cb -> {
            cb.setStyle("-fx-text-fill: white;");
        });

        rolesBox.getChildren().addAll(studentCheck, instructorCheck, adminCheck);
        grid.add(rolesBox, 0, 2, 2, 1);

        Button generateOtpButton = new Button("Generate OTP");
        generateOtpButton.getStyleClass().add("button");
        generateOtpButton.setStyle("-fx-background-color: #FFB300;");
        grid.add(generateOtpButton, 0, 3);

        Label otpLabel = new Label("Generated OTP: ");
        otpLabel.setStyle("-fx-text-fill: white;");
        grid.add(otpLabel, 0, 4);
        
        Label otpDisplay = new Label();
        otpDisplay.setStyle("-fx-text-fill: white;");
        grid.add(otpDisplay, 1, 4);

        Button deleteUserButton = new Button("Delete User");
        deleteUserButton.getStyleClass().add("button");
        deleteUserButton.setStyle("-fx-background-color: #FFB300;");
        grid.add(deleteUserButton, 0, 5);

        generateOtpButton.setOnAction(event -> {
            try {
                dbHelper.connectToDatabase();
                List<String> selectedRoles = new ArrayList<>();
                if (studentCheck.isSelected()) selectedRoles.add("Student");
                if (instructorCheck.isSelected()) selectedRoles.add("Instructor");
                if (adminCheck.isSelected()) selectedRoles.add("Admin");
                
                if (selectedRoles.isEmpty()) {
                    showError("Input Error", "select role");
                    return;
                }
                
                String roles = String.join(",", selectedRoles);
                String otp = dbHelper.generateOTP();
                dbHelper.createInvitation(otp, roles);
                
                otpDisplay.setText(otp);
                showInfo("Success", "OTP generated with roles: " + roles);
                
            } catch (SQLException e) {
                showError("OTP Error", "Failed to generate OTP: " + e.getMessage());
            } finally {
                dbHelper.closeConnection();
            }
        });

        deleteUserButton.setOnAction(event -> {
            try {
                String username = userInput.getText().trim();
                if (!username.isEmpty()) {
                    dbHelper.connectToDatabase();
                    int id = dbHelper.getUserIdByUsername(username);
                    dbHelper.deleteUser(id);
                    userInput.clear();
                    showInfo("Success", "user deleted");
                }
            } catch (SQLException e) {
                showError("Delete Error", "delete failed: " + e.getMessage());
            } finally {
                dbHelper.closeConnection();
            }
        });
    }

    //return vbox
    private VBox setupGroupManagement() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #A15757;");

        Label header = new Label("Access Management");
        header.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        TitledPane regularGroupsPane = new TitledPane();
        regularGroupsPane.setText("Regular Article Groups");
        regularGroupsPane.setStyle("-fx-text-fill: white;");
        
        VBox regularGroupsContent = new VBox(10);
        regularGroupsContent.setStyle("-fx-background-color: #A15757;");
        
        ListView<String> regularGroupsList = new ListView<>();
        Button viewRegularGroupsButton = new Button("View Regular Groups");
        Button manageRegularAccessButton = new Button("Manage Access");
        
        styleButtons(viewRegularGroupsButton, manageRegularAccessButton);
        
        regularGroupsContent.getChildren().addAll(
            regularGroupsList,
            new HBox(10, viewRegularGroupsButton, manageRegularAccessButton)
        );
        regularGroupsPane.setContent(regularGroupsContent);

        //for special groups
        TitledPane specialGroupsPane = new TitledPane();
        specialGroupsPane.setText("Special Access Groups");
        specialGroupsPane.setStyle("-fx-text-fill: white;");
        
        VBox specialGroupsContent = new VBox(10);
        specialGroupsContent.setStyle("-fx-background-color: #A15757;");
        
        ListView<String> specialGroupsList = new ListView<>();
        Button viewSpecialGroupsButton = new Button("View Special Groups");
        Button manageSpecialAccessButton = new Button("Manage access (special)");
        
        styleButtons(viewSpecialGroupsButton, manageSpecialAccessButton);
        
        specialGroupsContent.getChildren().addAll(
            specialGroupsList,
            new HBox(10, viewSpecialGroupsButton, manageSpecialAccessButton)
        );
        specialGroupsPane.setContent(specialGroupsContent);

        layout.getChildren().addAll(header, regularGroupsPane, specialGroupsPane);
        return layout;
    }

    private void styleButtons(Button... buttons) {
        for (Button button : buttons) {
            button.getStyleClass().add("button");
            button.setStyle("-fx-background-color: #FFB300;");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}