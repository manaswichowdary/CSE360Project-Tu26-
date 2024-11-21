package src;
import java.sql.*;
import javax.crypto.SecretKey;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import java.util.*;

/*
 * InstructorPage class handles Instructor user, allowing functions like - 
 * Adding students, assigning articles, sending invites to students.
 */
public class InstructorPage extends Application {
    private ArticleDatabaseHelper articleDbHelper = new ArticleDatabaseHelper();
    private SecretKey secretKey;

    public InstructorPage() {
        try {
            this.secretKey = ArticleEncryptionUtils.getAESKeyFromPassphrase("group26key");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("encryption fail");
        }
    }

    
    /** 
     * @param stage
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("Support360 Instructor");

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #A15757;");

        //phase 3 tabs for instructors
        Tab articlesTab = new Tab("Manage Articles");
        articlesTab.setClosable(false);
        GridPane articlesGrid = new GridPane();
        articlesGrid.setAlignment(Pos.CENTER);
        articlesGrid.getStyleClass().add("grid-pane");
        articlesGrid.setStyle("-fx-background-color: #A15757;");
        setupArticleManagement(articlesGrid);
        articlesTab.setContent(new ScrollPane(articlesGrid));

        Tab groupsTab = new Tab("Manage Groups");
        groupsTab.setClosable(false);
        VBox groupsLayout = setupGroupManagement();
        groupsLayout.setStyle("-fx-background-color: #A15757;");
        groupsTab.setContent(new ScrollPane(groupsLayout));

        Tab messagesTab = new Tab("Student Messages");
        messagesTab.setClosable(false);
        StudentMessagesPanel messagesPanel = new StudentMessagesPanel();
        messagesPanel.setStyle("-fx-background-color: #A15757;");
        messagesTab.setContent(messagesPanel);

        tabPane.getTabs().addAll(articlesTab, groupsTab, messagesTab);

        //bottom buttons
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

    
    /** 
     * UI for the Article management from instructor
     * @param grid
     */
    private void setupArticleManagement(GridPane grid) {
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

     
        Label header = new Label("Manage Help Articles");
        header.getStyleClass().add("label");
        header.setStyle("-fx-text-fill: white;");
        grid.add(header, 0, 0, 2, 1);

        // search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search articles...");
        searchField.getStyleClass().add("text-field");
        grid.add(searchField, 0, 1);

        ComboBox<String> levelComboBox = new ComboBox<>(FXCollections.observableArrayList(
            "all", "beginner", "intermediate", "advanced", "expert"
        ));
        levelComboBox.setValue("all");
        levelComboBox.setStyle("-fx-background-color: white;");
        grid.add(levelComboBox, 1, 1);

        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("button");
        searchButton.setStyle("-fx-background-color: #FFB300;");
        grid.add(searchButton, 2, 1);

        //list of results
        ListView<String> articleListView = new ListView<>();
        articleListView.setPrefHeight(200);
        grid.add(articleListView, 0, 2, 3, 1);

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        titleField.getStyleClass().add("text-field");
        grid.add(titleField, 0, 3, 3, 1);

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        descriptionField.getStyleClass().add("text-field");
        grid.add(descriptionField, 0, 4, 3, 1);

        TextField keywordsField = new TextField();
        keywordsField.setPromptText("Keywords (comma-separated)");
        keywordsField.getStyleClass().add("text-field");
        grid.add(keywordsField, 0, 5, 3, 1);

        TextArea bodyField = new TextArea();
        bodyField.setPromptText("Article content");
        bodyField.setWrapText(true);
        bodyField.setPrefRowCount(5);
        grid.add(bodyField, 0, 6, 3, 1);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button addArticleButton = new Button("Add Article");
        Button updateArticleButton = new Button("Update Article");
        Button deleteArticleButton = new Button("Delete Article");
        Button displayButton = new Button("Display");
        Button backupButton = new Button("Backup Articles");
        Button importButton = new Button("Import Articles");

        Arrays.asList(addArticleButton, updateArticleButton, deleteArticleButton,
                     displayButton, backupButton, importButton).forEach(button -> {
            button.getStyleClass().add("button");
            button.setStyle("-fx-background-color: #FFB300;");
        });

        buttonBox.getChildren().addAll(
            addArticleButton, updateArticleButton, deleteArticleButton,
            displayButton, backupButton, importButton
        );
        grid.add(buttonBox, 0, 7, 3, 1);

        /*
         * Instructor search for articles
         */
        searchButton.setOnAction(event -> {
            try {
                articleDbHelper.connectToDatabase();
                String searchTerm = searchField.getText();
                List<String> articles = articleDbHelper.searchArticlesSimple(searchTerm);
                articleListView.getItems().clear();
                articleListView.getItems().addAll(articles);
            } catch (Exception e) {
                showError("Search Error", e.getMessage());
            } finally {
                articleDbHelper.closeConnection();
            }
        });

        /*
         * Adding article
         */
        addArticleButton.setOnAction(event -> {
            try {
                if (titleField.getText().trim().isEmpty()) {
                    showError("Input Error", "enter title");
                    return;
                }

                articleDbHelper.connectToDatabase();
                String encryptedTitle = ArticleEncryptionUtils.encrypt(titleField.getText(), secretKey);
                String encryptedDescription = ArticleEncryptionUtils.encrypt(descriptionField.getText(), secretKey);
                String encryptedKeywords = ArticleEncryptionUtils.encrypt(keywordsField.getText(), secretKey);
                String encryptedBody = ArticleEncryptionUtils.encrypt(bodyField.getText(), secretKey);
                
                articleDbHelper.addArticle(encryptedTitle, encryptedDescription, encryptedKeywords, encryptedBody);
                showInfo("Success", "Article added successfully");
                clearInputs(titleField, descriptionField, keywordsField, bodyField);
                
                // this refreshes
                searchButton.fire();
            } catch (Exception e) {
                showError("Add Error", e.getMessage());
            } finally {
                articleDbHelper.closeConnection();
            }
        });

        /*
         * Updating an article
         */
        updateArticleButton.setOnAction(event -> {
            String selectedTitle = articleListView.getSelectionModel().getSelectedItem();
            if (selectedTitle != null) {
                try {
                    articleDbHelper.connectToDatabase();
                    String encryptedTitle = ArticleEncryptionUtils.encrypt(titleField.getText(), secretKey);
                    String encryptedDescription = ArticleEncryptionUtils.encrypt(descriptionField.getText(), secretKey);
                    String encryptedKeywords = ArticleEncryptionUtils.encrypt(keywordsField.getText(), secretKey);
                    String encryptedBody = ArticleEncryptionUtils.encrypt(bodyField.getText(), secretKey);
                    
                    articleDbHelper.updateArticle(selectedTitle, encryptedTitle, encryptedDescription, 
                        encryptedKeywords, encryptedBody);
                    
                    showInfo("Success", "article updated");
                    searchButton.fire(); // refresh
                } catch (Exception e) {
                    showError("Update Error", e.getMessage());
                } finally {
                    articleDbHelper.closeConnection();
                }
            } else {
                showError("Selection Error", "select updtate article");
            }
        });

        /*
         * Instructor has rights to delete an article
         */
        deleteArticleButton.setOnAction(event -> {
            String selectedTitle = articleListView.getSelectionModel().getSelectedItem();
            if (selectedTitle != null) {
                try {
                    articleDbHelper.connectToDatabase();
                    articleDbHelper.deleteArticle(selectedTitle);
                    showInfo("Success", "Article deleted successfully");
                    searchButton.fire(); // refresh
                } catch (SQLException e) {
                    showError("Delete Error", e.getMessage());
                } finally {
                    articleDbHelper.closeConnection();
                }
            } else {
                showError("Selection Error", "select delete article");
            }
        });

        /*
         * Display the body of an article
         */
        displayButton.setOnAction(event -> {
            String selectedTitle = articleListView.getSelectionModel().getSelectedItem();
            if (selectedTitle != null) {
                try {
                    articleDbHelper.connectToDatabase();
                    String encryptedBody = articleDbHelper.getArticleBody(selectedTitle);
                    
                    if (encryptedBody != null) {
                        String decryptedBody = ArticleEncryptionUtils.decrypt(encryptedBody, secretKey);
                        displayArticlePopup(selectedTitle, decryptedBody);
                    }
                } catch (Exception e) {
                    showError("Display Error", e.getMessage());
                } finally {
                    articleDbHelper.closeConnection();
                }
            } else {
                showError("Selection Error", "select article to display");
            }
        });

        /*
         * Create a backup file for the articles
         */
        backupButton.setOnAction(event -> {
            try {
                articleDbHelper.connectToDatabase();
                articleDbHelper.backupArticles("articles_backup.txt");
                showInfo("Success", "Articles backed up successfully");
            } catch (Exception e) {
                showError("Backup Error", e.getMessage());
            } finally {
                articleDbHelper.closeConnection();
            }
        });

        /*
         * Import a backed up file of articles
         */
        importButton.setOnAction(event -> {
            try {
                articleDbHelper.connectToDatabase();
                articleDbHelper.importArticles("articles_backup.txt");
                showInfo("Success", "imported articles");
                searchButton.fire(); // refresh
            } catch (Exception e) {
                showError("Import Error", e.getMessage());
            } finally {
                articleDbHelper.closeConnection();
            }
        });

        // listener
        articleListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    articleDbHelper.connectToDatabase();
                    String encryptedBody = articleDbHelper.getArticleBody(newVal);
                    if (encryptedBody != null) {
                        titleField.setText(newVal);
                        String decryptedBody = ArticleEncryptionUtils.decrypt(encryptedBody, secretKey);
                        bodyField.setText(decryptedBody);
                    }
                } catch (Exception e) {
                    showError("Error", "Failed to load article details: " + e.getMessage());
                } finally {
                    articleDbHelper.closeConnection();
                }
            }
        });
    }

    private VBox setupGroupManagement() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #A15757;");

        Label header = new Label("Group Management");
        header.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        
        TextField groupNameField = new TextField();
        groupNameField.setPromptText("New group name");
        groupNameField.setPrefWidth(200);
        
        Button createGroupButton = new Button("Create Group");
        createGroupButton.setStyle("-fx-background-color: #FFB300;");
        
        ListView<String> groupListView = new ListView<>();
        groupListView.setPrefHeight(200);

        /*
         * Grouping similar articles
         */
        createGroupButton.setOnAction(event -> {
            try {
                String groupName = groupNameField.getText().trim();
                if (!groupName.isEmpty()) {
                    articleDbHelper.connectToDatabase();
                    articleDbHelper.createGroup(groupName, LoginPage.loggedInUsername);
                    updateGroupList(groupListView);
                    showInfo("Success", "group created");
                    groupNameField.clear();
                }
            } catch (Exception e) {
                showError("Error", "error: " + e.getMessage());
            } finally {
                articleDbHelper.closeConnection();
            }
        });

        layout.getChildren().addAll(header, groupNameField, createGroupButton, groupListView);
        return layout;
    }

    /*
     * Updating a group's article membership
     */
    private void updateGroupList(ListView<String> listView) 
    {
        try {
            articleDbHelper.connectToDatabase();
            List<String> groups = articleDbHelper.getAvailableGroups(LoginPage.loggedInUsername);
            listView.setItems(FXCollections.observableArrayList(groups));
        } catch (Exception e) {
            showError("Error", "error: " + e.getMessage());
        } finally {
            articleDbHelper.closeConnection();
        }
    }

    private void showError(String title, String content) 
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) 
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /*
     * UI For article display
     */
    private void displayArticlePopup(String title, String content) 
    {
        Stage popupStage = new Stage();
        popupStage.setTitle("Article: " + title);
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #A15757;");
        
        Text titleText = new Text(title);
        titleText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: white;");
        
        TextArea contentArea = new TextArea(content);
        contentArea.setWrapText(true);
        contentArea.setEditable(false);
        contentArea.setPrefColumnCount(60);
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #FFB300;");
        closeButton.setOnAction(e -> popupStage.close());
        
        layout.getChildren().addAll(titleText, contentArea, closeButton);
        
        Scene popupScene = new Scene(layout, 800, 600);
        popupStage.setScene(popupScene);
        popupStage.show();
    }

    private void clearInputs(TextInputControl... inputs) {
        for (TextInputControl input : inputs) {
            input.clear();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
