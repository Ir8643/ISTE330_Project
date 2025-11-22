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
// package ISTE330_Project;

import java.sql.*;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataLayer {

    private Connection conn;

    public boolean connect() {
        if (conn != null) {
            return true;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost/Abstracts?serverTimezone=UTC";
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
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes("UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (byte b : digest)
                sb.append(String.format("%02x", b));
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

            if (!rs.next())
                return -1;

            String storedHash = rs.getString("password");
            String entered = hashPassword(password);

            if (entered == null || !storedHash.equalsIgnoreCase(entered))
                return -2;

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

            if (!rs.next())
                return -1;

            String storedHash = rs.getString("password");
            String entered = hashPassword(password);

            if (entered == null || !storedHash.equalsIgnoreCase(entered))
                return -2;

            return rs.getInt("stu_id");

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
            return -3;
        }
    }

    // Guest login
    public int loginGuest(String username, String password) {
        String sql = "SELECT a.account_id, a.password, g.guest_id " +
                "FROM account a JOIN guest g ON a.account_id = g.account_id " +
                "WHERE a.username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                return -1;

            String storedHash = rs.getString("password");
            String entered = hashPassword(password);

            if (entered == null || !storedHash.equalsIgnoreCase(entered))
                return -2;

            return rs.getInt("guest_id");

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
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException se) {
            }
            return 0;
        } finally {
            try {
                if (keys != null)
                    keys.close();
                if (psAbs != null)
                    psAbs.close();
                if (psLink != null)
                    psLink.close();
            } catch (SQLException e) {
            }
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
            while (rs.next()) {
                found = true;
                sb.append("ID: ").append(rs.getInt("abs_id")).append("\n")
                        .append("Title: ").append(rs.getString("title")).append("\n")
                        .append("Authors: ").append(rs.getString("authors")).append("\n")
                        .append("Abstract: ").append(rs.getString("abstract")).append("\n")
                        .append("----------------------------------------\n");
            }
            if (!found)
                return "You have no uploaded abstracts.";

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
            if (!rs.next()) {
                System.out.println("You do not own this abstract ID.");
                return false;
            }
        } catch (SQLException e) {
            return false;
        }

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
     * Manage Faculty Interests
     */
    public String getFacultyInterests(int facId) {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT i.keyword FROM interests i " +
                "JOIN faculty_interest fi ON i.interest_id = fi.interest_id " +
                "WHERE fi.fac_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facId);
            ResultSet rs = ps.executeQuery();
            sb.append("Your Interests: ");
            while (rs.next()) {
                sb.append(rs.getString("keyword")).append(", ");
            }
            // Remove trailing comma
            if (sb.length() > 16)
                sb.setLength(sb.length() - 2);
            else
                sb.append("None");

        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
        return sb.toString();
    }

    public boolean addFacultyInterest(int facId, String keyword) {
        return addInterestGeneric(facId, keyword, true); // true = faculty
    }

    public boolean removeFacultyInterest(int facId, String keyword) {
        return removeInterestGeneric(facId, keyword, true);
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

            sb.append(String.format("%-20s %-30s %-20s %-15s\n", "Name", "Email", "Program", "Interest"));
            sb.append("-------------------------------------------------------------------------------\n");

            boolean found = false;
            while (rs.next()) {
                found = true;
                String name = rs.getString("fname") + " " + rs.getString("lname");
                sb.append(String.format("%-20s %-30s %-20s %-15s\n",
                        name, rs.getString("email"), rs.getString("program"), rs.getString("keyword")));
            }
            if (!found)
                return "No students found with interest: " + keyword;

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
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stuId);
            ResultSet rs = ps.executeQuery();
            sb.append("Your Interests: ");
            while (rs.next()) {
                sb.append(rs.getString("keyword")).append(", ");
            }
            if (sb.length() > 16)
                sb.setLength(sb.length() - 2);
            else
                sb.append("None");
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
        return sb.toString();
    }

    public boolean removeStudentInterest(int stuId, String keyword) {
        return removeInterestGeneric(stuId, keyword, false);
    }

    // ==========================================
    // SHARED / HELPER METHODS (INTERESTS)
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
                if (rs.next())
                    interestId = rs.getInt(1);
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
            if (e.getErrorCode() == 1062)
                return true; // Duplicate ignored
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
            if (!rs.next())
                return false; // Interest doesn't exist
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

    // ==========================================
    // SEARCH / INTERSECTION METHODS
    // ==========================================

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

                String matchInterest = rs.getString("keyword");
                String matchTitle = rs.getString("title");
                String matchAbs = rs.getString("abstract");

                if (matchInterest != null && matchInterest.toLowerCase().contains(keyword.toLowerCase()))
                    sb.append("Interest Match: ").append(matchInterest).append("\n");

                if (matchTitle != null)
                    sb.append("Abstract Title: ").append(matchTitle).append("\n");

                if (matchAbs != null && matchAbs.length() > 100)
                    sb.append("Abstract Preview: ").append(matchAbs.substring(0, 100)).append("...\n");
                else if (matchAbs != null)
                    sb.append("Abstract: ").append(matchAbs).append("\n");

                sb.append("------------------------------------------------------------\n");
            }

            if (!found)
                return "No results found for: " + keyword;

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

    // EXPANDED guest search: supports multiple keywords separated by spaces or
    // commas
    public String guestSearch(String input) {
        StringBuilder sb = new StringBuilder();

        if (input == null || input.trim().isEmpty()) {
            return "Please enter at least one keyword.";
        }

        String[] tokens = input.split("[,\\s]+"); // split on commas or whitespace
        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty())
                continue;

            sb.append("===== RESULTS FOR \"").append(token).append("\" =====\n");
            sb.append("--- FACULTY RESULTS ---\n");
            sb.append(searchFacultyMaster(token)).append("\n\n");
            sb.append("--- STUDENT RESULTS ---\n");
            sb.append(searchStudentsByInterest(token)).append("\n\n");
        }

        return sb.toString();
    }

    // ==========================================
    // ACCOUNT CREATION / LOOKUP METHODS
    // ==========================================

    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM account WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error checking username: " + e.getMessage());
            return false;
        }
    }

    public Map<Integer, String> getColleges() {
        Map<Integer, String> map = new LinkedHashMap<>();
        String sql = "SELECT college_id, name FROM college ORDER BY college_id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt("college_id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading colleges: " + e.getMessage());
        }
        return map;
    }

    public Map<Integer, String> getMajors() {
        Map<Integer, String> map = new LinkedHashMap<>();
        String sql = "SELECT major_id, name FROM major ORDER BY major_id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt("major_id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading majors: " + e.getMessage());
        }
        return map;
    }

    public int createFacultyAccount(String username,
            String password,
            String fname,
            String lname,
            int buildingNo,
            int officeNo,
            String email,
            int collegeId) {

        PreparedStatement psAcc = null;
        PreparedStatement psFac = null;
        ResultSet keys = null;

        try {
            conn.setAutoCommit(false);

            // 1) Insert into account table, hashing in MySQL using SHA2
            String sqlAcc = "INSERT INTO account (username, password) VALUES (?, SHA2(?, 256))";
            psAcc = conn.prepareStatement(sqlAcc, Statement.RETURN_GENERATED_KEYS);
            psAcc.setString(1, username);
            psAcc.setString(2, password);
            psAcc.executeUpdate();

            keys = psAcc.getGeneratedKeys();
            if (!keys.next()) {
                conn.rollback();
                return -1;
            }
            int accountId = keys.getInt(1);

            // 2) Insert into faculty table
            String sqlFac = "INSERT INTO faculty " +
                    "(fname, lname, building_no, officer_no, email, account_id, college_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            psFac = conn.prepareStatement(sqlFac);
            psFac.setString(1, fname);
            psFac.setString(2, lname);
            psFac.setInt(3, buildingNo);
            psFac.setInt(4, officeNo);
            psFac.setString(5, email);
            psFac.setInt(6, accountId);
            psFac.setInt(7, collegeId);
            psFac.executeUpdate();

            conn.commit();
            return accountId;

        } catch (SQLException e) {
            System.out.println("Error creating faculty account: " + e.getMessage());
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException se) {
            }
            return -1;
        } finally {
            try {
                if (keys != null)
                    keys.close();
                if (psAcc != null)
                    psAcc.close();
                if (psFac != null)
                    psFac.close();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
            }
        }
    }

    public int createStudentAccount(String username,
            String password,
            String fname,
            String lname,
            String email,
            String program,
            int majorId) {

        PreparedStatement psAcc = null;
        PreparedStatement psStu = null;
        ResultSet keys = null;

        try {
            conn.setAutoCommit(false);

            // 1) Insert into account (SHA2 at DB level)
            String sqlAcc = "INSERT INTO account (username, password) VALUES (?, SHA2(?, 256))";
            psAcc = conn.prepareStatement(sqlAcc, Statement.RETURN_GENERATED_KEYS);
            psAcc.setString(1, username);
            psAcc.setString(2, password);
            psAcc.executeUpdate();

            keys = psAcc.getGeneratedKeys();
            if (!keys.next()) {
                conn.rollback();
                return -1;
            }
            int accountId = keys.getInt(1);

            // 2) Insert into student
            String sqlStu = "INSERT INTO student " +
                    "(fname, lname, email, program, major_id, account_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            psStu = conn.prepareStatement(sqlStu);
            psStu.setString(1, fname);
            psStu.setString(2, lname);
            psStu.setString(3, email);
            psStu.setString(4, program);
            psStu.setInt(5, majorId);
            psStu.setInt(6, accountId);
            psStu.executeUpdate();

            conn.commit();
            return accountId;

        } catch (SQLException e) {
            System.out.println("Error creating student account: " + e.getMessage());
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException se) {
            }
            return -1;
        } finally {
            try {
                if (keys != null)
                    keys.close();
                if (psAcc != null)
                    psAcc.close();
                if (psStu != null)
                    psStu.close();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
            }
        }
    }

    public int createGuestAccount(String username,
            String password,
            String fname,
            String lname,
            String email) {

        PreparedStatement psAcc = null;
        PreparedStatement psGuest = null;
        ResultSet keys = null;

        try {
            conn.setAutoCommit(false);

            // 1) Insert into account
            String sqlAcc = "INSERT INTO account (username, password) VALUES (?, SHA2(?, 256))";
            psAcc = conn.prepareStatement(sqlAcc, Statement.RETURN_GENERATED_KEYS);
            psAcc.setString(1, username);
            psAcc.setString(2, password);
            psAcc.executeUpdate();

            keys = psAcc.getGeneratedKeys();
            if (!keys.next()) {
                conn.rollback();
                return -1;
            }
            int accountId = keys.getInt(1);

            // 2) Insert into guest
            String sqlGuest = "INSERT INTO guest (fname, lname, email, account_id) " +
                    "VALUES (?, ?, ?, ?)";
            psGuest = conn.prepareStatement(sqlGuest);
            psGuest.setString(1, fname);
            psGuest.setString(2, lname);
            psGuest.setString(3, email);
            psGuest.setInt(4, accountId);
            psGuest.executeUpdate();

            conn.commit();
            return accountId;

        } catch (SQLException e) {
            System.out.println("Error creating guest account: " + e.getMessage());
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException se) {
            }
            return -1;
        } finally {
            try {
                if (keys != null)
                    keys.close();
                if (psAcc != null)
                    psAcc.close();
                if (psGuest != null)
                    psGuest.close();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
            }
        }
    }
}