package com.scc.dao;

import com.scc.model.Bicycle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BicycleDao {

    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    // CORE BUSINESS PROCESS: register a new bicycle in the fleet (logs BICYCLE_ADDED).
    public boolean addBicycle(int staffId, String brandModel, String bikeType, double hourlyRate) {
        String sql = "INSERT INTO bicycles(brand_model, bike_type, hourly_rate, status) "
                + "VALUES (?, ?, ?, 'AVAILABLE')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, brandModel);
            pstmt.setString(2, bikeType);
            pstmt.setDouble(3, hourlyRate);
            pstmt.executeUpdate();

            int bikeId = -1;
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    bikeId = keys.getInt(1);
                }
            }

            activityLogDao.logAction(staffId, "BICYCLE_ADDED",
                    String.format("Staff [%d] added Bike #%d %s (%s) at P%.2f/hr",
                            staffId, bikeId, brandModel, bikeType, hourlyRate));
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding bicycle: " + e.getMessage());
            return false;
        }
    }

    public List<Bicycle> getAllBicycles() {
        String sql = "SELECT bike_id, brand_model, bike_type, hourly_rate, status "
                + "FROM bicycles ORDER BY bike_id";
        List<Bicycle> bikes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                bikes.add(new Bicycle(
                        rs.getInt("bike_id"),
                        rs.getString("brand_model"),
                        rs.getString("bike_type"),
                        rs.getDouble("hourly_rate"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bicycles: " + e.getMessage());
        }
        return bikes;
    }

    public boolean updateBikeStatus(int staffId, int bikeId, String newStatus) {
        String selectSql = "SELECT status FROM bicycles WHERE bike_id = ?";
        String updateSql = "UPDATE bicycles SET status = ? WHERE bike_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            String oldStatus;
            try (PreparedStatement select = conn.prepareStatement(selectSql)) {
                select.setInt(1, bikeId);
                try (ResultSet rs = select.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    oldStatus = rs.getString("status");
                }
            }

            try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                update.setString(1, newStatus.toUpperCase());
                update.setInt(2, bikeId);
                if (update.executeUpdate() == 0) {
                    return false;
                }
            }

            activityLogDao.logAction(staffId, "BIKE_STATUS_UPDATE",
                    "Updated Bike #" + bikeId + " status from " + oldStatus
                            + " to " + newStatus.toUpperCase());
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating bike status: " + e.getMessage());
            return false;
        }
    }
}