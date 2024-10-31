package src;

import java.sql.SQLException;
import java.util.List;
import javax.crypto.SecretKey;
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
 * UI for admin articles management
 */
public class AdminArticlesPage extends Application 
{

    private ArticleDatabaseHelper articleDbHelper = new ArticleDatabaseHelper();
    private SecretKey secretKey;

    public AdminArticlesPage() 
    {
        try {
            this.secretKey = ArticleEncryptionUtils.getAESKeyFromPassphrase("group26key");
        } catch (Exception e) {
            e.printStackTrace();
            
            System.out.println("AdminArticlesPage fail");
        }
    }

    @Override
    public void start(Stage stage) 
    {
        stage.setTitle("Support360 Admin - Manage Articles");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.getStyleClass().add("grid-pane");

        Label header = new Label("Manage Help Articles");
        header.getStyleClass().add("label");
        grid.add(header, 0, 0);

        TextField searchField = new TextField();
        searchField.setPromptText("Search articles...");
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
        keywordsField.setPromptText("Keywords (comma separated)");
        keywordsField.getStyleClass().add("text-field");
        grid.add(keywordsField, 0, 5);

        TextField bodyField = new TextField();
        bodyField.setPromptText("Body");
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

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");
        grid.add(backButton, 0, 9);

        searchButton.setOnAction(event -> 
        {
            try 
            {
                articleDbHelper.connectToDatabase();
                String encryptedSearch = ArticleEncryptionUtils.encrypt(searchField.getText(), secretKey);
                List<String> articles = articleDbHelper.searchArticles(encryptedSearch);
                
                List<String> decryptedArticles = ArticleEncryptionUtils.decryptArticles(articles, secretKey);
                articleListView.getItems().setAll(decryptedArticles);
            } catch (Exception e) 
            {
                e.printStackTrace();
            } finally {
                articleDbHelper.closeConnection();
            }
        });

        addArticleButton.setOnAction(event -> 
        {
            try 
            {
            	
                articleDbHelper.connectToDatabase();
                String encryptedTitle = ArticleEncryptionUtils.encrypt(titleField.getText(), secretKey);
                String encryptedDescription = ArticleEncryptionUtils.encrypt(descriptionField.getText(), secretKey);
                String encryptedKeywords = ArticleEncryptionUtils.encrypt(keywordsField.getText(), secretKey);
                String encryptedBody = ArticleEncryptionUtils.encrypt(bodyField.getText(), secretKey);
                articleDbHelper.addArticle(encryptedTitle, encryptedDescription, encryptedKeywords, encryptedBody);
            } catch (Exception e) 
            {
                e.printStackTrace();
            } finally 
            {
                System.out.println("added article: " + titleField.getText());
                articleDbHelper.closeConnection();
            }
        });

        updateArticleButton.setOnAction(event -> 
        {
            String selectedArticle = articleListView.getSelectionModel().getSelectedItem();
            if (selectedArticle != null) 
            {
                try 
                {
                    articleDbHelper.connectToDatabase();
                    String encryptedTitle = ArticleEncryptionUtils.encrypt(titleField.getText(), secretKey);
                    String encryptedDescription = ArticleEncryptionUtils.encrypt(descriptionField.getText(), secretKey);
                    String encryptedKeywords = ArticleEncryptionUtils.encrypt(keywordsField.getText(), secretKey);
                    String encryptedBody = ArticleEncryptionUtils.encrypt(bodyField.getText(), secretKey);
                    articleDbHelper.updateArticle(selectedArticle, encryptedTitle, encryptedDescription, encryptedKeywords, encryptedBody);
                } catch (Exception e) 
                {
                    e.printStackTrace();
                } finally 
                {
                	System.out.println("article updated");
                    articleDbHelper.closeConnection();
                }
            }
        });

        deleteArticleButton.setOnAction(event -> 
        {
            String selectedArticle = articleListView.getSelectionModel().getSelectedItem();
            if (selectedArticle != null) 
            {
                try 
                {
                    articleDbHelper.connectToDatabase();
                    articleDbHelper.deleteArticle(selectedArticle);
                } catch (SQLException e) 
                {
                    e.printStackTrace();
                } finally 
                {
                	System.out.println("article deleted");
                    articleDbHelper.closeConnection();
                }
            }
        });

        backupButton.setOnAction(event -> 
        {
            try 
            {
                articleDbHelper.connectToDatabase();
                articleDbHelper.backupArticles();
            } catch (SQLException e) 
            {
                e.printStackTrace();
            } finally 
            {
            	System.out.println("articles backed up");
                articleDbHelper.closeConnection();
            }
        });

        backButton.setOnAction(event -> 
        {
            AdminPage adminPage = new AdminPage();
            adminPage.start(new Stage());
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
