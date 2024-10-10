package src;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility to hash passwords
 */
public class PasswordUtil 
{

    //password hashing
    /**
     * @param password the password we hash
     * @return the hashed item we return
     */
    public static String hashPassword(String password) 
    {
        try 
        {
            //use SHA256 hashing digest
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes());	//apply hashing to bytes of input password

            // covert to hex
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) 
            {
                sb.append(String.format("%02x", b));  //converts each byte to hex
            }
            return sb.toString();  //returns as hex string
        } 
        catch (NoSuchAlgorithmException e) //required exception catch
        {
            throw new RuntimeException("no SHA-256 algorithm", e);
        }
    }
}
