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
            System.out.println(" [1] Create New User");
            System.out.println(" [2] View All System Users");
            System.out.println(" [3] View Pending Registrations");
            System.out.println(" [4] Approve User Account");
            System.out.println(" [5] Update User Role & Status");
            System.out.println(" [6] Delete (Deactivate) User");
            System.out.println(" [0] Back");
            System.out.println("------------------------------------------");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> comingSoon();
                case "2" -> {
                    System.out.println("\nALL SYSTEM USERS");
                    printUserTable(userDao.getAllUsers());
                }
                case "3" -> {
                    System.out.println("\nPENDING REGISTRATIONS");
                    printUserTable(userDao.getPendingUsers());
                }
                case "4" -> approveUserAccount(admin);
                case "5" -> updateUser(admin);
                case "6" -> comingSoon();
                case "0" -> running = false;
                default -> System.out.println("Invalid choice. Please enter a number from 0 to 6.");
            }
        }
    }

    // ---------------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------------
    private void createUser(User admin) {
        System.out.println("\nCREATE NEW USER (enter 0 at any step to cancel)");

        String firstName = readRequired("First name: ");
        if (firstName == null) { cancelled("Creation"); return; }

        String lastName = readRequired("Last name: ");
        if (lastName == null) { cancelled("Creation"); return; }

        String username;
        while (true) {
            username = readRequired("Username: ");
            if (username == null) { cancelled("Creation"); return; }
            if (userDao.usernameExists(username)) {
                System.out.println("Username already taken. Try another.");
            } else {
                break;
            }
        }

        String password;
        while (true) {
            password = readRequired("Password (min 6 characters): ");
            if (password == null) { cancelled("Creation"); return; }
            if (password.length() < 6) {
                System.out.println("Password must be at least 6 characters.");
            } else {
                break;
            }
        }

        String role = readValidRole();
        if (role == null) { cancelled("Creation"); return; }

        String status = readValidStatus();
        if (status == null) { cancelled("Creation"); return; }

        if (userDao.createUser(firstName, lastName, username, password, role, status)) {
            userDao.logActivity(admin.user_id(), "USER_CREATE",
                    admin.username() + " created " + username + " (" + role + " / " + status + ")");
            System.out.println("Success! User " + username + " created as " + role + " / " + status + ".");
        } else {
            System.out.println("Creation failed. Please try again.");
        }
    }

    // ---------------------------------------------------------------
    // READ
    // ---------------------------------------------------------------
    // ---------------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------------
    private void approveUserAccount(User admin) {
        System.out.println("\nAPPROVE USER ACCOUNT");
        List<User> pending = userDao.getPendingUsers();

        if (pending.isEmpty()) {
            System.out.println("No pending accounts to approve.");
            return;
        }

        printUserTable(pending);

        while (true) {
            int userId = readIntInput("Enter User ID to approve (0 to cancel): ");

            if (userId == 0) {
                cancelled("Approval");
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

    private void updateUser(User admin) {
        System.out.println("\nUPDATE USER ROLE & STATUS");
        List<User> users = userDao.getAllUsers();
        printUserTable(users);

        while (true) {
            int targetUserId = readIntInput("Enter User ID to update (0 to cancel): ");

            if (targetUserId == 0) {
                cancelled("Update");
                return;
            }

            if (targetUserId == admin.user_id()) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                continue;
            }

            User target = findInList(users, targetUserId);
            if (target == null) {
                System.out.println("User ID " + targetUserId + " does not exist. Try again.");
                continue;
            }

            String newRole = readValidRole();
            if (newRole == null) { cancelled("Update"); return; }

            String newStatus = readValidStatus();
            if (newStatus == null) { cancelled("Update"); return; }

            if (userDao.updateUserRoleAndStatus(targetUserId, newRole, newStatus)) {
                userDao.logActivity(admin.user_id(), "STATUS_CHANGE",
                        admin.username() + " changed " + target.username()
                                + " to " + newRole + " / " + newStatus);
                System.out.println("Success! " + target.username() + " is now "
                        + newRole + " with status " + newStatus + ".");
            } else {
                System.out.println("Update failed. Please try again.");
            }
            return;
        }
    }

    // ---------------------------------------------------------------
    // DELETE (soft delete = DEACTIVATED, user can no longer log in)
    // ---------------------------------------------------------------
    private void deleteUser(User admin) {
        System.out.println("\nDELETE (DEACTIVATE) USER");
        List<User> users = userDao.getAllUsers();
        printUserTable(users);

        while (true) {
            int targetUserId = readIntInput("Enter User ID to delete (0 to cancel): ");

            if (targetUserId == 0) {
                cancelled("Delete");
                return;
            }

            if (targetUserId == admin.user_id()) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                continue;
            }

            User target = findInList(users, targetUserId);
            if (target == null) {
                System.out.println("User ID " + targetUserId + " does not exist. Try again.");
                continue;
            }

            if ("DEACTIVATED".equalsIgnoreCase(target.status())) {
                System.out.println(target.username() + " is already deleted (deactivated).");
                return;
            }

            System.out.print("Delete " + target.username() + "? Type YES to confirm: ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("YES")) {
                cancelled("Delete");
                return;
            }

            if (userDao.deactivateUser(targetUserId)) {
                userDao.logActivity(admin.user_id(), "USER_DELETE",
                        admin.username() + " deleted (deactivated) " + target.username());
                System.out.println("Success! " + target.username() + " has been deleted (deactivated).");
            } else {
                System.out.println("Delete failed. Please try again.");
            }
            return;
        }
    }

    // ---------------------------------------------------------------
    // INPUT VALIDATION WRAPPERS
    // ---------------------------------------------------------------
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

    // Returns null if the admin enters 0 (cancel)
    private String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (value.equals("0")) return null;
            if (!value.isEmpty()) return value;
            System.out.println("This field is required.");
        }
    }

    // Returns null if the admin enters 0 (cancel)
    private String readValidRole() {
        while (true) {
            System.out.print("Enter role (OWNER, COUNTER, MAINTENANCE) or 0 to cancel: ");
            String role = scanner.nextLine().trim().toUpperCase();
            if (role.equals("0")) return null;
            if (role.equals("OWNER") || role.equals("COUNTER") || role.equals("MAINTENANCE")) {
                return role;
            }
            System.out.println("Invalid role. Please try again.");
        }
    }

    // Returns null if the admin enters 0 (cancel)
    private String readValidStatus() {
        while (true) {
            System.out.print("Enter status (APPROVED, PENDING) or 0 to cancel: ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (status.equals("0")) return null;
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("Invalid status. Please try again.");
        }
    }

    // ---------------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------------
    private void comingSoon() {
        System.out.println(">> Coming soon.");
    }

    private void cancelled(String action) {
        System.out.println(action + " cancelled.");
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

        String format = "| %-5s | %-15s | %-12s | %-11s |%n";
        String border = "+-------+-----------------+--------------+-------------+";

        System.out.println(border);
        System.out.printf(format, "ID", "Username", "Role", "Status");
        System.out.println(border);
        for (User u : users) {
            System.out.printf(format,
                    String.valueOf(u.user_id()),
                    truncate(u.username(), 15),
                    truncate(u.role(), 12),
                    truncate(u.status(), 11));
        }
        System.out.println(border);
    }

    private String truncate(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max - 1) + "~";
    }
}