package ISTE330_Project;

import java.sql.*;

public class DataLayer {
     
    private Connection conn;

    public boolean connect() {
        if (conn != null) {
            return true;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Updated URL to match the database name in setup.sql
            String url  = "jdbc:mysql://localhost/Abstracts?serverTimezone=UTC";
            String user = "root";
            String pass = "student"; // Ensure this matches your local MySQL password

            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected to database.");
            return true;
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.out.println("Error connecting to DB: " + e.getMessage());
            return false;
        }
    }
    
    public void close() {
        try {
            if(conn != null) conn.close();
        } catch(SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    // ==========================================
    // FACULTY METHODS
    // ==========================================

    // [cite: 27] Input from Faculty must include an abstract file
    public int insertAbstractFromFile(int facId, String title, String authors, String filePath) {
        PreparedStatement psAbs = null;
        PreparedStatement psLink = null;
        ResultSet keys = null;
        int absId;

        try {
            String abstractText = java.nio.file.Files.readString(java.nio.file.Path.of(filePath));

            // FIXED: Column name in setup.sql is `abstract`, not abstract_text. 
            // using backticks because abstract is a reserved keyword in SQL.
            String insertAbstractSql =
                    "INSERT INTO abstract (title, `abstract`, authors) " +
                    "VALUES (?, ?, ?)";

            conn.setAutoCommit(false); 

            psAbs = conn.prepareStatement(insertAbstractSql, Statement.RETURN_GENERATED_KEYS);
            psAbs.setString(1, title);
            psAbs.setString(2, abstractText);
            psAbs.setString(3, authors);
            // Note: setup.sql does not have a file_path column, removed from query to prevent error.

            int rows = psAbs.executeUpdate();
            if (rows == 0) {
                System.out.println("No abstract inserted.");
                conn.rollback();
                return 0;
            }

            keys = psAbs.getGeneratedKeys();
            if (keys.next()) {
                absId = keys.getInt(1);
            } else {
                conn.rollback();
                return 0;
            }

            // Link faculty -> abstract [cite: 34]
            // setup.sql uses 'prof_id', matches here.
            String linkSql = "INSERT INTO faculty_abstracts (prof_id, abs_id) VALUES (?, ?)";
            psLink = conn.prepareStatement(linkSql);
            psLink.setInt(1, facId);
            psLink.setInt(2, absId);
            psLink.executeUpdate();

            conn.commit();      
            conn.setAutoCommit(true);
            return absId;

        } catch (Exception e) {
            System.out.println("Error in insertAbstractFromFile: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException se) {}
            return 0;
        } finally {
            try {
                if (keys != null) keys.close();
                if (psAbs != null) psAbs.close();
                if (psLink != null) psLink.close();
            } catch (SQLException e) {}
        }
    }

    //  Faculty search in student entries
    public String searchStudentsByInterest(String keyword) {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT s.fname, s.lname, s.email, s.program, i.keyword " +
                     "FROM student s " +
                     "JOIN student_interests si ON s.stu_id = si.stu_id " +
                     "JOIN interests i ON si.interest_id = i.interest_id " +
                     "WHERE i.keyword LIKE ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            sb.append(String.format("%-20s %-20s %-30s %-15s\n", "Name", "Email", "Program", "Interest"));
            sb.append("--------------------------------------------------------------------------------\n");
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                String name = rs.getString("fname") + " " + rs.getString("lname");
                sb.append(String.format("%-20s %-20s %-30s %-15s\n", 
                    name, rs.getString("email"), rs.getString("program"), rs.getString("keyword")));
            }
            if(!found) return "No students found with interest: " + keyword;
            
        } catch (SQLException e) {
            return "Database Error: " + e.getMessage();
        }
        return sb.toString();
    }

    // ==========================================
    // STUDENT METHODS
    // ==========================================

    // [cite: 27] Input from Student must include list of key topics (interests)
    public boolean insertStudentInterest(int stuId, String interestKeyword) {
        // 1. Check if interest exists, if not create it. 2. Link to student.
        PreparedStatement psCheck = null;
        PreparedStatement psInsertInt = null;
        PreparedStatement psLink = null;
        ResultSet rs = null;
        int interestId = -1;

        try {
            // Check if keyword exists
            String checkSql = "SELECT interest_id FROM interests WHERE keyword = ?";
            psCheck = conn.prepareStatement(checkSql);
            psCheck.setString(1, interestKeyword);
            rs = psCheck.executeQuery();

            if (rs.next()) {
                interestId = rs.getInt("interest_id");
            } else {
                // Insert new interest
                String insertSql = "INSERT INTO interests (keyword) VALUES (?)";
                psInsertInt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                psInsertInt.setString(1, interestKeyword);
                psInsertInt.executeUpdate();
                rs = psInsertInt.getGeneratedKeys();
                if (rs.next()) interestId = rs.getInt(1);
            }

            // Link to student
            String linkSql = "INSERT INTO student_interests (stu_id, interest_id) VALUES (?, ?)";
            psLink = conn.prepareStatement(linkSql);
            psLink.setInt(1, stuId);
            psLink.setInt(2, interestId);
            psLink.executeUpdate();
            
            return true;

        } catch (SQLException e) {
            // Ignore duplicate primary key errors (if student already has this interest)
            if(e.getErrorCode() == 1062) return true; 
            System.out.println("Error adding interest: " + e.getMessage());
            return false;
        }
    }

    //  Provide faculty name, building, office, email based on interest intersection
    public String searchFacultyByKeyword(String keyword) {
        StringBuilder sb = new StringBuilder();
        // Search both Interests AND Abstract content/titles [cite: 33]
        String sql = "SELECT DISTINCT f.fname, f.lname, f.email, f.building_no, f.officer_no " +
                     "FROM faculty f " +
                     "LEFT JOIN faculty_interest fi ON f.fac_id = fi.fac_id " +
                     "LEFT JOIN interests i ON fi.interest_id = i.interest_id " +
                     "LEFT JOIN faculty_abstracts fa ON f.fac_id = fa.prof_id " +
                     "LEFT JOIN abstract a ON fa.abs_id = a.abs_id " +
                     "WHERE i.keyword LIKE ? OR a.title LIKE ? OR a.abstract LIKE ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String query = "%" + keyword + "%";
            ps.setString(1, query);
            ps.setString(2, query);
            ps.setString(3, query);
            ResultSet rs = ps.executeQuery();

            sb.append(String.format("%-25s %-25s %-10s %-10s\n", "Faculty Name", "Email", "Bldg", "Office"));
            sb.append("--------------------------------------------------------------------------\n");

            boolean found = false;
            while (rs.next()) {
                found = true;
                String name = rs.getString("fname") + " " + rs.getString("lname");
                sb.append(String.format("%-25s %-25s %-10d %-10d\n", 
                    name, rs.getString("email"), rs.getInt("building_no"), rs.getInt("officer_no")));
            }
            if(!found) return "No faculty found for keyword: " + keyword;

        } catch (SQLException e) {
            return "Database Error: " + e.getMessage();
        }
        return sb.toString();
    }

    // ==========================================
    // GUEST / PUBLIC METHODS
    // ==========================================

    // [cite: 45] Outside business/library searching for student AND/OR faculty
    public String guestSearch(String keyword) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- FACULTY RESULTS ---\n");
        sb.append(searchFacultyByKeyword(keyword)); // Reuse logic
        sb.append("\n\n--- STUDENT RESULTS ---\n");
        sb.append(searchStudentsByInterest(keyword)); // Reuse logic
        return sb.toString();
    }
}