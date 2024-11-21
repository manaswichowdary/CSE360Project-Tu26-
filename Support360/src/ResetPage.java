package src;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.scene.text.Text;
import java.util.Map;
import java.util.List;

/*
 * StudentMessagesPanel class displays all messages received from students
 */
public class StudentMessagesPanel extends VBox 
{
    private DatabaseHelper dbHelper;
    private ListView<String> messageListView;
    private TextArea messageDetailArea;

    /*
     * Constructor to initialize the DB
     * Displaying all Student messages
     */
    public StudentMessagesPanel() {
        this.dbHelper = new DatabaseHelper();
        this.setPadding(new Insets(10));
        this.setSpacing(10);

        Label titleLabel = new Label("Student Messages");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        messageListView = new ListView<>();
        messageListView.setPrefHeight(200);

        messageDetailArea = new TextArea();
        messageDetailArea.setEditable(false);
        messageDetailArea.setWrapText(true);
        messageDetailArea.setPrefRowCount(5);

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshMessages());

        this.getChildren().addAll(
            titleLabel,
            refreshButton,
            
            messageListView,
            new Label("Message Details:"),
            messageDetailArea
        );

        messageListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> showMessageDetails(newVal));

        refreshMessages();
    }

    /*
     * RefereshMessages method fetches recently added messages
     */
    private void refreshMessages() {
        try {
            dbHelper.connectToDatabase();
            List<Map<String, String>> messages = dbHelper.getStudentMessages();
            messageListView.getItems().clear();
            
            for (Map<String, String> message : messages) {
                String username = message.get("username");
                String type = message.get("type");
                String created = message.get("created");
                
                String listItem = String.format(
                    "[%s] %s - %s Message",
                    created.split("\\.")[0],
                    username,
                    type.substring(0, 1).toUpperCase() + type.substring(1)
                );
                
                messageListView.getItems().add(listItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("load message failure");
        } finally {
            dbHelper.closeConnection();
        }
    }

    
    
    /** 
     * Method to display the body of the Message
     * @param selectedItem
     */
    private void showMessageDetails(String selectedItem) 
    {
    
    	if (selectedItem == null) return;
        
        try {
            dbHelper.connectToDatabase();
            List<Map<String, String>> messages = dbHelper.getStudentMessages();
            
            for (Map<String, String> message : messages) {
                String created = message.get("created").split("\\.")[0];
                if (selectedItem.contains(created)) {
                    String details = String.format(
                        "From: %s\n" +
                        "Type: %s\n" +
                        "Time: %s\n\n" +
                        "Message:\n%s\n\n" +
                        "Search History:\n%s",
                        message.get("username"),
                        message.get("type"),
                        message.get("created"),
                        message.get("message"),
                        message.get("searchHistory")
                    );
                    messageDetailArea.setText(details);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("load message details");
        } finally {
            dbHelper.closeConnection();
        }
    }

    
    /** 
     * @param message
     */
    private void showError(String message) 
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
