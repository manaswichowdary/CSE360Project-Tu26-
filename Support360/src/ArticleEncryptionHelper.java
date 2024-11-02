package src;

import javax.crypto.SecretKey;

/**
 * helper methods for article encryption
 */
public class ArticleEncryptionHelper 
{

    private static final String SECRET_KEY = "123group26key123";

    //encryption
    public static String encryptArticleContent(String content) throws Exception 
    {
        SecretKey secretKey = ArticleEncryptionUtils.getAESKeyFromPassphrase(SECRET_KEY);
        return ArticleEncryptionUtils.encrypt(content, secretKey);
    }
    //decryption
    public static String decryptArticleContent(String encryptedContent) throws Exception 
    {
        SecretKey secretKey = ArticleEncryptionUtils.getAESKeyFromPassphrase(SECRET_KEY);
        return ArticleEncryptionUtils.decrypt(encryptedContent, secretKey);
    }

    public static String encryptTitle(String title) throws Exception 
    {
        return encryptArticleContent(title);
    }

    public static String decryptTitle(String encryptedTitle) throws Exception 
    {
        return decryptArticleContent(encryptedTitle);
    }

    public static String encryptDescription(String description) throws Exception 
    {
        return encryptArticleContent(description);
    }

    public static String decryptDescription(String encryptedDescription) throws Exception 
    {
        return decryptArticleContent(encryptedDescription);
    }

    public static String encryptKeywords(String keywords) throws Exception 
    {
        return encryptArticleContent(keywords);
    }

    public static String decryptKeywords(String encryptedKeywords) throws Exception 
    {
        return decryptArticleContent(encryptedKeywords);
    }

    public static String encryptBody(String body) throws Exception 
    {
        return encryptArticleContent(body);
    }

    public static String decryptBody(String encryptedBody) throws Exception 
    {
        return decryptArticleContent(encryptedBody);
    }
}
