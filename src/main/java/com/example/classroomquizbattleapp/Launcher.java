package com.example.classroomquizbattleapp;

import com.example.classroomquizbattleapp.database.DatabaseHelper;

public class Launcher {
    public static void main(String[] args) {
        // 1. Initialize the database schema safely
        DatabaseHelper.initializeDatabase();

        // 2. Clear out repair blocks and immediately launch your application
        System.out.println("Application database linked up and ready.");

        // 3. Hand control back over to your JavaFX GUI launch
        HelloApplication.main(args);
    }
}