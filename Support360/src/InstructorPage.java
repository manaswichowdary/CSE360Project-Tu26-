package src;
import java.sql.*;
import java.sql.SQLException;
import java.util.List;
import javax.crypto.SecretKey;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Instructor page
 * 
 * allows instructor to manage the article db,
 * pretty much the same as AdminArticlesPage
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

    @Override
    public void start(Stage stage) {
        stage.setTitle("Support360 Instructor - Manage Articles");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.getStyleClass().add("grid-pane");

        Label header = new Label("Manage Help Articles");
        header.getStyleClass().add("label");
        grid.add(header, 0, 0);

        TextField searchField = new TextField();
        searchField.setPromptText("search...");
        searchField.getStyleClass().add("text-field");
        grid.add(searchField, 0, 1);

        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("button");
        grid.add(searchButton, 1, 1);

        ListView<String> articleListView = new ListView<>();
        grid.add(articleListView, 0, 2, 2, 1);

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        titleField.getStyleClass().add("text-field");
        grid.add(titleField, 0, 3);

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        descriptionField.getStyleClass().add("text-field");
        grid.add(descriptionField, 0, 4);

        TextField keywordsField = new TextField();
        keywordsField.setPromptText("keywords");
        keywordsField.getStyleClass().add("text-field");
        grid.add(keywordsField, 0, 5);

        TextField bodyField = new TextField();
        bodyField.setPromptText("article content");
        bodyField.getStyleClass().add("text-field");
        grid.add(bodyField, 0, 6);

        Button addArticleButton = new Button("Add Article");
        addArticleButton.getStyleClass().add("button");
        grid.add(addArticleButton, 0, 7);

        Button updateArticleButton = new Button("Update Article");
        updateArticleButton.getStyleClass().add("button");
        grid.add(updateArticleButton, 1, 7);

        Button deleteArticleButton = new Button("Delete Article");
        deleteArticleButton.getStyleClass().add("button");
        grid.add(deleteArticleButton, 0, 8);

        Button backupButton = new Button("Backup Articles");
        backupButton.getStyleClass().add("button");
        grid.add(backupButton, 1, 8);
        
        Button importButton = new Button("Import Articles");
        importButton.getStyleClass().add("button");
        grid.add(importButton, 2, 8);
        
        Button displayButton = new Button("Display");
        displayButton.getStyleClass().add("button");
        grid.add(displayButton, 0, 9);

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("button");
        grid.add(logoutButton, 0, 10);

        searchButton.setOnAction(event -> {
            try {
                articleDbHelper.connectToDatabase();
                String encryptedSearch = ArticleEncryptionUtils.encrypt(searchField.getText(), secretKey);
                List<String> articles = articleDbHelper.searchArticles(encryptedSearch);
                
                List<String> decryptedArticles = ArticleEncryptionUtils.decryptArticles(articles, secretKey);
                articleListView.getItems().setAll(decryptedArticles);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                articleDbHelper.closeConnection();
            }
        });

        addArticleButton.setOnAction(event -> {
            try {
                articleDbHelper.connectToDatabase();
                String encryptedTitle = ArticleEncryptionUtils.encrypt(titleField.getText(), secretKey);
                String encryptedDescription = ArticleEncryptionUtils.encrypt(descriptionField.getText(), secretKey);
                String encryptedKeywords = ArticleEncryptionUtils.encrypt(keywordsField.getText(), secretKey);
                String encryptedBody = ArticleEncryptionUtils.encrypt(bodyField.getText(), secretKey);
                articleDbHelper.addArticle(encryptedTitle, encryptedDescription, encryptedKeywords, encryptedBody);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("added article: " + titleField.getText());
                articleDbHelper.closeConnection();
            }
        });

        updateArticleButton.setOnAction(event -> {
            String selectedArticle = articleListView.getSelectionModel().getSelectedItem();
            if (selectedArticle != null) {
                try {
                    articleDbHelper.connectToDatabase();
                    String encryptedTitle = ArticleEncryptionUtils.encrypt(titleField.getText(), secretKey);
                    String encryptedDescription = ArticleEncryptionUtils.encrypt(descriptionField.getText(), secretKey);
                    String encryptedKeywords = ArticleEncryptionUtils.encrypt(keywordsField.getText(), secretKey);
                    String encryptedBody = ArticleEncryptionUtils.encrypt(bodyField.getText(), secretKey);
                    articleDbHelper.updateArticle(selectedArticle, encryptedTitle, encryptedDescription, encryptedKeywords, encryptedBody);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("article updated");
                    articleDbHelper.closeConnection();
                }
            }
        });

        deleteArticleButton.setOnAction(event -> {
            String selectedArticle = articleListView.getSelectionModel().getSelectedItem();
            if (selectedArticle != null) {
                try {
                    articleDbHelper.connectToDatabase();
                    articleDbHelper.deleteArticle(selectedArticle);
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("article deleted");
                    articleDbHelper.closeConnection();
                }
            }
        });

        backupButton.setOnAction(event -> {
            try {
                articleDbHelper.connectToDatabase();
                articleDbHelper.backupArticles("test.txt");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("articles backed up");
                articleDbHelper.closeConnection();
            }
        });
        
        importButton.setOnAction(event -> {
            try {
                articleDbHelper.connectToDatabase();
                articleDbHelper.importArticles("test.txt"); //?
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                articleDbHelper.closeConnection();
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
                        
                        Stage articleStage = new Stage();
                        articleStage.setTitle("Article: " + selectedTitle);
                        
                        Text titleText = new Text(selectedTitle);
                        titleText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                        
                        TextArea contentArea = new TextArea(decryptedBody);
                        contentArea.setWrapText(true);
                        contentArea.setEditable(false);
                        contentArea.setPrefRowCount(20);
                        contentArea.setPrefColumnCount(60);
                        contentArea.setStyle("-fx-font-size: 14px;");
                        
                        VBox layout = new VBox(10);
                        layout.setPadding(new Insets(15));
                        layout.getChildren().addAll(titleText, contentArea);
                        
                        ScrollPane scrollPane = new ScrollPane(layout);
                        scrollPane.setFitToWidth(true);
                        
                        Scene articleScene = new Scene(scrollPane, 800, 600);
                        articleStage.setScene(articleScene);
                        articleStage.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    articleDbHelper.closeConnection();
                }
            }
        });

        logoutButton.setOnAction(event -> {
            LoginPage loginPage = new LoginPage();
            Stage loginStage = new Stage();
            loginPage.start(loginStage);
            LoginPage.loggedInUsername = null;
            stage.close();
        });

        Scene scene = new Scene(grid, 600, 600);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}