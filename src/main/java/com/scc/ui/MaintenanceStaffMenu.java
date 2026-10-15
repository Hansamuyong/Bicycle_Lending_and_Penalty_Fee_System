package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class MaintenanceStaffMenu {
    private final UserDao userDao;
    private final Scanner scanner;

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
                case "2":
                case "3":
                case "4":
                case "5":
                    comingSoon();
                    break;
                case "0":
                    userDao.logActivity(staff.user_id(), "LOGOUT", staff.username() + " logged out");
                    System.out.println("Logging out... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 0 to 5.");
            }
        }
    }

    private void comingSoon() {
        System.out.println(">> Coming soon.");
    }
}