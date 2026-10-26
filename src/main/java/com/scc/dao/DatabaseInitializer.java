package com.scc.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        String users = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    user_role TEXT NOT NULL CHECK (user_role IN ('OWNER','COUNTER','MAINTENANCE')),
                    status TEXT NOT NULL DEFAULT 'PENDING'
                )""";

        String bicycles = """
                CREATE TABLE IF NOT EXISTS bicycles (
                    bike_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    brand_model TEXT NOT NULL,
                    bike_type TEXT NOT NULL,
                    hourly_rate REAL NOT NULL,
                    status TEXT NOT NULL DEFAULT 'AVAILABLE'
                        CHECK (status IN ('AVAILABLE','RENTED','UNDER_MAINTENANCE','RETIRED'))
                )""";

        String customers = """
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    contact_number TEXT NOT NULL,
                    id_proof_details TEXT,
                    date_registered TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )""";

        String rentals = """
                CREATE TABLE IF NOT EXISTS rentals (
                    rental_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER NOT NULL,
                    bike_id INTEGER NOT NULL,
                    user_id INTEGER NOT NULL,
                    borrow_time TEXT NOT NULL,
                    due_time TEXT NOT NULL,
                    return_time TEXT,
                    base_fee REAL NOT NULL DEFAULT 0,
                    penalty_fee REAL NOT NULL DEFAULT 0,
                    total_amount REAL NOT NULL DEFAULT 0,
                    payment_status TEXT NOT NULL DEFAULT 'UNPAID',
                    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
                    FOREIGN KEY (bike_id) REFERENCES bicycles(bike_id),
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                )""";

        String logs = """
                CREATE TABLE IF NOT EXISTS activity_logs (
                    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    action_type TEXT NOT NULL,
                    description TEXT,
                    timestamp TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                )""";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(users);
            stmt.execute(bicycles);
            stmt.execute(customers);
            stmt.execute(rentals);
            stmt.execute(logs);
            migratePasswordColumn(conn);
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    // Existing databases: rename the old password_hash column to password.
    private static void migratePasswordColumn(Connection conn) {
        boolean hasOldColumn = false;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(users)")) {
            while (rs.next()) {
                if ("password_hash".equalsIgnoreCase(rs.getString("name"))) {
                    hasOldColumn = true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking users table: " + e.getMessage());
            return;
        }

        if (hasOldColumn) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE users RENAME COLUMN password_hash TO password");
                System.out.println("--- Column password_hash renamed to password. "
                        + "Old hashed values must be reset to plain passwords.");
            } catch (SQLException e) {
                System.err.println("Error renaming column: " + e.getMessage());
            }
        }
    }
}