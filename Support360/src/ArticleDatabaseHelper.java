package src;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String articlesTable = "CREATE TABLE IF NOT EXISTS articles ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "title VARCHAR(255), "
            + "description VARCHAR(255), "
            + "keywords VARCHAR(255), "
            + "body TEXT, "
            + "level VARCHAR(20), "
            + "group_id INT, "
            + "special_group_id INT)";
        statement.execute(articlesTable);

        String groupsTable = "CREATE TABLE IF NOT EXISTS article_groups ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "group_name VARCHAR(255), "
            + "created_by VARCHAR(255))";
        statement.execute(groupsTable);

        String specialGroupsTable = "CREATE TABLE IF NOT EXISTS special_access_groups ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "group_name VARCHAR(255), "
            + "created_by VARCHAR(255))";
        statement.execute(specialGroupsTable);

        String accessRightsTable = "CREATE TABLE IF NOT EXISTS group_access_rights ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "group_id INT, "
            + "username VARCHAR(255), "
            + "access_type VARCHAR(50), "
            + "group_type VARCHAR(50))";
        statement.execute(accessRightsTable);

        String searchHistoryTable = "CREATE TABLE IF NOT EXISTS search_history ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "username VARCHAR(255), "
            + "search_terms TEXT, "
            + "search_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        statement.execute(searchHistoryTable);
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
        
        String query = "SELECT title, description, keywords FROM articles WHERE 1=1";
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            query += " AND (LOWER(title) LIKE LOWER(?) OR " +
                    "LOWER(description) LIKE LOWER(?) OR " +
                    "LOWER(keywords) LIKE LOWER(?))";
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                pstmt.setString(3, searchPattern);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                try {
                    String encryptedTitle = rs.getString("title");
                    if (encryptedTitle != null) {
                        String decryptedTitle = ArticleEncryptionUtils.decrypt(encryptedTitle, secretKey);
                        results.add(decryptedTitle);
                    }
                } catch (Exception e) {
                    System.err.println("Error decrypting article data: " + e.getMessage());
                }
            }
        }
        return results;
    }
    
    public List<String> searchArticlesSimple(String searchTerm) throws SQLException {
        List<String> results = new ArrayList<>();
        String query = "SELECT title FROM articles WHERE " +
                      "LOWER(title) LIKE LOWER(?) OR " +
                      "LOWER(description) LIKE LOWER(?) OR " +
                      "LOWER(keywords) LIKE LOWER(?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String encryptedTitle = rs.getString("title");
                try {
                    String decryptedTitle = ArticleEncryptionUtils.decrypt(encryptedTitle, secretKey);
                    results.add(decryptedTitle);
                } catch (Exception e) {
                    System.err.println("Error decrypting title: " + e.getMessage());
                }
            }
        }
        return results;
    }
    
    private boolean isAdmin(String username) {
        try {
            String query = "SELECT role FROM cse360users WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return "Admin".equals(rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking admin status: " + e.getMessage());
        }
        return false;
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
            String encryptedOldTitle = ArticleEncryptionUtils.encrypt(oldTitle, secretKey);
            
            String selectQuery = "SELECT title, description, keywords, body FROM articles WHERE title = ?";
            String existingTitle = null;
            String existingDescription = null;
            String existingKeywords = null;
            String existingBody = null;
            
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
            String header = reader.readLine();
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
    
    
    //Phase 3 stuff
    
    public void createGroup(String groupName, String creatorUsername) throws SQLException {
        String query = "INSERT INTO article_groups (group_name, created_by) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, groupName);
            pstmt.setString(2, creatorUsername);
            pstmt.executeUpdate();
            System.out.println("Group created: " + groupName + " by " + creatorUsername);
        }
    }
    
    public void createSpecialGroup(String groupName, String creatorUsername) throws SQLException 
    {
        String query = "INSERT INTO special_access_groups (group_name, created_by) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) 
        {
            pstmt.setString(1, groupName);
            pstmt.setString(2, creatorUsername);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) 
            {
                int groupId = rs.getInt(1);
                addGroupAccess(groupId, creatorUsername, "admin", "special");
            }
        }
    }
    
    public void addGroupAccess(int groupId, String username, String accessType, String groupType) 
    		throws SQLException {
    		    String query = "INSERT INTO group_access_rights (group_id, username, access_type, group_type) "
    		        + "VALUES (?, ?, ?, ?)";
    		    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
    		        pstmt.setInt(1, groupId);
    		        pstmt.setString(2, username);
    		        pstmt.setString(3, accessType);
    		        pstmt.setString(4, groupType);
    		        pstmt.executeUpdate();
    		    }
    		}
    
    public void addArticleToGroup(int articleId, int groupId, boolean isSpecial) throws SQLException {
        String query = "UPDATE articles SET " + 
            (isSpecial ? "special_group_id" : "group_id") + " = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, articleId);
            pstmt.executeUpdate();
        }
    }
    
    
    
    public boolean hasGroupAccess(int groupId, String username, String accessType, String groupType) 
    		throws SQLException {
    		    String query = "SELECT 1 FROM group_access_rights WHERE group_id = ? AND username = ? "
    		        + "AND access_type = ? AND group_type = ?";
    		    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
    		        pstmt.setInt(1, groupId);
    		        pstmt.setString(2, username);
    		        pstmt.setString(3, accessType);
    		        pstmt.setString(4, groupType);
    		        return pstmt.executeQuery().next();
    		    }
    		}
    

    public List<Map<String, String>> searchArticlesWithFilters(String searchTerm, String level, 
        Integer groupId, String username) throws SQLException 
    {
        StringBuilder query = new StringBuilder(
            "SELECT a.*, g.group_name, sg.group_name as special_group_name FROM articles a "
            + "LEFT JOIN article_groups g ON a.group_id = g.group_id "
            + "LEFT JOIN special_access_groups sg ON a.special_group_id = sg.group_id "
            + "WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            query.append("AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ? "
                + "OR LOWER(keywords) LIKE ? OR LOWER(author) LIKE ?) ");
            String term = "%" + searchTerm.toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (level != null && !level.equals("all")) {
            query.append("AND level = ? ");
            params.add(level);
        }

        if (groupId != null) {
            query.append("AND (group_id = ? OR special_group_id = ?) ");
            params.add(groupId);
            params.add(groupId);
        }

        query.append("AND (special_group_id IS NULL OR EXISTS (SELECT 1 FROM group_access_rights "
            + "WHERE group_id = a.special_group_id AND username = ? AND access_type IN ('admin', 'view'))) ");
        params.add(username);

        List<Map<String, String>> results = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> article = new HashMap<>();
                article.put("id", rs.getString("id"));
                
                try {
                    article.put("title", ArticleEncryptionUtils.decrypt(rs.getString("title"), secretKey));
                    article.put("description", ArticleEncryptionUtils.decrypt(rs.getString("description"), secretKey));
                    article.put("author", rs.getString("author"));
                    article.put("level", rs.getString("level"));
                    article.put("group_name", rs.getString("group_name"));
                    article.put("special_group_name", rs.getString("special_group_name"));
                } catch (Exception e) {
                    System.err.println("Error decrypting article fields: " + e.getMessage());
                    continue;
                }
                results.add(article);
            }
        }
        return results;
    }

    public void setArticleLevel(int articleId, String level) throws SQLException 
    {
    	
        String query = "UPDATE articles SET level = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, level);
            pstmt.setInt(2, articleId);
            pstmt.executeUpdate();
        }
    }

    public List<String> getAvailableGroups(String username) throws SQLException 
    {
        String query = "SELECT DISTINCT g.group_name FROM article_groups g "
            + "LEFT JOIN group_access_rights ar ON g.group_id = ar.group_id "
            + "WHERE ar.username = ? OR g.created_by = ?";
        List<String> groups = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                groups.add(rs.getString("group_name"));
            }
        }
        return groups;
    }

    public void recordSearchHistory(String username, String searchTerms) throws SQLException 
    {
        String query = "INSERT INTO search_history (username, search_terms) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, searchTerms);
            pstmt.executeUpdate();
        }
    }

    public List<String> getSearchHistory(String username) throws SQLException 
    {
        String query = "SELECT search_terms FROM search_history WHERE username = ? "
            + "ORDER BY search_time DESC LIMIT 10";
        List<String> history = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                history.add(rs.getString("search_terms"));
            }
        }
        return history;
    }

    public void closeConnection() 
    {
        try {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
            System.out.println("Database connection closed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}