package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // Inserts a new audit log entry (local time so it is easy to read).
    public boolean logAction(int userId, String actionType, String description) {
        String sql = "INSERT INTO activity_logs(user_id, action_type, description, timestamp) "
                + "VALUES (?, ?, ?, datetime('now', 'localtime'))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, actionType);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error writing activity log: " + e.getMessage());
            return false;
        }
    }

    // All logs, newest first.
    public List<ActivityLog> getAllLogs() {
        String sql = "SELECT log_id, user_id, action_type, description, timestamp "
                + "FROM activity_logs ORDER BY timestamp DESC, log_id DESC";
        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                logs.add(mapRowToLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching logs: " + e.getMessage());
        }
        return logs;
    }

    // Logs of one user, newest first.
    public List<ActivityLog> getLogsByUserId(int userId) {
        String sql = "SELECT log_id, user_id, action_type, description, timestamp "
                + "FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC, log_id DESC";
        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching logs for user: " + e.getMessage());
        }
        return logs;
    }

    // Deletes all logs. Returns how many rows were removed, or -1 on error.
    public int clearAllLogs() {
        String sql = "DELETE FROM activity_logs";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error clearing logs: " + e.getMessage());
            return -1;
        }
    }

    private ActivityLog mapRowToLog(ResultSet rs) throws SQLException {
        return new ActivityLog(
                rs.getInt("log_id"),
                rs.getInt("user_id"),
                rs.getString("action_type"),
                rs.getString("description"),
                rs.getString("timestamp")
        );
    }
}