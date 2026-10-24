package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User admin) {
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
                case "1" -> {
                    System.out.println("\nALL SYSTEM USERS");
                    printUserTable(userDao.getAllUsers());
                }
                case "2" -> {
                    System.out.println("\nPENDING REGISTRATIONS");
                    printUserTable(userDao.getPendingUsers());
                }
                case "3" -> approveUserAccount(admin);
                case "0" -> running = false;
                default -> System.out.println("Invalid choice. Please enter a number from 0 to 3.");
            }
        }
    }

    private void approveUserAccount(User admin) {
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
                userDao.logActivity(admin.user_id(), "STATUS_CHANGE",
                        admin.username() + " approved " + target.username());
                System.out.println("Success! " + target.username() + " (" + target.role() + ") has been approved.");
            } else {
                System.out.println("Approval failed. Please try again.");
            }
            return;
        }
    }

    private User findInList(List<User> users, int userId) {
        for (User u : users) {
            if (u.user_id() == userId) return u;
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
}