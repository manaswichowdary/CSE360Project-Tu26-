package src;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class StudentPage extends Application {
    private ArticleDatabaseHelper articleDbHelper = new ArticleDatabaseHelper();
    private DatabaseHelper dbHelper = new DatabaseHelper();
    private SecretKey secretKey;
    private List<String> searchHistory = new ArrayList<>();
    private ListView<String> articleListView;
    private String currentlySelectedArticle = null;

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

        Button exportButton = new Button("Export to PDF");
        exportButton.getStyleClass().add("button");
        exportButton.setDisable(true);

        HBox helpButtons = new HBox(10);
        helpButtons.setAlignment(Pos.CENTER);

        Button genericHelpButton = new Button("I'm Confused (Generic Help)");
        Button specificHelpButton = new Button("Can't Find Info (Specific Help)");
        helpButtons.getChildren().addAll(genericHelpButton, specificHelpButton);

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("button");
        
        Button inboxButton = new Button("Message Inbox");
        inboxButton.getStyleClass().add("button");

        mainLayout.getChildren().addAll(
            header,
            searchLabel,
            searchField,
            levelComboBox,
            searchButton,
            articleListView,
            inboxButton,
            exportButton,
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
        
        inboxButton.setOnAction(event -> showInbox());
        
        articleListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedTitle = articleListView.getSelectionModel().getSelectedItem();
                if (selectedTitle != null) {
                    displayArticle(selectedTitle);
                }
            }
        });

        articleListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            exportButton.setDisable(newVal == null);
            currentlySelectedArticle = newVal;
        });

        exportButton.setOnAction(event -> {
            if (currentlySelectedArticle != null) {
                exportArticleToPDF(currentlySelectedArticle);
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

    private void displayArticle(String title) {
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
        } catch (Exception e) {
            showError("Display Error", "Failed to display article: " + e.getMessage());
        } finally {
            articleDbHelper.closeConnection();
        }
    }
    
    private void showInbox() {
        Stage inboxStage = new Stage();
        inboxStage.setTitle("Message Inbox");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #A15757;");

        ListView<VBox> messageList = new ListView<>();
        messageList.setPrefHeight(400);
        messageList.setStyle("-fx-control-inner-background: white;");

        try {
            dbHelper.connectToDatabase();
            List<Map<String, String>> messages = dbHelper.getStudentInbox(LoginPage.loggedInUsername);
            
            for (Map<String, String> message : messages) {
                VBox messageBox = new VBox(5);
                messageBox.setPadding(new Insets(10));
                messageBox.setStyle("-fx-background-color: white; -fx-border-color: #A15757; -fx-border-radius: 5;");
                
                Label typeLabel = new Label("Type: " + message.get("type") + " Help Request");
                typeLabel.setStyle("-fx-font-weight: bold;");
                
                TextArea questionArea = new TextArea("Your Question:\n" + message.get("message"));
                questionArea.setEditable(false);
                questionArea.setWrapText(true);
                questionArea.setPrefRowCount(2);
                questionArea.setStyle("-fx-control-inner-background: #f8f8f8;");
                
                TextArea responseArea = new TextArea("Response:\n" + message.get("response"));
                responseArea.setEditable(false);
                responseArea.setWrapText(true);
                responseArea.setPrefRowCount(2);
                responseArea.setStyle("-fx-control-inner-background: #f0f0f0;");
                
                Label timeLabel = new Label("Time: " + message.get("created"));
                timeLabel.setStyle("-fx-font-size: 11px;");
                
                messageBox.getChildren().addAll(typeLabel, questionArea, responseArea, timeLabel);
                messageList.getItems().add(messageBox);
            }
            
            if (messageList.getItems().isEmpty()) {
                Label emptyLabel = new Label("No messages found");
                emptyLabel.setStyle("-fx-text-fill: white;");
                layout.getChildren().add(emptyLabel);
            }
        } catch (SQLException e) {
            showError("Error", "Failed to load inbox: " + e.getMessage());
        } finally {
            dbHelper.closeConnection();
        }

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #FFB300;");
        refreshButton.setOnAction(e -> {
            messageList.getItems().clear();
            showInbox();
        });

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #FFB300;");
        closeButton.setOnAction(e -> inboxStage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(refreshButton, closeButton);

        layout.getChildren().addAll(
            new Label("Your Messages:") {{
                setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
            }},
            messageList,
            buttonBox
        );

        Scene scene = new Scene(layout, 600, 500);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        inboxStage.setScene(scene);
        inboxStage.show();
    }

    private void exportArticleToPDF(String title) {
        try {
            articleDbHelper.connectToDatabase();
            String encryptedBody = articleDbHelper.getArticleBody(title);
            
            if (encryptedBody != null) {
                String decryptedBody = ArticleEncryptionUtils.decrypt(encryptedBody, secretKey);
                
                String projectPath = new File(".").getAbsolutePath();
                File pdfDir = new File(projectPath, "pdfs");
                if (!pdfDir.exists()) {
                    pdfDir.mkdirs();
                }
                
                String fileName = pdfDir.getPath() + File.separator + 
                                title.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
                                
                PDDocument document = new PDDocument();
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText(title);
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 720);
                

                String[] lines = decryptedBody.split("\n");
                float leading = 15;
                for (String line : lines) {
                    List<String> wrappedLines = wrapText(line, PDType1Font.HELVETICA, 12, page.getMediaBox().getWidth() - 100);
                    for (String wrappedLine : wrappedLines) {
                        contentStream.showText(wrappedLine);
                        contentStream.newLineAtOffset(0, -leading);
                    }
                }
                contentStream.endText();
                contentStream.close();

                document.save(fileName);
                document.close();
                
                showInfo("Success", "Article exported to: " + fileName);
            }
        } catch (Exception e) {
            showError("Export Error", "Failed to export article: " + e.getMessage());
        } finally {
            articleDbHelper.closeConnection();
        }
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        
        for (String word : words) {
            if (line.length() > 0) {
                String testLine = line + " " + word;
                if (font.getStringWidth(testLine) * fontSize / 1000f <= maxWidth) {
                    line.append(" ").append(word);
                    continue;
                }
            } else if (font.getStringWidth(word) * fontSize / 1000f <= maxWidth) {
                line.append(word);
                continue;
            }
            if (line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    private void showHelpDialog(String messageType, String prompt) {
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