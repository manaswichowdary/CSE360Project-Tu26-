package src;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.List;

public class Support360SystemTests 
{
    private ArticleDatabaseHelper articleDb;
    private DatabaseHelper userDb;
    
    @Before
    public void setup() 
    {
        articleDb = new ArticleDatabaseHelper();
        userDb = new DatabaseHelper();
    }

    @Test
    public void testConnection() 
    {
        try 
        {
            articleDb.connectToDatabase();
            assertTrue(true);
            articleDb.closeConnection();
        } catch (Exception e) 
        {
            fail("Connection failed: " + e.getMessage());
        }
    }

    @Test
    public void testAddArticle() 
    {
        try {
            articleDb.connectToDatabase();
            articleDb.addArticle("Test Title", "Test Desc", "test", "content");
            assertTrue(true);
            articleDb.closeConnection();
        } catch (Exception e) {
            fail("Add failed: " + e.getMessage());
        }
    }

    @Test
    public void testSearchArticle() {
        try {
            articleDb.connectToDatabase();
            List<String> results = articleDb.searchArticles("Test");
            assertNotNull(results);
            articleDb.closeConnection();
        } catch (Exception e) {
            fail("Search failed: " + e.getMessage());
        }
    }

    @Test
    public void testCreateGroup() {
        try {
            articleDb.connectToDatabase();
            articleDb.createGroup("TestGroup", "admin");
            assertTrue(true);
            articleDb.closeConnection();
        } catch (Exception e) {
            fail("Group creation failed: " + e.getMessage());
        }
    }
}