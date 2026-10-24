package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner scanner;
    private final OwnerMenu ownerMenu;
    private final CounterStaffMenu counterMenu;
    private final MaintenanceStaffMenu maintenanceMenu;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
        // The same UserDao and Scanner are shared with every menu.
        this.ownerMenu = new OwnerMenu(userDao, scanner);
        this.counterMenu = new CounterStaffMenu(userDao, scanner);
        this.maintenanceMenu = new MaintenanceStaffMenu(userDao, scanner);
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=====================================================");
            System.out.println("   BICYCLE LENDING AND PENALTY FEE SYSTEM");
            System.out.println("=====================================================");
            System.out.println("[1] Login");
            System.out.println("[2] Register");
            System.out.println("[0] Exit");
            System.out.print("Enter choice: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> handleLogin();
                case "2" -> handleRegister();
                case "0" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please enter 0, 1 or 2.");
            }
        }
    }

    private void handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        User user = userDao.login(username, password);
        if (user == null) {
            return;
        }

        System.out.println("\nWelcome, " + user.fullName() + "! (" + user.role() + ")");
        routeToMenu(user);
    }

    private void routeToMenu(User user) {
        switch (user.role().toUpperCase()) {
            case "OWNER" -> ownerMenu.display(user);
            case "COUNTER" -> counterMenu.display(user);
            case "MAINTENANCE" -> maintenanceMenu.display(user);
            default -> System.out.println("Unknown role: " + user.role());
        }
    }

    private void handleRegister() {
        System.out.print("First name: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("Last name: ");
        String lastName = scanner.nextLine().trim();
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty()) {
            System.out.println(" -> All fields are required.");
            return;
        }
        if (userDao.usernameExists(username)) {
            System.out.println(" -> Username already taken.");
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        if (password.length() < 6) {
            System.out.println(" -> Password must be at least 6 characters.");
            return;
        }

        String role = selectRole();
        if (role == null) {
            return;
        }

        userDao.registerUser(firstName, lastName, username, password, role);
    }

    private String selectRole() {
        System.out.println("Role:  [1] Shop Owner   [2] Counter Staff   [3] Maintenance Staff");
        System.out.print("Enter choice: ");
        switch (scanner.nextLine().trim()) {
            case "1": return "OWNER";
            case "2": return "COUNTER";
            case "3": return "MAINTENANCE";
            default:
                System.out.println(" -> Invalid role choice.");
                return null;
        }
    }
}