package src;
import java.util.List;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class StudentMessagesPanel extends VBox {
    private DatabaseHelper dbHelper;
    private ListView<VBox> messageListView;

    public StudentMessagesPanel() {
        this.dbHelper = new DatabaseHelper();
        this.setPadding(new Insets(10));
        this.setSpacing(10);

        Label titleLabel = new Label("Student Messages");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #FFB300;");
        refreshButton.setOnAction(e -> refreshMessages());

        messageListView = new ListView<>();
        messageListView.setPrefHeight(400);

        this.getChildren().addAll(titleLabel, refreshButton, messageListView);
        refreshMessages();
    }

    private void refreshMessages() {
        try {
            dbHelper.connectToDatabase();
            List<Map<String, String>> messages = dbHelper.getStudentMessages();
            messageListView.getItems().clear();
            
            for (Map<String, String> message : messages) {
                messageListView.getItems().add(createMessageBox(message));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load messages");
        } finally {
            dbHelper.closeConnection();
        }
    }

    private VBox createMessageBox(Map<String, String> message) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-border-color: #A15757; -fx-border-radius: 5;");

        //uname and message
        Label userLabel = new Label("From: " + message.get("username"));
        userLabel.setStyle("-fx-font-weight: bold;");

        Label typeLabel = new Label("Type: " + message.get("type") + " Help Request");

        //content
        TextArea messageArea = new TextArea(message.get("message"));
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setPrefRowCount(3);

        //history
        Label historyLabel = new Label("Search History: " + message.get("searchHistory"));
        historyLabel.setWrapText(true);

        Label timeLabel = new Label("Time: " + message.get("created"));
        timeLabel.setStyle("-fx-font-size: 11px;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button resolveButton = new Button("Resolve");
        resolveButton.setStyle("-fx-background-color: #FFB300;");
        resolveButton.setOnAction(e -> {
            String messageId = message.get("id");
            if (messageId != null) {
                showResolveDialog(Integer.parseInt(messageId));
            }
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #FFB300;");
        deleteButton.setOnAction(e -> {
            String messageId = message.get("id");
            if (messageId != null) {
                try {
                    dbHelper.connectToDatabase();
                    dbHelper.deleteStudentMessage(Integer.parseInt(messageId));
                    refreshMessages();
                } catch (Exception ex) {
                    showError("Failed to delete message: " + ex.getMessage());
                } finally {
                    dbHelper.closeConnection();
                }
            }
        });

        buttonBox.getChildren().addAll(resolveButton, deleteButton);

        box.getChildren().addAll(
            userLabel,
            typeLabel,
            messageArea,
            historyLabel,
            timeLabel,
            buttonBox
        );

        return box;
    }

    private void showResolveDialog(int messageId) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Resolve Message");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #A15757;");

        Label promptLabel = new Label("Enter your response:");
        promptLabel.setStyle("-fx-text-fill: white;");

        TextArea responseArea = new TextArea();
        responseArea.setWrapText(true);
        responseArea.setPrefRowCount(5);

        Button sendButton = new Button("Send Response");
        sendButton.setStyle("-fx-background-color: #FFB300;");
        sendButton.setOnAction(e -> {
            try {
                dbHelper.connectToDatabase();
                dbHelper.resolveStudentMessage(messageId, responseArea.getText());
                dialog.close();
                refreshMessages();
            } catch (Exception ex) {
                showError("Failed to send response: " + ex.getMessage());
            } finally {
                dbHelper.closeConnection();
            }
        });

        layout.getChildren().addAll(promptLabel, responseArea, sendButton);
        Scene scene = new Scene(layout, 400, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}