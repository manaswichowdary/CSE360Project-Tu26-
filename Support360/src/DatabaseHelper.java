package src;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class DatabaseHelper {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:./database/project1_CSE360m2";

    // Database credentials
    static final String USER = "group26";
    static final String PASS = "ayogroup26";

    private Connection connection = null;
    private Statement statement = null;

    /**
     * @throws SQLException
     */
    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER); // Load the JDBC driver
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            createTables();  // Create the necessary tables if they don't exist
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }
    
    /**
     * @return
     */
    public Connection getConnection() {
        return connection;
    }

    // Create necessary tables
    private void createTables() throws SQLException {
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "username VARCHAR(255) UNIQUE, "
                + "password VARCHAR(255), "
                + "first_name VARCHAR(100), "
                + "middle_name VARCHAR(100), "
                + "last_name VARCHAR(100), "
                + "preferred_first_name VARCHAR(100), "
                + "email VARCHAR(255) UNIQUE, "
                + "role VARCHAR(255))";  // Default role is Student/Instructor/Admin
        statement.execute(userTable);

        String rolesTable = "CREATE TABLE IF NOT EXISTS user_roles ("
                + "user_id INT, "
                + "role VARCHAR(50), "
                + "PRIMARY KEY (user_id, role), "
                + "FOREIGN KEY (user_id) REFERENCES cse360users(id))";
        statement.execute(rolesTable);
        
        String otpTable = "CREATE TABLE IF NOT EXISTS user_otps ("
                + "user_id INT, "
                + "otp VARCHAR(6), "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (user_id) REFERENCES cse360users(id))";
        statement.execute(otpTable);

        String invitationTable = "CREATE TABLE IF NOT EXISTS invitations ("
                + "code VARCHAR(100) PRIMARY KEY, "
                + "roles VARCHAR(100), "
                + "used BOOLEAN DEFAULT FALSE)";
        statement.execute(invitationTable);

        String passwordResetsTable = "CREATE TABLE IF NOT EXISTS password_resets ("
                + "user_id INT, "
                + "reset_token VARCHAR(100), "
                + "expiration TIMESTAMP, "
                + "used BOOLEAN DEFAULT FALSE, "
                + "FOREIGN KEY (user_id) REFERENCES cse360users(id))";
        statement.execute(passwordResetsTable);
    }

    // Check if the database is empty
    /**
     * @return
     * @throws SQLException
     */
    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM cse360users";
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count") == 0;
        }
        return true;
    }
    
    /**
     * @param query
     * @return
     * @throws SQLException
     */
    public String runSQLQuery(String query) throws SQLException {
        StringBuilder result = new StringBuilder();  // Use StringBuilder to accumulate the result
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            
            // Iterate through the result set and accumulate the results
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) result.append(", ");  // Append commas between columns
                    
                    String value = rs.getString(i);  // Retrieve column data
                    if (value == null) {
                        result.append("");  // Append empty string if the value is NULL
                    } else {
                        result.append(value);  // Append the actual column data
                    }
                }
                result.append("\n");  // Add a newline after each row
            }
        }
        return result.toString().trim();  // Return the accumulated result as a string, trimming any extra spaces
    }




    // Register the first user as Admin
    /**
     * @param username
     * @param password
     * @throws SQLException
     */
    public void registerFirstUser(String username, String password) throws SQLException {
        if (isDatabaseEmpty()) {
        	String insertAdmin = "INSERT INTO cse360users (USERNAME, PASSWORD, ROLE) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertAdmin)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, "Admin");
                pstmt.executeUpdate();
                System.out.println("First user registered as Admin.");
            }
        }
    }

    // Register a user with a given role
    /**
     * @param username
     * @param password
     * @param role
     * @throws SQLException
     */
    public void register(String username, String password, String role) throws SQLException {
    	String insertUser = "INSERT INTO cse360users (USERNAME, PASSWORD, ROLE) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
        }
        System.out.println(role);
    }

    // Complete account setup by adding first name, email, etc.
    /**
     * @param userId
     * @param firstName
     * @param middleName
     * @param lastName
     * @param preferredFirstName
     * @throws SQLException
     */
    public void finishAccountSetup(int userId, String firstName, String middleName, String lastName, String preferredFirstName) throws SQLException {
        String updateUser = "UPDATE cse360users SET first_name = ?, middle_name = ?, last_name = ?, preferred_first_name = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateUser)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, middleName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, preferredFirstName);
            pstmt.setInt(5, userId);
            pstmt.executeUpdate();
            System.out.println("Account setup completed.");
        }
    }
    
    /**
     * @param username
     * @param password
     * @return
     * @throws SQLException
     */
    public String getRole(String username, String password) throws SQLException {
        String role = null;
        String query = "SELECT role FROM cse360users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                role = rs.getString("role");
            }
        }
        return role;
    }


    // Login method for validating credentials
    /**
     * @param username
     * @param password
     * @param role
     * @return
     * @throws SQLException
     */
    public boolean login(String username, String password, String role) throws SQLException {
        String query = "SELECT * FROM cse360users WHERE 'username' = ? AND 'password' = ? AND 'role' = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Check if the user exists
    /**
     * @param username
     * @return
     */
    public boolean doesUserExist(String username) {
        String query = "SELECT COUNT(*) FROM cse360users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get roles for a given user
    /**
     * @param userId
     * @return
     * @throws SQLException
     */
    public List<String> getUserRoles(int userId) throws SQLException {
        List<String> roles = new ArrayList<>();
        String query = "SELECT role FROM user_roles WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("role"));
                }
            }
        }
        return roles;
    }

    // Create invitation code
    /**
     * @param code
     * @param roles
     * @throws SQLException
     */
    public void createInvitation(String code, String roles) throws SQLException {
        String insertCode = "INSERT INTO invitations (code, roles) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertCode)) {
            pstmt.setString(1, code);
            pstmt.setString(2, roles);
            pstmt.executeUpdate();
            System.out.println("Invitation code created.");
        }
    }

    // Validate invitation code
    /**
     * @param code
     * @return
     * @throws SQLException
     */
    public String validateInvitation(String code) throws SQLException {
        String query = "SELECT roles FROM invitations WHERE code = ? AND used = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("roles");
                }
            }
        }
        return null;  // Invalid code
    }

    // Mark invitation as used
    /**
     * @param code
     * @throws SQLException
     */
    public void markInvitationUsed(String code) throws SQLException {
        String update = "UPDATE invitations SET used = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(update)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        }
    }

    // Create password reset token
    /**
     * @param userId
     * @param token
     * @param expiration
     * @throws SQLException
     */
    public void createResetToken(int userId, String token, Timestamp expiration) throws SQLException {
        String insertToken = "INSERT INTO password_resets (user_id, reset_token, expiration) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertToken)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, token);
            pstmt.setTimestamp(3, expiration);
            pstmt.executeUpdate();
            System.out.println("Reset token created.");
        }
    }

    // Validate reset token
    /**
     * @param token
     * @return
     * @throws SQLException
     */
    public boolean validateResetToken(String token) throws SQLException {
        String query = "SELECT * FROM password_resets WHERE reset_token = ? AND used = FALSE AND expiration > CURRENT_TIMESTAMP";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, token);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Mark reset token as used
    /**
     * @param token
     * @throws SQLException
     */
    public void markResetTokenUsed(String token) throws SQLException {
        String update = "UPDATE password_resets SET used = TRUE WHERE reset_token = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(update)) {
            pstmt.setString(1, token);
            pstmt.executeUpdate();
        }
    }

    // Delete user
    /**
     * @param userId
     * @throws SQLException
     */
    public void deleteUser(int userId) throws SQLException {
        String deleteUser = "DELETE FROM cse360users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteUser)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            System.out.println("User deleted.");
        }
    }
    
 // Method to generate a random 6-digit OTP
    /**
     * @return
     */
    public String generateOTP() {
        int otp = (int)(Math.random() * 900000) + 100000;  // Generate random 6-digit number
        return String.valueOf(otp);
    }

    // Method to store OTP in the database
    /**
     * @param userId
     * @param otp
     * @throws SQLException
     */
    public void storeOTP(int userId, String otp) throws SQLException {
        String insertOTP = "INSERT INTO user_otps (user_id, otp) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertOTP)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, otp);
            pstmt.executeUpdate();
            System.out.println("OTP stored in the database.");
        }
    }

    // Method to get user ID by username
    /**
     * @param username
     * @return
     * @throws SQLException
     */
    public int getUserIdByUsername(String username) throws SQLException {
        String query = "SELECT id FROM cse360users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;  // User not found
    }
    
 // Validate the OTP for a user
    /**
     * @param userId
     * @param otp
     * @return
     * @throws SQLException
     */
    public boolean validateOTP(int userId, String otp) throws SQLException {
        String query = "SELECT * FROM user_otps WHERE user_id = ? AND otp = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, otp);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();  // Return true if the OTP matches
        }
    }


    // Close database connection
    /**
     * 
     */
    public void closeConnection() {
        try {
            if (statement != null) statement.close();
        } catch (SQLException se2) {
            se2.printStackTrace();
        }
        try {
            if (connection != null) connection.close();
        } catch (SQLException se) {
        	se.printStackTrace();
        }
    }
}
        
