package src;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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

//    NOT WORKING
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

//    NOT WORKING
    public void deleteArticle(String title) throws SQLException 
    {
        String query = "DELETE FROM articles WHERE title = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) 
        {
            pstmt.setString(1, title);
            pstmt.executeUpdate();
        }
    }

//    NEEDS TESTING
    public void importArticles(String filename) throws Exception {
	    // Reset the application's set of articles to empty
	    String sqlClearTable = "DELETE FROM articles";
	    Statement stmt = connection.createStatement();
	    stmt.executeUpdate(sqlClearTable);

	    int maxId = 0;

	    // Read the file and insert the data
	    BufferedReader reader = new BufferedReader(new FileReader(filename));
	    String line;
	    while ((line = reader.readLine()) != null) {
	        // Split the line into fields
	        String[] fields = line.split("\\|", -1); // -1 to include trailing empty strings

	        if (fields.length < 7) {
	            // Invalid line, skip or handle error
	            System.err.println("Invalid line in backup file: " + line);
	            continue;
	        }

	        // Extract fields
	        String title = fields[1];
	        String desc = fields[2];
	        String keywords = fields[3];
	        String body = fields[4];

	        // Insert into articles
	        String insertArticle = "INSERT INTO articles (title, description, keywords, body) VALUES (?, ?, ?, ?)";
			try (PreparedStatement pstpmt = connection.prepareStatement(insertArticle)) {
				pstpmt.setString(1, title);
				pstpmt.setString(2, desc);
				pstpmt.setString(3, keywords);
				pstpmt.setString(4, body);
				pstpmt.executeUpdate();
			}
	    }
	    reader.close();

	    // After inserting, reset the id sequence to maxId + 1
	    String sqlResetId = "ALTER TABLE articles ALTER COLUMN id RESTART WITH " + (maxId + 1);
	    stmt.executeUpdate(sqlResetId);
	    stmt.close();

	    System.out.println("Data imported successfully from: " + filename);
	}
    
//    NEEDS TESTING
    public void backupArticles(String filename) throws SQLException, Exception
    {
    	//get all data
		String sql = "SELECT * FROM articles";
		
		//setup file saving stuff
		File file = new File(filename);
	    File parentDir = file.getParentFile();
	    if (parentDir != null && !parentDir.exists()) {
	        parentDir.mkdirs();
	    }
	    
	    try (Statement stmt = connection.createStatement();
		         ResultSet rs = stmt.executeQuery(sql);
		         BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {

		        while (rs.next()) {
		            // Retrieve the encrypted data
		            String title = rs.getString("title");
		            String desc = rs.getString("description");
		            String keywords = rs.getString("keywords");
		            String body = rs.getString("body");


		            // Construct a line with fields separated by a delimiter (e.g., '|')
		            String line = String.join("|",
		                    title,
		                    desc,
		                    keywords,
		                    body);

		            // Write the line to the file
		            writer.write(line);
		            writer.newLine();
		            
		            System.out.println("Backup made at: " + filename);
		        }
		    }
    }
    
//    NOT WORKING
    public void displayArticle (String title) throws SQLException{
    	//grab specific article for specific id
    	String sql = "SELECT * FROM articles WHERE title = ?";
    	try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    	pstmt.setString(1, title);
    	try (ResultSet rs = pstmt.executeQuery()) {
    	if(rs.next()) {
    	//process article data
    	String enBody = rs.getString("body");
    	//turn strings into chars
    	
    	//BODY IS NOT PRINTING, so seems like getString isnt actually pulling the string?
    	System.out.println(enBody);
    	}}}
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
