package src;
/**
 * testing class (for phase 2 right now)
 * performs testing on non-UI classes
 */
public class Tests 
{
    private ArticleDatabaseHelper dbHelper;
    
    private String testResults; //hold test results
    private int testsPassed;
    private int totalTests;
    
    /*
     * Constructor to initialize Tests
     */
    public Tests() 
    {
    	//db
        dbHelper = new ArticleDatabaseHelper();
        testResults = "";
        
        testsPassed = 0;
        totalTests = 0;
    }
    
    
    /** 
     * Checking a particular test
     * @param testName
     * @param condition
     */
    private void checkTest(String testName, boolean condition) 
    {
        totalTests++;
        if (condition) 
        {
            testsPassed++;
            testResults += "PASSED: " + testName + "\n";
        } else 
        {
            testResults += "FAILED: " + testName + "\n";
        }
    }
    
    /*
     * Method to run all checks in once
     */
    public void runAllTests() 
    {
        
    	try {
            testDatabaseConnection();
            testEncryption();
            
            System.out.println("\nTest Results:");
            System.out.println(testResults);
            System.out.println("Tests Passed: " + testsPassed + "/" + totalTests);
            
        } catch (Exception e) 
        {
            System.out.println("Testing failed with error: " + e.getMessage());
        }
    }

    /*
     * Check connection to the DB
     */
    private void testDatabaseConnection() 
    {
        try 
        {
            dbHelper.connectToDatabase();
            dbHelper.closeConnection();
            checkTest("Database Connection", true);
        } catch (Exception e) {
            checkTest("Database Connection", false);
        }
    }
    
    /*
     * Check if Encryption worked
     */
    private void testEncryption() 
    {
        try {
            String testContent = "Test Content";
            String encrypted = ArticleEncryptionHelper.encryptBody(testContent);
            String decrypted = ArticleEncryptionHelper.decryptBody(encrypted);
            
            boolean encryptionSuccess = !encrypted.equals(testContent);
            boolean decryptionSuccess = decrypted.equals(testContent);
            
            checkTest("Content Encryption", encryptionSuccess);
            checkTest("Content Decryption", decryptionSuccess);
            
        } catch (Exception e) {
            checkTest("", false);
        }
    }


    
    /** 
     * @param args
     */
    public static void main(String[] args) {
        Tests tester = new Tests();
        tester.runAllTests();
    }
}
