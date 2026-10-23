package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // 1. Initialize database
        DatabaseInitializer.initialize();
        System.out.println("Database file: " + new File("BicycleLendingSystem.db").getAbsolutePath());

        // 2. Instantiate shared DAOs and utilities
        UserDao userDao = new UserDao();
        Scanner scanner = new Scanner(System.in);

        // 3. Delegate execution to Authentication
        new Authentication(userDao, scanner).start();

        scanner.close();
    }
}