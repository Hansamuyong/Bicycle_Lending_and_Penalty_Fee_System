package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class OwnerMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public OwnerMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User owner) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("========== SHOP OWNER MENU ==========");
            System.out.println("[1] Manage Staff Accounts (Approve / Deactivate)");
            System.out.println("[2] Manage Bicycles");
            System.out.println("[3] View Bike Availability, Status and Rates");
            System.out.println("[4] Update Rental / Penalty Rates");
            System.out.println("[5] Manage Customers");
            System.out.println("[6] Cancel Invalid Rental Transactions");
            System.out.println("[7] Daily Rental & Penalty Fee Summary Report");
            System.out.println("[8] View User Activity & System Audit Log");
            System.out.println("[0] Logout");
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                case "2":
                case "3":
                case "4":
                case "5":
                case "6":
                case "7":
                case "8":
                    comingSoon();
                    break;
                case "0":
                    userDao.logActivity(owner.user_id(), "LOGOUT", owner.username() + " logged out");
                    System.out.println("Logging out... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 0 to 8.");
            }
        }
    }

    private void comingSoon() {
        System.out.println(">> Coming soon.");
    }
}