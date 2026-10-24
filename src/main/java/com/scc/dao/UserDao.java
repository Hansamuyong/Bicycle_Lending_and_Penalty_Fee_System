package com.scc.dao;

import com.scc.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class UserDao {

    public UserDao() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            defaultOwnerGenerate(conn);
        } catch (SQLException e) {
            System.err.println("Error seeding default owner: " + e.getMessage());
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public void defaultOwnerGenerate(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE user_role = 'OWNER'";
        String insertSql = "INSERT INTO users(first_name, last_name, username, password_hash, user_role, status) "
                + "VALUES(?, ?, ?, ?, 'OWNER', 'APPROVED')";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, "Shop");
                    pstmt.setString(2, "Owner");
                    pstmt.setString(3, "owner");
                    pstmt.setString(4, hashPassword("owner123"));
                    pstmt.executeUpdate();
                    System.out.println("--- Default Owner Created (Username: owner | Password: owner123)");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default owner: " + e.getMessage());
        }
    }

    public boolean registerUser(String firstName, String lastName, String username, String password, String role) {
        String sql = "INSERT INTO users(first_name, last_name, username, password_hash, user_role, status) "
                + "VALUES (?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, username);
            pstmt.setString(4, hashPassword(password));
            pstmt.setString(5, role.toUpperCase());
            pstmt.executeUpdate();
            System.out.println("---- Registration successful. Please wait for the Shop Owner's approval.");
            return true;
        } catch (SQLException e) {
            System.err.println("Failed: " + e.getMessage());
            return false;
        }
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT username FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("\nDatabase error: " + e.getMessage());
            return true;
        }
    }

    public User login(String username, String password) {
        String sql = "SELECT user_id, first_name, last_name, username, password_hash, user_role, status "
                + "FROM users WHERE username = ? AND password_hash = ?";

        User user = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println(" -> Account is pending. Waiting for approval.");
                        return null;
                    }
                    if ("DEACTIVATED".equalsIgnoreCase(status)) {
                        System.out.println(" -> Account has been deactivated.");
                        return null;
                    }
                    user = new User(
                            rs.getInt("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("user_role"),
                            status
                    );
                } else {
                    System.out.println(" -> Invalid username or password");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during login " + e.getMessage());
        }

        if (user != null) {
            logActivity(user.user_id(), "LOGIN", user.username() + " logged in");
        }
        return user;
    }

    public void logActivity(int userId, String actionType, String description) {
        String sql = "INSERT INTO activity_logs(user_id, action_type, description) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, actionType);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error writing activity log: " + e.getMessage());
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, first_name, last_name, username, password_hash, user_role, status "
                + "FROM users ORDER BY user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    public List<User> getPendingUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, first_name, last_name, username, password_hash, user_role, status "
                + "FROM users WHERE status = 'PENDING' ORDER BY user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending users: " + e.getMessage());
        }
        return users;
    }

    public boolean approveUser(int userId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE user_id = ? AND status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
            return false;
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("user_role"),
                rs.getString("status")
        );
    }
}