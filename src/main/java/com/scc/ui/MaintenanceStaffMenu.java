package com.scc.ui;

import com.scc.dao.BicycleDao;
import com.scc.dao.UserDao;
import com.scc.model.Bicycle;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class MaintenanceStaffMenu {
    private final UserDao userDao;
    private final Scanner scanner;
    private final BicycleDao bicycleDao = new BicycleDao();

    public MaintenanceStaffMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User staff) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("========== MAINTENANCE STAFF MENU ==========");
            System.out.println("[1] Add New Bicycle");
            System.out.println("[2] View Bike Availability, Status and Rates");
            System.out.println("[3] Update Bike Status (Available / Under Maintenance)");
            System.out.println("[4] Log Repair Notes");
            System.out.println("[5] Retire Broken Bicycle (Soft Delete)");
            System.out.println("[0] Logout");
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    comingSoon();   // addBicycle(staff);
                    break;
                case "2":
                    System.out.println("\nBICYCLE FLEET");
                    printBikeTable(bicycleDao.getAllBicycles());
                    break;
                case "3":
                    comingSoon();   // updateBikeStatus(staff);
                    break;
                case "4":
                case "5":
                    comingSoon();
                    break;
                case "0":
                    System.out.println("Logging out... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 0 to 5.");
            }
        }
    }

    // ---------------------------------------------------------------
    // CORE BUSINESS PROCESS: add a bicycle (logged as BICYCLE_ADDED)
    // ---------------------------------------------------------------
    private void addBicycle(User staff) {
        System.out.println("\nADD NEW BICYCLE (enter 0 to cancel)");

        String brandModel = readRequired("Brand / model: ");
        if (brandModel == null) { System.out.println("Add bicycle cancelled."); return; }

        String bikeType = readRequired("Bike type (e.g. Mountain, Road, City): ");
        if (bikeType == null) { System.out.println("Add bicycle cancelled."); return; }

        double rate = readRate();
        if (rate == 0) { System.out.println("Add bicycle cancelled."); return; }

        if (bicycleDao.addBicycle(staff.user_id(), brandModel, bikeType, rate)) {
            System.out.println("Success! " + brandModel + " added at P" + String.format("%.2f", rate) + "/hr.");
        } else {
            System.out.println("Failed to add bicycle. Please try again.");
        }
    }

    // ---------------------------------------------------------------
    // LIFECYCLE / STATE CHANGE: update status (logged as BIKE_STATUS_UPDATED)
    // ---------------------------------------------------------------
    private void updateBikeStatus(User staff) {
        System.out.println("\nUPDATE BIKE STATUS");
        List<Bicycle> bikes = bicycleDao.getAllBicycles();
        printBikeTable(bikes);
        if (bikes.isEmpty()) {
            return;
        }

        while (true) {
            int bikeId = readIntInput("Enter Bike ID to update (0 to cancel): ");
            if (bikeId == 0) {
                System.out.println("Update cancelled.");
                return;
            }

            Bicycle target = null;
            for (Bicycle b : bikes) {
                if (b.bike_id() == bikeId) target = b;
            }
            if (target == null) {
                System.out.println("Bike ID " + bikeId + " does not exist. Try again.");
                continue;
            }
            if (target.status().equals("RENTED") || target.status().equals("RETIRED")) {
                System.out.println("Bike #" + bikeId + " is " + target.status()
                        + " and its status cannot be changed here.");
                continue;
            }

            String newStatus = readBikeStatus();
            if (newStatus == null) {
                System.out.println("Update cancelled.");
                return;
            }

            if (bicycleDao.updateBikeStatus(staff.user_id(), bikeId, newStatus)) {
                System.out.println("Success! Bike #" + bikeId + " is now " + newStatus + ".");
            } else {
                System.out.println("Update failed. Please try again.");
            }
            return;
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Returns null when 0 is entered.
    private String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (value.equals("0")) return null;
            if (!value.isEmpty()) return value;
            System.out.println("This field is required.");
        }
    }

    // Returns 0 when cancelled; otherwise a positive hourly rate.
    private double readRate() {
        while (true) {
            System.out.print("Hourly rate in pesos (0 to cancel): ");
            String input = scanner.nextLine().trim();
            try {
                double rate = Double.parseDouble(input);
                if (rate == 0) return 0;
                if (rate > 0) return rate;
                System.out.println("Rate must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Returns null when 0 is entered.
    private String readBikeStatus() {
        while (true) {
            System.out.print("New status - [1] AVAILABLE  [2] UNDER_MAINTENANCE  [0] Cancel: ");
            switch (scanner.nextLine().trim()) {
                case "1": return "AVAILABLE";
                case "2": return "UNDER_MAINTENANCE";
                case "0": return null;
                default: System.out.println("Invalid choice. Please enter 0, 1 or 2.");
            }
        }
    }

    private void printBikeTable(List<Bicycle> bikes) {
        if (bikes == null || bikes.isEmpty()) {
            System.out.println("No bicycles found.");
            return;
        }

        String format = "| %-5s | %-20s | %-10s | %-10s | %-17s |%n";
        String border = "+-------+----------------------+------------+------------+-------------------+";

        System.out.println(border);
        System.out.printf(format, "ID", "Brand / Model", "Type", "Rate/hr", "Status");
        System.out.println(border);
        for (Bicycle b : bikes) {
            System.out.printf(format,
                    String.valueOf(b.bike_id()),
                    truncate(b.brand_model(), 20),
                    truncate(b.bike_type(), 10),
                    String.format("P%.2f", b.hourly_rate()),
                    b.status());
        }
        System.out.println(border);
    }

    private String truncate(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max - 1) + "~";
    }

    private void comingSoon() {
        System.out.println(">> Coming soon.");
    }
}