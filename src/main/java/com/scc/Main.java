package com.scc;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.CounterStaffMenu;
import com.scc.ui.MaintenanceStaffMenu;
import com.scc.ui.OwnerMenu;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        UserDao userDao = new UserDao();
        Scanner scanner = new Scanner(System.in);

        OwnerMenu ownerMenu = new OwnerMenu(userDao, scanner);
        CounterStaffMenu counterMenu = new CounterStaffMenu(userDao, scanner);
        MaintenanceStaffMenu maintenanceMenu = new MaintenanceStaffMenu(userDao, scanner);

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=====================================================");
            System.out.println("   BICYCLE LENDING AND PENALTY FEE SYSTEM");
            System.out.println("=====================================================");
            System.out.println("[1] Login");
            System.out.println("[2] Register (Staff)");
            System.out.println("[0] Exit");
            System.out.print("Enter choice: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> handleLogin(userDao, scanner, ownerMenu, counterMenu, maintenanceMenu);
                case "2" -> handleRegister(userDao, scanner);
                case "0" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please enter 0, 1 or 2.");
            }
        }
        scanner.close();
    }

    private static void handleLogin(UserDao userDao, Scanner scanner, OwnerMenu ownerMenu,
                                    CounterStaffMenu counterMenu, MaintenanceStaffMenu maintenanceMenu) {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        User user = userDao.login(username, password);
        if (user == null) {
            return;
        }

        System.out.println("\nWelcome, " + user.fullName() + "! (" + user.role() + ")");
        switch (user.role().toUpperCase()) {
            case "OWNER" -> ownerMenu.display(user);
            case "COUNTER" -> counterMenu.display(user);
            case "MAINTENANCE" -> maintenanceMenu.display(user);
            default -> System.out.println("Unknown role: " + user.role());
        }
    }

    private static void handleRegister(UserDao userDao, Scanner scanner) {
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

        System.out.println("Role:  [1] Counter Staff   [2] Maintenance Staff");
        System.out.print("Enter choice: ");
        String role;
        switch (scanner.nextLine().trim()) {
            case "1" -> role = "COUNTER";
            case "2" -> role = "MAINTENANCE";
            default -> {
                System.out.println(" -> Invalid role choice.");
                return;
            }
        }

        userDao.registerUser(firstName, lastName, username, password, role);
    }
}