package src;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import javax.crypto.SecretKey;
import java.util.*;

public class StudentPage extends Application 
{
    private ArticleDatabaseHelper articleDbHelper = new ArticleDatabaseHelper();
    private DatabaseHelper dbHelper = new DatabaseHelper();
    private SecretKey secretKey;
    private List<String> searchHistory = new ArrayList<>();
    
    private ListView<String> articleListView;

    public StudentPage() {
        try {
            this.secretKey = ArticleEncryptionUtils.getAESKeyFromPassphrase("group26key");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) 
    {
        stage.setTitle("Support360 Student");

        VBox mainLayout = new VBox(15);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));

        Label header = new Label("Welcome, " + LoginPage.loggedInUsername + "!");
        header.getStyleClass().add("label");

        GridPane searchControls = new GridPane();
        searchControls.setHgap(10);
        searchControls.setVgap(10);
        searchControls.setAlignment(Pos.CENTER);

        Label searchLabel = new Label("Search articles:");
        TextField searchField = new TextField();
        searchField.setPromptText("Enter search terms...");
        searchField.setPrefWidth(300);

        ComboBox<String> levelComboBox = new ComboBox<>(FXCollections.observableArrayList(
            "all", "beginner", "intermediate", "advanced", "expert"
        ));
        levelComboBox.setValue("all");

        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("button");

        articleListView = new ListView<>();
        articleListView.setPrefHeight(200);
        articleListView.setPrefWidth(400);

        HBox helpButtons = new HBox(10);
        helpButtons.setAlignment(Pos.CENTER);

        Button genericHelpButton = new Button("I'm Confused (Generic Help)");
        Button specificHelpButton = new Button("Can't Find Info (Specific Help)");
        helpButtons.getChildren().addAll(genericHelpButton, specificHelpButton);

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("button");

        mainLayout.getChildren().addAll(
            header,
            searchLabel,
            searchField,
            levelComboBox,
            searchButton,
            articleListView,
            helpButtons,
            logoutButton
        );

        searchButton.setOnAction(event -> {
            try {
                articleDbHelper.connectToDatabase();
                String searchTerm = searchField.getText();
                
                if (!searchTerm.trim().isEmpty()) {
                    searchHistory.add(searchTerm);
                }

                List<String> articles = articleDbHelper.searchArticles(searchTerm);
                
                articleListView.getItems().clear();
                articleListView.getItems().addAll(articles);
                
                System.out.println("Found " + articles.size() + " matching articles");
            } catch (Exception e) {
                showError("Search Error", "Failed to perform search: " + e.getMessage());
            } finally {
                articleDbHelper.closeConnection();
            }
        });

        articleListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedTitle = articleListView.getSelectionModel().getSelectedItem();
                if (selectedTitle != null) {
                    displayArticle(selectedTitle);
                }
            }
        });

        genericHelpButton.setOnAction(event -> 
            showHelpDialog("generic", "describe your question:"));
        
        specificHelpButton.setOnAction(event -> 
            showHelpDialog("specific", "describe your question:"));

        logoutButton.setOnAction(event -> {
            LoginPage loginPage = new LoginPage();
            Stage loginStage = new Stage();
            loginPage.start(loginStage);
            LoginPage.loggedInUsername = null;
            stage.close();
        });

        Scene scene = new Scene(mainLayout, 600, 700);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void displayArticle(String title) 
    {
        try {
            articleDbHelper.connectToDatabase();
            String encryptedBody = articleDbHelper.getArticleBody(title);
            
            if (encryptedBody != null) {
                String decryptedBody = ArticleEncryptionUtils.decrypt(encryptedBody, secretKey);
                
                Stage articleStage = new Stage();
                articleStage.setTitle("Article: " + title);
                
                VBox layout = new VBox(10);
                layout.setPadding(new Insets(15));
                
                Text titleText = new Text(title);
                titleText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                
                TextArea contentArea = new TextArea(decryptedBody);
                contentArea.setWrapText(true);
                contentArea.setEditable(false);
                contentArea.setPrefRowCount(20);
                contentArea.setPrefColumnCount(60);
                
                layout.getChildren().addAll(titleText, contentArea);
                
                ScrollPane scrollPane = new ScrollPane(layout);
                scrollPane.setFitToWidth(true);
                
                Scene articleScene = new Scene(scrollPane, 800, 600);
                articleStage.setScene(articleScene);
                articleStage.show();
            }
        } catch (Exception e) 
        {
            showError("Display Error", "Failed to display article: " + e.getMessage());
        } finally {
            articleDbHelper.closeConnection();
        }
    }

    private void showHelpDialog(String messageType, String prompt) 
    {
        Stage dialogStage = new Stage();
        VBox dialogLayout = new VBox(10);
        dialogLayout.setPadding(new Insets(20));
        dialogLayout.setAlignment(Pos.CENTER);

        Label promptLabel = new Label(prompt);
        TextArea messageArea = new TextArea();
        messageArea.setWrapText(true);
        
        messageArea.setPrefRowCount(5);

        Button sendButton = new Button("Send Message");
        sendButton.getStyleClass().add("button");

        dialogLayout.getChildren().addAll(promptLabel, messageArea, sendButton);

        sendButton.setOnAction(e -> {
            try {
                dbHelper.connectToDatabase();
                dbHelper.addStudentMessage(
                    LoginPage.loggedInUsername,
                    messageType,
                    messageArea.getText(),
                    String.join(", ", searchHistory)
                );
                dialogStage.close();
                showInfo("Message Sent", "Message sent.");
            } catch (Exception ex) {
                showError("Error", "Failed to send message: " + ex.getMessage());
            } finally {
                dbHelper.closeConnection();
            }
        });

        Scene dialogScene = new Scene(dialogLayout, 400, 300);
        dialogScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        
        dialogStage.setScene(dialogScene);
        dialogStage.show();
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

    public static void main(String[] args) {
        launch(args);
    }
}