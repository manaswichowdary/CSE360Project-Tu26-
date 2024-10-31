package src;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

public class ArticleEncryptionUtils 
{

    public static SecretKey getAESKeyFromPassphrase(String passphrase) 
    {
        try 
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] keyBytes = md.digest(passphrase.getBytes());
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Error generating AES key from passphrase", e);
        }
    }

    public static String encrypt(String data, SecretKey secretKey) throws Exception 
    {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedData, SecretKey secretKey) throws Exception 
    {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    // decrypts list
    public static List<String> decryptArticles(List<String> encryptedArticles, SecretKey secretKey) 
    {
        List<String> decryptedArticles = new ArrayList<>();
        for (String encryptedArticle : encryptedArticles) 
        {
            try 
            {
                String decryptedArticle = decrypt(encryptedArticle, secretKey);
                decryptedArticles.add(decryptedArticle);
            } catch (Exception e) 
            {
                e.printStackTrace();  // any errors
            }
        }
        return decryptedArticles;
    }
}
