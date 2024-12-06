package src;

public class Tests {
   private ArticleDatabaseHelper dbHelper;
   private String testResults;
   private int testsPassed;
   private int totalTests;

   public Tests() {
       dbHelper = new ArticleDatabaseHelper();
       testResults = "";
       testsPassed = 0;
       totalTests = 0;
   }

   private void checkTest(String testName, boolean condition) {
       totalTests++;
       if (condition) {
           testsPassed++;
           testResults += "PASSED: " + testName + "\n";
       } else {
           testResults += "FAILED: " + testName + "\n";
       }
   }

   public void runAllTests() {
       try {
           testDatabaseConnection();
           testEncryption();
           
           System.out.println("\nTest Results:");
           System.out.println(testResults);
           System.out.println("Tests Passed: " + testsPassed + "/" + totalTests);
           
       } catch (Exception e) {
           System.out.println("Testing failed with error: " + e.getMessage());
       }
   }

   private void testDatabaseConnection() {
       try {
           dbHelper.connectToDatabase();
           dbHelper.closeConnection();
           checkTest("Database Connection", true);
       } catch (Exception e) {
           checkTest("Database Connection", false);
       }
   }

   private void testEncryption() {
       try {
           String testContent = "Test Content";
           String encrypted = ArticleEncryptionHelper.encryptBody(testContent);
           String decrypted = ArticleEncryptionHelper.decryptBody(encrypted);

           boolean encryptionSuccess = !encrypted.equals(testContent);
           boolean decryptionSuccess = decrypted.equals(testContent);

           checkTest("Content Encryption", encryptionSuccess);
           checkTest("Content Decryption", decryptionSuccess);
           
       } catch (Exception e) {
           checkTest("Encryption Operations", false);
       }
   }


   public static void main(String[] args) {
       Tests tester = new Tests();
       tester.runAllTests();
   }
}