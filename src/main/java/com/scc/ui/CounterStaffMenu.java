package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class CounterStaffMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    public CounterStaffMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User staff) {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("========== COUNTER STAFF MENU ==========");
            System.out.println("[1] Register New Customer");
            System.out.println("[2] Edit Customer Details");
            System.out.println("[3] Search Customer Borrowing History / Overdue Balance");
            System.out.println("[4] View Bike Availability, Status and Rates");
            System.out.println("[5] Borrow a Bicycle (Create Rental)");
            System.out.println("[6] Return a Bicycle (Late Fee and Payment)");
            System.out.println("[7] Cancel Invalid / Unfulfilled Rental");
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
                    comingSoon();
                    break;
                case "0":
                    userDao.logActivity(staff.user_id(), "LOGOUT", staff.username() + " logged out");
                    System.out.println("Logging out... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 0 to 7.");
            }
        }
    }

    private void comingSoon() {
        System.out.println(">> Coming soon.");
    }
}