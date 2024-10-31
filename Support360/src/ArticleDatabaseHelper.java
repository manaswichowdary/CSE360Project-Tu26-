package src;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * based off database helper
 */
public class ArticleDatabaseHelper 
{

    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:./database/articles_db";
    static final String USER = "group26";
    static final String PASS = "ayogroup26";

    private Connection connection = null;
    private Statement statement = null;

    public void connectToDatabase() throws SQLException 
    {
        try 
        {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            createTables();
        } catch (ClassNotFoundException e) 
        {
            throw new SQLException("JDBC Driver not found", e);
        }
    }

    private void createTables() throws SQLException 
    {
        String articlesTable = "CREATE TABLE IF NOT EXISTS articles (" +
                               "id INT AUTO_INCREMENT PRIMARY KEY, " +
                               "title VARCHAR(255), " +
                               "description VARCHAR(255), " +
                               "keywords VARCHAR(255), " +
                               "body TEXT)";
        statement.execute(articlesTable);
    }

    public void addArticle(String title, String description, String keywords, String body) throws SQLException 
    {
        String query = "INSERT INTO articles (title, description, keywords, body) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) 
        {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, keywords);
            pstmt.setString(4, body);
            pstmt.executeUpdate();
        }
    }

    public List<String> searchArticles(String encryptedSearch) throws SQLException 
    {
        String query = "SELECT title FROM articles WHERE title = ?";
        List<String> results = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(query)) 
        {
            pstmt.setString(1, encryptedSearch);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) 
            {
                results.add(rs.getString("title"));
            }
        }
        return results;
    }

    public void updateArticle(String title, String newTitle, String newDescription, String newKeywords, String newBody) throws SQLException 
    {
        String query = "UPDATE articles SET title = ?, description = ?, keywords = ?, body = ? WHERE title = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) 
        {
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newDescription);
            pstmt.setString(3, newKeywords);
            pstmt.setString(4, newBody);
            pstmt.setString(5, title);
            pstmt.executeUpdate();
        }
    }

    public void deleteArticle(String title) throws SQLException 
    {
        String query = "DELETE FROM articles WHERE title = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) 
        {
            pstmt.setString(1, title);
            pstmt.executeUpdate();
        }
    }

    public void backupArticles() throws SQLException 
    {
        //NEED TO IMPLEMENT
    }

    public void closeConnection() 
    {
        try
        {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        } catch (SQLException e) 
        {
            e.printStackTrace();
        }
    }
}
