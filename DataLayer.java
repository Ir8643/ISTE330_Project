package ISTE330_Project;

import java.sql.*;


public class DataLayer {
     
    private Connection conn;

    public void connect() {
        if (conn != null) {
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url  = "jdbc:mysql://localhost/()?serverTimezone=UTC";//insert database name, replace the ()
            String user = "root";
            String pass = "student";  

            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected to database.");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error connecting to DB: " + e.getMessage());
        }
    }

    // Input From Faculty(Abstract File)
    public int insertAbstractFromFile(int facId, String title, String authors, String filePath) {
        PreparedStatement psAbs = null;
        PreparedStatement psLink = null;
        ResultSet keys = null;
        
        int absId;

        try {
            
            // Read the abstract text from the file
            String abstractText =
                    java.nio.file.Files.readString(java.nio.file.Path.of(filePath));

            // Insert into abstract table
            String insertAbstractSql =
                    "INSERT INTO abstract (title, abstract_text, authors, file_path) " +
                    "VALUES (?, ?, ?, ?)";

            conn.setAutoCommit(false);  // transaction start

            psAbs = conn.prepareStatement(insertAbstractSql, Statement.RETURN_GENERATED_KEYS);
            psAbs.setString(1, title);
            psAbs.setString(2, abstractText);
            psAbs.setString(3, authors);
            psAbs.setString(4, filePath);

            int rows = psAbs.executeUpdate();
            if (rows == 0) {
                System.out.println("No abstract inserted.");
                conn.rollback();
                return 0;
            }

            // Get generated abs_id
            keys = psAbs.getGeneratedKeys();
            if (keys.next()) {
                absId = keys.getInt(1);
            } else {
                System.out.println("No abstract ID returned.");
                conn.rollback();
                return 0;
            }

            // Link faculty -> abstract
            String linkSql =
                    "INSERT INTO facult_abstracts (prof_id, abs_id) VALUES (?, ?)";
            psLink = conn.prepareStatement(linkSql);
            psLink.setInt(1, facId);
            psLink.setInt(2, absId);
            psLink.executeUpdate();

            conn.commit();      
            conn.setAutoCommit(true);

            System.out.println("Abstract " + absId + " inserted for faculty " + facId);
            return absId;

        } catch (Exception e) {
            System.out.println("Error in insertAbstractFromFile: " + e.getMessage());
            try {
                if (conn != null) {
                    //incase insertion fails so you don't insert half the data
                    conn.rollback();
                    conn.setAutoCommit(true);
                }
            } catch (SQLException se) {
                System.out.println("Rollback failed: " + se.getMessage());
            }
            return 0;
        } finally {
            try {
                if (keys != null) keys.close();
                if (psAbs != null) psAbs.close();
                if (psLink != null) psLink.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}
