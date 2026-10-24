package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
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
                    manageUsers(owner);
                    break;
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

    private void manageUsers(User owner) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("==========================================");
            System.out.println("          MANAGE USERS & ROLES");
            System.out.println("==========================================");
            System.out.println(" [1] View All System Users");
            System.out.println(" [2] View Pending Registrations");
            System.out.println(" [3] Approve User Account");
            System.out.println(" [0] Back");
            System.out.println("------------------------------------------");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    System.out.println("\nALL SYSTEM USERS");
                    printUserTable(userDao.getAllUsers());
                    break;
                case "2":
                    System.out.println("\nPENDING REGISTRATIONS");
                    printUserTable(userDao.getPendingUsers());
                    break;
                case "3":
                    approveUserAccount(owner);
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 0 to 3.");
            }
        }
    }

    private void approveUserAccount(User owner) {
        System.out.println("\nAPPROVE USER ACCOUNT");
        List<User> pending = userDao.getPendingUsers();

        if (pending.isEmpty()) {
            System.out.println("No pending accounts to approve.");
            return;
        }

        printUserTable(pending);

        while (true) {
            System.out.print("Enter User ID to approve (0 to cancel): ");
            String input = scanner.nextLine().trim();

            int userId;
            try {
                userId = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric User ID.");
                continue;
            }

            if (userId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            User target = findInList(pending, userId);
            if (target == null) {
                System.out.println("User ID " + userId + " is not in the pending list. Try again.");
                continue;
            }

            if (userDao.approveUser(userId)) {
                userDao.logActivity(owner.user_id(), "STATUS_CHANGE",
                        owner.username() + " approved " + target.username());
                System.out.println("Success! " + target.username() + " (" + target.role() + ") has been approved.");
            } else {
                System.out.println("Approval failed. Please try again.");
            }
            return;
        }
    }

    private User findInList(List<User> users, int userId) {
        for (User u : users) {
            if (u.user_id() == userId) {
                return u;
            }
        }
        return null;
    }

    private void printUserTable(List<User> users) {
        if (users == null || users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        String format = "| %-5s | %-15s | %-12s | %-10s |%n";
        String border = "+-------+-----------------+--------------+------------+";

        System.out.println(border);
        System.out.printf(format, "ID", "Username", "Role", "Status");
        System.out.println(border);
        for (User u : users) {
            System.out.printf(format,
                    String.valueOf(u.user_id()),
                    truncate(u.username(), 15),
                    truncate(u.role(), 12),
                    truncate(u.status(), 10));
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