package src;
import java.sql.*;
import java.sql.SQLException;
import java.util.List;
import javax.crypto.SecretKey;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * UI for admin articles management
 */
public class AdminArticlesPage extends Application 
{

    private ArticleDatabaseHelper articleDbHelper = new ArticleDatabaseHelper();
    private SecretKey secretKey;

    public AdminArticlesPage() 
    {
        try 
        {
            this.secretKey = ArticleEncryptionUtils.getAESKeyFromPassphrase("group26key");
        } catch (Exception e) 
        {
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

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");
        grid.add(backButton, 0, 9);
        
        Button displayButton = new Button("Display");
        displayButton.getStyleClass().add("button");
        grid.add(displayButton, 0, 10);

        searchButton.setOnAction(event -> {
            try {
                articleDbHelper.connectToDatabase();
                String searchTerm = searchField.getText();
                System.out.println("Searching for: " + searchTerm);
                
                List<String> articles = articleDbHelper.searchArticles(searchTerm);
                
                //update article results w search results
                articleListView.getItems().clear();
                articleListView.getItems().addAll(articles);
            } catch (Exception e) {
                System.err.println("searching error: " + e.getMessage());
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

        updateArticleButton.setOnAction(event -> {
            String selectedTitle = articleListView.getSelectionModel().getSelectedItem();
            if (selectedTitle != null) 
            {
            	
                try {
                    articleDbHelper.connectToDatabase();
                    
                    //grabs text fields
                    String newTitle = titleField.getText().trim();
                    String newDescription = descriptionField.getText().trim();
                    String newKeywords = keywordsField.getText().trim();
                    String newBody = bodyField.getText().trim();
                    
                    //update artice with the added content
                    articleDbHelper.updateArticle(selectedTitle, newTitle, newDescription, newKeywords, newBody);
                    
                    // if title has content, update with that content
                    if (!newTitle.isEmpty()) 
                    {
                        int selectedIndex = articleListView.getSelectionModel().getSelectedIndex();
                        articleListView.getItems().set(selectedIndex, newTitle);
                    }
                    
                    // clear fields after
                    titleField.clear();
                    
                    descriptionField.clear();
                    keywordsField.clear();
                    bodyField.clear();
                    
                    System.out.println("article updated");
                    
                    
                    // refresh results to show update
                    String searchTerm = searchField.getText();
                    
                    List<String> articles = articleDbHelper.searchArticles(searchTerm);
                    articleListView.getItems().clear();
                    articleListView.getItems().addAll(articles);
                    
                } catch (SQLException e) 
                
                {
                    System.err.println("error updating: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    articleDbHelper.closeConnection();
                }
            } else {
                System.out.println("article not slected");
            }
        });

        deleteArticleButton.setOnAction(event -> {
            String selectedTitle = articleListView.getSelectionModel().getSelectedItem();
            if (selectedTitle != null) 
            {
                try {
                    articleDbHelper.connectToDatabase();
                    articleDbHelper.deleteArticle(selectedTitle);
                    
                    // remove article from results
                    articleListView.getItems().remove(selectedTitle);
                    
                    // clear section
                    articleListView.getSelectionModel().clearSelection();
                    
                    System.out.println("article deleted: " + selectedTitle);
                } catch (SQLException e) {
                    System.err.println("Error deleting: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    articleDbHelper.closeConnection();
                }
            } else {
                System.out.println("No article selected for deletion");
            }
        });

        backupButton.setOnAction(event -> 
        {
            try 
            {
                articleDbHelper.connectToDatabase();
                articleDbHelper.backupArticles("test.txt");
            } catch (SQLException e) 
            {
                e.printStackTrace();
            } catch (Exception e) {
				e.printStackTrace();
			} finally 
            {
            	System.out.println("articles backed up");
                articleDbHelper.closeConnection();
            }
        });
        
        importButton.setOnAction(event -> 
        {
            try 
            {
                articleDbHelper.connectToDatabase();
                articleDbHelper.importArticles("test.txt");
            } catch (SQLException e) 
            {
                e.printStackTrace();
            } catch (Exception e) 
            {
            	
				e.printStackTrace();
			} finally 
            {
            	System.out.println("articles imported");
                articleDbHelper.closeConnection();
            }
        });
        
        
        
        displayButton.setOnAction(event -> {
            String selectedTitle = articleListView.getSelectionModel().getSelectedItem();
            System.out.println("title (decrypted): " + selectedTitle);
            
            if (selectedTitle != null) 
            {
                try {
                    articleDbHelper.connectToDatabase();
                    String encryptedBody = articleDbHelper.getArticleBody(selectedTitle);
                    
                    if (encryptedBody != null) 
                    {
                        try {
                            // decrypt body
                            String decryptedBody = ArticleEncryptionUtils.decrypt(encryptedBody, secretKey);
                            System.out.println("article body decrypted!");
                            
                            // new window for body txt
                            Stage articleStage = new Stage();
                            articleStage.setTitle("Article: " + selectedTitle);
                            
                            // content formatting
                            Text titleText = new Text(selectedTitle);
                            titleText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"); 
                            TextArea contentArea = new TextArea(decryptedBody);
                            contentArea.setWrapText(true);
                            
                            contentArea.setEditable(false);
                            
                            contentArea.setPrefRowCount(20);
                            contentArea.setPrefColumnCount(60);
                            contentArea.setStyle("-fx-font-size: 14px;");
                            
                            // vbox for display
                            VBox layout = new VBox(10);
                            layout.setPadding(new Insets(15));
                            layout.getChildren().addAll(titleText, contentArea);
                            
                            // scrolling for off screen content
                            ScrollPane scrollPane = new ScrollPane(layout);
                            scrollPane.setFitToWidth(true);
                            
                            // scene
                            Scene articleScene = new Scene(scrollPane, 800, 600);
                            articleStage.setScene(articleScene);
                            
                            // shows window
                            articleStage.show();
                        } catch (Exception e) {
                            System.err.println("decryption error: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("article has no content: " + selectedTitle);
                    }
                } catch (Exception e) {
                    System.err.println("error: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    articleDbHelper.closeConnection();
                }
            } else {
                System.out.println("none selected");
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
