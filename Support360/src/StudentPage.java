package src;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.layout.GridPane;
import javax.crypto.SecretKey;
import java.util.List;

public class StudentPage extends Application {
    private ArticleDatabaseHelper articleDbHelper = new ArticleDatabaseHelper();
    private SecretKey secretKey;

    public StudentPage() {
        try {
            this.secretKey = ArticleEncryptionUtils.getAESKeyFromPassphrase("group26key");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Support360 Student");

        // vbox layout
        VBox mainLayout = new VBox(15);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));

        // header
        Label header = new Label("Welcome, " + LoginPage.loggedInUsername + "!");
        header.getStyleClass().add("label");

        // searching
        Label searchLabel = new Label("Search for help articles:");
        searchLabel.getStyleClass().add("label");
        
        TextField searchField = new TextField();
        searchField.setPromptText("...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("text-field");
        
        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("button");

        //results
        ListView<String> articleListView = new ListView<>();
        articleListView.setPrefHeight(200);
        articleListView.setPrefWidth(400);

        //profile button (not yet implemented)
        Button profileButton = new Button("Profile");
        profileButton.getStyleClass().add("button");
        
        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("button");

        //add to vbox
        mainLayout.getChildren().addAll(
            header,
            searchLabel,
            searchField,
            searchButton,
            articleListView,
            profileButton,
            logoutButton
        );

        //EVENT HANDLING
        searchButton.setOnAction(event -> {
            try {
                articleDbHelper.connectToDatabase();
                String searchTerm = searchField.getText();
                
                List<String> articles = articleDbHelper.searchArticles(searchTerm);
                articleListView.getItems().clear();
                articleListView.getItems().addAll(articles);
                
                System.out.println("found " + articles.size() + " matching articles in db");
            } catch (Exception e) {
                System.err.println("searching articles erorr: " + e.getMessage());
                e.printStackTrace();
            } finally {
                articleDbHelper.closeConnection();
            }
        });

        //article selection from results
        articleListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) 
            { // double clicking
            	//double clicking will pop up article content for students
                String selectedTitle = articleListView.getSelectionModel().getSelectedItem();
                if (selectedTitle != null) 
                {
                    try {
                        articleDbHelper.connectToDatabase();
                        String encryptedBody = articleDbHelper.getArticleBody(selectedTitle);
                        
                        if (encryptedBody != null) 
                        {
                            //decrypts body
                            String decryptedBody = ArticleEncryptionUtils.decrypt(encryptedBody, secretKey);
                            
                            // new window
                            Stage articleStage = new Stage();
                            
                            articleStage.setTitle("Article: " + selectedTitle);
                            
                            //formatting
                            Text titleText = new Text(selectedTitle);
                            titleText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                            
                            TextArea contentArea = new TextArea(decryptedBody);
                            contentArea.setWrapText(true);
                            contentArea.setEditable(false);
                            contentArea.setPrefRowCount(20);
                            contentArea.setPrefColumnCount(60);
                            
                            
                            contentArea.setStyle("-fx-font-size: 14px;");
                            
                            //vbox layout
                            VBox layout = new VBox(10);
                            layout.setPadding(new Insets(15));
                            layout.getChildren().addAll(titleText, contentArea);
                            
                            //scroll pane for extra articles
                            ScrollPane scrollPane = new ScrollPane(layout);
                            scrollPane.setFitToWidth(true);
                            
                            Scene articleScene = new Scene(scrollPane, 800, 600);
                            articleStage.setScene(articleScene);
                            articleStage.show();
                        }
                    } catch (Exception e) {
                        System.err.println("showing articles error: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        articleDbHelper.closeConnection();
                    }
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

        Scene scene = new Scene(mainLayout, 600, 600);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}