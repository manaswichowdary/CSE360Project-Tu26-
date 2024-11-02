package src;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


import javax.crypto.SecretKey;

/**
 * Article Database Helper; handles functions for the article db
 * similar to DatabaseHelper.java (for our user db), creates new db
 */
public class ArticleDatabaseHelper {
    static final String JDBC_DRIVER = "org.h2.Driver";
    
    static final String DB_URL = "jdbc:h2:./database/articles_db";
    static final String USER = "group26";
    static final String PASS = "ayogroup26";

    private Connection connection = null;
    private Statement statement = null;

    public void connectToDatabase() throws SQLException {
        try {
            System.out.println("Connecting to article database...");
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            createTables();
            System.out.println("success!");
        } catch (ClassNotFoundException e) {
            System.err.println("driver error: " + e.getMessage());
            throw new SQLException("JDBC Driver not found", e);
        }
    }

    private void createTables() throws SQLException {
        String articlesTable = "CREATE TABLE IF NOT EXISTS articles (" +
                             "id INT AUTO_INCREMENT PRIMARY KEY, " +
                             "title VARCHAR(255), " +
                             "description VARCHAR(255), " +
                             "keywords VARCHAR(255), " +
                             "body TEXT)";
        statement.execute(articlesTable);
        System.out.println("Articles table checked/created successfully");
    }

    public void addArticle(String title, String description, String keywords, String body) throws SQLException {
        String query = "INSERT INTO articles (title, description, keywords, body) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, keywords);
            pstmt.setString(4, body);
            pstmt.executeUpdate();
            System.out.println("Article added successfully: " + title);
        }
    }

    /**
     * handles article searching
     * @param searchTerm
     * @return
     * @throws SQLException
     */
    public List<String> searchArticles(String searchTerm) throws SQLException {
        List<String> results = new ArrayList<>();
        //filter articles after encryption
        String query = "SELECT title FROM articles";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String encryptedTitle = rs.getString("title");
                try {
                    //decrypt title
                    String decryptedTitle = ArticleEncryptionUtils.decrypt(encryptedTitle, secretKey);
                    
                    //if search field is empty
                    if (searchTerm == null || searchTerm.isEmpty() || 
                        decryptedTitle.toLowerCase().contains(searchTerm.toLowerCase())) 
                    {
                        results.add(decryptedTitle);
                        System.out.println("matching article: " + decryptedTitle);
                    }
                } catch (Exception e) 
                {
                    System.err.println("decryption error: " + e.getMessage());
                }
            }
        }
        
        System.out.println("Search completed. Found " + results.size() + " matching articles");
        return results;
    }

    /**
     * updates article with new content
     * @param oldTitle
     * @param newTitle
     * @param newDescription
     * @param newKeywords
     * @param newBody
     * @throws SQLException
     */
    public void updateArticle(String oldTitle, String newTitle, String newDescription, String newKeywords, String newBody) throws SQLException {
        try {
            //encrypt old title to be able to find it
            String encryptedOldTitle = ArticleEncryptionUtils.encrypt(oldTitle, secretKey);
            
            //existing data
            String selectQuery = "SELECT title, description, keywords, body FROM articles WHERE title = ?";
            String existingTitle = null;
            String existingDescription = null;
            String existingKeywords = null;
            String existingBody = null;
            
            //try connection
            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
                selectStmt.setString(1, encryptedOldTitle);
                ResultSet rs = selectStmt.executeQuery();
                
                
                if (rs.next()) 
                {
                	
                    existingTitle = ArticleEncryptionUtils.decrypt(rs.getString("title"), secretKey);
                    existingDescription = ArticleEncryptionUtils.decrypt(rs.getString("description"), secretKey);
                    existingKeywords = ArticleEncryptionUtils.decrypt(rs.getString("keywords"), secretKey);
                    
                    existingBody = ArticleEncryptionUtils.decrypt(rs.getString("body"), secretKey);
                } else {
                    throw new SQLException("No article found with title: " + oldTitle);
                }
            }
            
            //check for updated values and update artice w them
            String finalTitle = (newTitle != null && !newTitle.trim().isEmpty()) ? 
                ArticleEncryptionUtils.encrypt(newTitle, secretKey) : 
                ArticleEncryptionUtils.encrypt(existingTitle, secretKey);
            String finalDescription = (newDescription != null && !newDescription.trim().isEmpty()) ? 
                ArticleEncryptionUtils.encrypt(newDescription, secretKey) : 
                ArticleEncryptionUtils.encrypt(existingDescription, secretKey);
                
            String finalKeywords = (newKeywords != null && !newKeywords.trim().isEmpty()) ? 
                ArticleEncryptionUtils.encrypt(newKeywords, secretKey) : 
                ArticleEncryptionUtils.encrypt(existingKeywords, secretKey);
                
            
            String finalBody = (newBody != null && !newBody.trim().isEmpty()) ? 
                ArticleEncryptionUtils.encrypt(newBody, secretKey) : 
                ArticleEncryptionUtils.encrypt(existingBody, secretKey);

            //update final article
            String updateQuery = "UPDATE articles SET title = ?, description = ?, keywords = ?, body = ? WHERE title = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                updateStmt.setString(1, finalTitle);
                updateStmt.setString(2, finalDescription);
                updateStmt.setString(3, finalKeywords);
                updateStmt.setString(4, finalBody);
                
                updateStmt.setString(5, encryptedOldTitle);
                
                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected == 0) 
                {
                    throw new SQLException("update fail: " + oldTitle);
                }
                System.out.println("article " + oldTitle + " updated");
            }
        } catch (Exception e) {
            System.err.println("update error: " + e.getMessage());
            throw new SQLException("Error updating article: " + oldTitle, e);
        }
    }

    /**
     * handles article deletion
     * @param title
     * @throws SQLException
     */
    public void deleteArticle(String title) throws SQLException {
        try {
            //encrypt title for db matching
            String encryptedTitle = ArticleEncryptionUtils.encrypt(title, secretKey);
            String query = "DELETE FROM articles WHERE title = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, encryptedTitle);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected == 0) 
                {
                    throw new SQLException("article not found: " + title);
                }
                System.out.println("article deleted: " + title);
            }
        } catch (Exception e) 
        {
            System.err.println("Error during article deletion: " + e.getMessage());
            throw new SQLException("Error deleting article: " + title, e);
        }
    }

    private SecretKey secretKey;	//secret key for encryption

    public ArticleDatabaseHelper() {
        try {
            this.secretKey = ArticleEncryptionUtils.getAESKeyFromPassphrase("group26key");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("secret key error");
        }
    }
    
    /**
     * grabs body text from article
     * @param title
     * @return
     * @throws SQLException
     */
    public String getArticleBody(String title) throws SQLException { //note: article db titles are encrypted; input title string isn't
        try {
            String encryptedTitle = ArticleEncryptionUtils.encrypt(title, secretKey); //encrypt input for comparison
            String query = "SELECT body FROM articles WHERE title = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, encryptedTitle);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String body = rs.getString("body");
                    return body;
                }
                System.out.println("no encrypted title found for " + encryptedTitle);
                return null;
            }
        } catch (Exception e) {
            System.err.println("error: " + e.getMessage());
            throw new SQLException("Encryption error", e);
        }
    }

    /**
     * handles backing up article
     * @param filename
     * @throws SQLException
     * @throws IOException
     */
    public void backupArticles(String filename) throws SQLException, IOException {
        File file = new File(filename);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) 
        {
            parentDir.mkdirs();
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM articles");
             BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            
            // Write header
            writer.write("id|title|description|keywords|body");
            writer.newLine();
            
            while (rs.next()) {
                String line = String.format("%d|%s|%s|%s|%s",
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("keywords"),
                    rs.getString("body")
                );
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Created backup: " + filename);
        }
    }

    /**
     * imports articles from backup
     * @param filename
     * @throws SQLException
     * @throws IOException
     */
    public void importArticles(String filename) throws SQLException, IOException {
        statement.executeUpdate("DELETE FROM articles");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String header = reader.readLine(); // ignore header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\|", -1);
                if (fields.length < 5)
                {
                	continue;
                }

                String sql = "INSERT INTO articles (title, description, keywords, body) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, fields[1]); // title
                    pstmt.setString(2, fields[2]); // description
                    pstmt.setString(3, fields[3]); //keywords
                    pstmt.setString(4, fields[4]); // body
                    pstmt.executeUpdate();
                }
            }
            System.out.println("Articles restored from backup: " + filename);
        }
    }

    public void closeConnection() {
        try {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
            System.out.println("Database connection closed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}