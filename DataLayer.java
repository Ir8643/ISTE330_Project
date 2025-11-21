// Group 3
// ISTE 330 - Database Management Systems
// RIT Research Database System
// Team Members:
//  - Innocenzio Rizzuto
//  - Sanjay Charitesh Makam
//  - Joseph McEnroe
//  - Mohamed Abdullah Najumudeen
//  - Jake Paczkowski
//  - Muzammilkhan Pathan
package ISTE330_Project;

import java.sql.*;
import java.security.MessageDigest;

public class DataLayer {
     
    private Connection conn;

    public boolean connect() {
        if (conn != null) {
            return true;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url  = "jdbc:mysql://localhost/Abstracts?serverTimezone=UTC";
            String user = "root";
            String pass = "student"; 

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

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes("UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();

        } catch (Exception e) {
            return null;
        }
    }

    public int loginFaculty(String username, String password) {
        String sql = "SELECT a.account_id, a.password, f.fac_id " +
                     "FROM account a JOIN faculty f ON a.account_id = f.account_id " +
                     "WHERE a.username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return -1;

            String storedHash = rs.getString("password");
            String entered = hashPassword(password);

            if (!storedHash.equals(entered)) return -2; 

            return rs.getInt("fac_id"); 

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
            return -3;
        }
    }

    public int loginStudent(String username, String password) {
        String sql = "SELECT a.account_id, a.password, s.stu_id " +
                     "FROM account a JOIN student s ON a.account_id = s.account_id " +
                     "WHERE a.username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return -1;

            String storedHash = rs.getString("password");
            String entered = hashPassword(password);

            if (!storedHash.equals(entered)) return -2; 

            return rs.getInt("stu_id"); 

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
            return -3;
        }
    }


    // ==========================================
    // FACULTY METHODS
    // ==========================================

    public int insertAbstractFromFile(int facId, String title, String authors, String abstracts) {
        PreparedStatement psAbs = null;
        PreparedStatement psLink = null;
        ResultSet keys = null;
        int absId;

        try {
            String insertAbstractSql = "INSERT INTO abstract (title, `abstract`, authors) VALUES (?, ?, ?)";

            conn.setAutoCommit(false); 

            psAbs = conn.prepareStatement(insertAbstractSql, Statement.RETURN_GENERATED_KEYS);
            psAbs.setString(1, title);
            psAbs.setString(2, abstracts);
            psAbs.setString(3, authors);

            int rows = psAbs.executeUpdate();
            if (rows == 0) {
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

    public String getFacultyOwnAbstracts(int facId) {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT a.abs_id, a.title, a.abstract, a.authors " +
                     "FROM abstract a " +
                     "JOIN faculty_abstracts fa ON a.abs_id = fa.abs_id " +
                     "WHERE fa.prof_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facId);
            ResultSet rs = ps.executeQuery();
            
            boolean found = false;
            while(rs.next()) {
                found = true;
                sb.append("ID: ").append(rs.getInt("abs_id")).append("\n")
                  .append("Title: ").append(rs.getString("title")).append("\n")
                  .append("Authors: ").append(rs.getString("authors")).append("\n")
                  .append("Abstract: ").append(rs.getString("abstract")).append("\n")
                  .append("----------------------------------------\n");
            }
            if(!found) return "You have no uploaded abstracts.";
            
        } catch (SQLException e) {
            return "DB Error: " + e.getMessage();
        }
        return sb.toString();
    }
    public boolean updateAbstract(int facId, int absId, String title, String authors, String content) {
        // First check ownership
        String checkSql = "SELECT 1 FROM faculty_abstracts WHERE prof_id = ? AND abs_id = ?";
        try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            psCheck.setInt(1, facId);
            psCheck.setInt(2, absId);
            ResultSet rs = psCheck.executeQuery();
            if(!rs.next()) {
                System.out.println("You do not own this abstract ID.");
                return false;
            }
        } catch (SQLException e) { return false; }

        // Update
        String updateSql = "UPDATE abstract SET title = ?, authors = ?, abstract = ? WHERE abs_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, title);
            ps.setString(2, authors);
            ps.setString(3, content);
            ps.setInt(4, absId);
            
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * NEW: Manage Faculty Interests
     */
    public String getFacultyInterests(int facId) {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT i.keyword FROM interests i " +
                     "JOIN faculty_interest fi ON i.interest_id = fi.interest_id " +
                     "WHERE fi.fac_id = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facId);
            ResultSet rs = ps.executeQuery();
            sb.append("Your Interests: ");
            while(rs.next()) {
                sb.append(rs.getString("keyword")).append(", ");
            }
            // Remove trailing comma
            if(sb.length() > 16) sb.setLength(sb.length()-2); 
            else sb.append("None");
            
        } catch(SQLException e) { return "Error: " + e.getMessage(); }
        return sb.toString();
    }

    public boolean addFacultyInterest(int facId, String keyword) {
        return addInterestGeneric(facId, keyword, true); // true = faculty
    }

    public boolean removeFacultyInterest(int facId, String keyword) {
        return removeInterestGeneric(facId, keyword, true);
    }

    public String autoMatchStudentsForFaculty(int facId) {
    String sql =
        "SELECT DISTINCT s.fname, s.lname, s.email, s.program, i.keyword " +
        "FROM faculty_interest fi " +
        "JOIN interests i ON fi.interest_id = i.interest_id " +
        "JOIN student_interests si ON si.interest_id = i.interest_id " +
        "JOIN student s ON s.stu_id = si.stu_id " +
        "WHERE fi.fac_id = ?";

    StringBuilder sb = new StringBuilder();
    sb.append("=== Students Matching Your Interests ===\n");

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, facId);
        ResultSet rs = ps.executeQuery();

        boolean found = false;
        while (rs.next()) {
            found = true;

            sb.append(rs.getString("fname")).append(" ").append(rs.getString("lname")).append("\n")
              .append("Email: ").append(rs.getString("email")).append("\n")
              .append("Program: ").append(rs.getString("program")).append("\n")
              .append("Matched Interest: ").append(rs.getString("keyword")).append("\n")
              .append("-------------------------------------------\n");
        }

        if (!found) return "No matching students found.";

    } catch (SQLException e) {
        return "DB Error: " + e.getMessage();
    }

    return sb.toString();
}


    // ==========================================
    // STUDENT METHODS
    // ==========================================

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

    public boolean insertStudentInterest(int stuId, String interestKeyword) {
        return addInterestGeneric(stuId, interestKeyword, false); // false = student
    }
    public String getStudentInterests(int stuId) {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT i.keyword FROM interests i " +
                     "JOIN student_interests si ON i.interest_id = si.interest_id " +
                     "WHERE si.stu_id = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stuId);
            ResultSet rs = ps.executeQuery();
            sb.append("Your Interests: ");
            while(rs.next()) {
                sb.append(rs.getString("keyword")).append(", ");
            }
            if(sb.length() > 16) sb.setLength(sb.length()-2);
            else sb.append("None");
        } catch(SQLException e) { return "Error: " + e.getMessage(); }
        return sb.toString();
    }

    public boolean removeStudentInterest(int stuId, String keyword) {
        return removeInterestGeneric(stuId, keyword, false);
    }

    public String autoMatchFacultyForStudent(int stuId) {
    String sql = 
        "SELECT DISTINCT f.fname, f.lname, f.email, f.building_no, f.officer_no, i.keyword " +
        "FROM student_interests si " +
        "JOIN interests i ON si.interest_id = i.interest_id " +
        "JOIN faculty_interest fi ON fi.interest_id = i.interest_id " +
        "JOIN faculty f ON f.fac_id = fi.fac_id " +
        "WHERE si.stu_id = ?";

    StringBuilder sb = new StringBuilder();
    sb.append("=== Faculty Matching Your Interests ===\n");

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, stuId);
        ResultSet rs = ps.executeQuery();

        boolean found = false;
        while (rs.next()) {
            found = true;
            sb.append(rs.getString("fname")).append(" ").append(rs.getString("lname")).append("\n")
              .append("Email: ").append(rs.getString("email")).append("\n")
              .append("Building: ").append(rs.getInt("building_no"))
              .append("  Office: ").append(rs.getInt("officer_no")).append("\n")
              .append("Matched Interest: ").append(rs.getString("keyword")).append("\n")
              .append("-------------------------------------------\n");
        }

        if (!found) return "No matching faculty found.";

    } catch (SQLException e) {
        return "DB Error: " + e.getMessage();
    }

    return sb.toString();
}

    // ==========================================
    // SHARED / HELPER METHODS
    // ==========================================

    private boolean addInterestGeneric(int userId, String keyword, boolean isFaculty) {
        PreparedStatement psCheck = null;
        PreparedStatement psInsertInt = null;
        PreparedStatement psLink = null;
        ResultSet rs = null;
        int interestId = -1;

        try {
            // 1. Find or Create Interest
            String checkSql = "SELECT interest_id FROM interests WHERE keyword = ?";
            psCheck = conn.prepareStatement(checkSql);
            psCheck.setString(1, keyword);
            rs = psCheck.executeQuery();

            if (rs.next()) {
                interestId = rs.getInt("interest_id");
            } else {
                String insertSql = "INSERT INTO interests (keyword) VALUES (?)";
                psInsertInt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                psInsertInt.setString(1, keyword);
                psInsertInt.executeUpdate();
                rs = psInsertInt.getGeneratedKeys();
                if (rs.next()) interestId = rs.getInt(1);
            }

            // 2. Link to User
            String table = isFaculty ? "faculty_interest" : "student_interests";
            String idCol = isFaculty ? "fac_id" : "stu_id";
            
            String linkSql = "INSERT INTO " + table + " (" + idCol + ", interest_id) VALUES (?, ?)";
            psLink = conn.prepareStatement(linkSql);
            psLink.setInt(1, userId);
            psLink.setInt(2, interestId);
            psLink.executeUpdate();
            return true;

        } catch (SQLException e) {
            if(e.getErrorCode() == 1062) return true; // Duplicate ignored
            System.out.println("Error adding interest: " + e.getMessage());
            return false;
        }
    }

    private boolean removeInterestGeneric(int userId, String keyword, boolean isFaculty) {
        try {
            // Get interest ID first
            String getIntId = "SELECT interest_id FROM interests WHERE keyword = ?";
            PreparedStatement ps = conn.prepareStatement(getIntId);
            ps.setString(1, keyword);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()) return false; // Interest doesn't exist
            int interestId = rs.getInt(1);

            // Delete Link
            String table = isFaculty ? "faculty_interest" : "student_interests";
            String idCol = isFaculty ? "fac_id" : "stu_id";

            String delSql = "DELETE FROM " + table + " WHERE " + idCol + " = ? AND interest_id = ?";
            PreparedStatement psDel = conn.prepareStatement(delSql);
            psDel.setInt(1, userId);
            psDel.setInt(2, interestId);
            return psDel.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error removing interest: " + e.getMessage());
            return false;
        }
    }

    public String searchFacultyMaster(String keyword) {
        StringBuilder sb = new StringBuilder();
        // Matches logic: "search on the accounts interests, abstracts, and keyword"
        String sql = "SELECT DISTINCT f.fname, f.lname, f.email, f.building_no, f.officer_no, " +
                     "a.title, a.abstract, i.keyword " +
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

            boolean found = false;
            while (rs.next()) {
                found = true;
                String name = rs.getString("fname") + " " + rs.getString("lname");
                
                sb.append("Faculty:  ").append(name).append("\n");
                sb.append("Email:    ").append(rs.getString("email")).append("\n");
                sb.append("Location: Bldg ").append(rs.getInt("building_no"))
                  .append(" Rm ").append(rs.getInt("officer_no")).append("\n");
                
                // Because of the LEFT JOINS, fields might be null if the match was on the other side
                String matchInterest = rs.getString("keyword");
                String matchTitle = rs.getString("title");
                String matchAbs = rs.getString("abstract");

                if(matchInterest != null && matchInterest.toLowerCase().contains(keyword.toLowerCase()))
                     sb.append("Interest Match: ").append(matchInterest).append("\n");
                
                if(matchTitle != null) 
                     sb.append("Abstract Title: ").append(matchTitle).append("\n");
                
                if(matchAbs != null && matchAbs.length() > 100)
                    sb.append("Abstract Preview: ").append(matchAbs.substring(0, 100)).append("...\n");
                else if (matchAbs != null)
                    sb.append("Abstract: ").append(matchAbs).append("\n");
                    
                sb.append("------------------------------------------------------------\n");
            }

            if(!found) return "No results found for: " + keyword;

        } catch (SQLException e) {
            return "Database Error: " + e.getMessage();
        }
        return sb.toString();
    }

    public String searchFacultyByKeyword(String keyword) {
        return searchFacultyMaster(keyword);
    }

    public String searchFacultyByAbstract(String keyword) {
        return searchFacultyMaster(keyword);
    }
    
    public String guestSearch(String keyword) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- FACULTY RESULTS ---\n");
        sb.append(searchFacultyMaster(keyword)); 
        sb.append("\n\n--- STUDENT RESULTS ---\n");
        sb.append(searchStudentsByInterest(keyword)); 
        return sb.toString();
    }
    public String autoMatchGuest() {
    StringBuilder sb = new StringBuilder();
    sb.append("=== FACULTY MATCHES ===\n");
    sb.append(searchFacultyMaster("")); 
    sb.append("\n\n=== STUDENT MATCHES ===\n");
    sb.append(searchStudentsByInterest(""));
    return sb.toString();
}

}