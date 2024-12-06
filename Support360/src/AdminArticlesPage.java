package src;
import java.sql.*;
import javax.crypto.SecretKey;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import java.util.*;

public class AdminArticlesPage extends Application {
    private ArticleDatabaseHelper articleDbHelper = new ArticleDatabaseHelper();
    private SecretKey secretKey;

    public AdminArticlesPage() {
        try {
            this.secretKey = ArticleEncryptionUtils.getAESKeyFromPassphrase("group26key");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("AdminArticlesPage initialization failed");
        }
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Support360 Admin - Manage Articles");

        VBox mainLayout = new VBox(10);
        mainLayout.setStyle("-fx-background-color: #A15757;");
        
        HBox topButtons = new HBox(10);
        topButtons.setAlignment(Pos.CENTER_RIGHT);
        topButtons.setPadding(new Insets(10));
        
        Button backButton = new Button("Back to Admin Page");
        backButton.setStyle("-fx-background-color: #FFB300;"); 
        
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #FFB300;"); 
        
        topButtons.getChildren().addAll(backButton, logoutButton);

        setupArticleManagement(mainLayout);
        mainLayout.getChildren().add(0, topButtons);

        backButton.setOnAction(event -> {
            AdminPage adminPage = new AdminPage();
            Stage adminStage = new Stage();
            adminPage.start(adminStage);
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

    public void setupArticleManagement(VBox container) {
        container.setSpacing(10);
        container.setPadding(new Insets(20));

        Label header = new Label("Manage Help Articles");
        header.getStyleClass().add("header-label");
        header.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);

        TextField searchField = new TextField();
        searchField.setPromptText("Search articles...");
        searchField.setPrefWidth(300);

        ComboBox<String> levelComboBox = new ComboBox<>(FXCollections.observableArrayList(
            "all", "beginner", "intermediate", "advanced", "expert"
        ));
        levelComboBox.setValue("all");
        levelComboBox.setStyle("-fx-background-color: white;");

        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #FFB300;");

        searchBox.getChildren().addAll(searchField, levelComboBox, searchButton);

        ListView<String> articleListView = new ListView<>();
        articleListView.setPrefHeight(200);
        articleListView.setStyle("-fx-control-inner-background: #ffffff;");

        VBox inputFields = new VBox(10);
        inputFields.setStyle("-fx-spacing: 10;");

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        titleField.setStyle("-fx-prompt-text-fill: #666666;");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        descriptionField.setStyle("-fx-prompt-text-fill: #666666;");

        TextField keywordsField = new TextField();
        keywordsField.setPromptText("Keywords (comma-separated)");
        keywordsField.setStyle("-fx-prompt-text-fill: #666666;");

        TextArea bodyField = new TextArea();
        bodyField.setPromptText("Article content");
        bodyField.setWrapText(true);
        bodyField.setPrefRowCount(5);
        bodyField.setStyle("-fx-prompt-text-fill: #666666;");

        inputFields.getChildren().addAll(
            new Label("Title:"), titleField,
            new Label("Description:"), descriptionField,
            new Label("Keywords:"), keywordsField,
            new Label("Content:"), bodyField
        );

        inputFields.getChildren().filtered(node -> node instanceof Label)
            .forEach(node -> node.setStyle("-fx-text-fill: white;"));

        HBox mainButtons = new HBox(10);
        mainButtons.setAlignment(Pos.CENTER);

        Button addArticleButton = new Button("Add Article");
        Button updateArticleButton = new Button("Update Article");
        Button deleteArticleButton = new Button("Delete Article");
        Button displayButton = new Button("Display");

        Arrays.asList(addArticleButton, updateArticleButton, deleteArticleButton, displayButton)
            .forEach(button -> button.setStyle("-fx-background-color: #FFB300;"));

        mainButtons.getChildren().addAll(
            addArticleButton, updateArticleButton, deleteArticleButton, displayButton
        );

        HBox utilityButtons = new HBox(10);
        utilityButtons.setAlignment(Pos.CENTER);

        Button backupButton = new Button("Backup Articles");
        Button importButton = new Button("Import Articles");

        Arrays.asList(backupButton, importButton)
            .forEach(button -> button.setStyle("-fx-background-color: #FFB300;"));

        utilityButtons.getChildren().addAll(backupButton, importButton);

        container.getChildren().addAll(
            header,
            searchBox,
            articleListView,
            inputFields,
            mainButtons,
            utilityButtons
        );

        //handlers
        searchButton.setOnAction(event -> {
            try {
                articleDbHelper.connectToDatabase();
                String searchTerm = searchField.getText();
                List<String> articles = articleDbHelper.searchArticles(searchTerm);
                articleListView.getItems().clear();
                articleListView.getItems().addAll(articles);
            } catch (Exception e) {
                showError("Search Error", e.getMessage());
            } finally {
                articleDbHelper.closeConnection();
            }
        });

        addArticleButton.setOnAction(event -> {
            try {
                if (titleField.getText().trim().isEmpty()) {
                    showError("Input Error", "Title is required");
                    return;
                }

                articleDbHelper.connectToDatabase();
                String encryptedTitle = ArticleEncryptionUtils.encrypt(titleField.getText(), secretKey);
                String encryptedDescription = ArticleEncryptionUtils.encrypt(descriptionField.getText(), secretKey);
                String encryptedKeywords = ArticleEncryptionUtils.encrypt(keywordsField.getText(), secretKey);
                String encryptedBody = ArticleEncryptionUtils.encrypt(bodyField.getText(), secretKey);
                
                articleDbHelper.addArticle(encryptedTitle, encryptedDescription, encryptedKeywords, encryptedBody);
                showInfo("Success", "Article added successfully");
                clearFields(titleField, descriptionField, keywordsField, bodyField);
                
                //refresh
                searchButton.fire();
            } catch (Exception e) {
                showError("Add Error", e.getMessage());
            } finally {
                articleDbHelper.closeConnection();
            }
        });

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
                    
                    showInfo("Success", "Article updated successfully");
                    //refresh
                    searchButton.fire();
                } catch (Exception e) {
                    showError("Update Error", e.getMessage());
                } finally {
                    articleDbHelper.closeConnection();
                }
            } else {
                showError("Selection Error", "select article");
            }
        });

        deleteArticleButton.setOnAction(event -> {
            String selectedTitle = articleListView.getSelectionModel().getSelectedItem();
            if (selectedTitle != null) {
                try {
                    articleDbHelper.connectToDatabase();
                    articleDbHelper.deleteArticle(selectedTitle);
                    showInfo("Success", "article deleted");
                    //refresh
                    searchButton.fire();
                } catch (SQLException e) {
                    showError("Delete Error", e.getMessage());
                } finally {
                    articleDbHelper.closeConnection();
                }
            } else {
                showError("Selection Error", "Please select an article to delete");
            }
        });

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
                showError("Selection Error", "select article");
            }
        });

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

        importButton.setOnAction(event -> {
            try {
                articleDbHelper.connectToDatabase();
                articleDbHelper.importArticles("articles_backup.txt");
                showInfo("Success", "Articles imported successfully");
                //refresh
                searchButton.fire();
            } catch (Exception e) {
                showError("Import Error", e.getMessage());
            } finally {
                articleDbHelper.closeConnection();
            }
        });

        //listener
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

    private void displayArticlePopup(String title, String content) {
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
        contentArea.setPrefRowCount(20);
        contentArea.setPrefColumnCount(60);
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #FFB300;");
        closeButton.setOnAction(e -> popupStage.close());
        
        layout.getChildren().addAll(titleText, contentArea, closeButton);
        
        Scene popupScene = new Scene(layout, 800, 600);
        popupStage.setScene(popupScene);
        popupStage.show();
    }

    private void clearFields(TextInputControl... fields) {
        for (TextInputControl field : fields) {
            field.clear();
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