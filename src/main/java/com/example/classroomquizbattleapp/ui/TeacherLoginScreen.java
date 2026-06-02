package com.example.classroomquizbattleapp.ui;

import com.example.classroomquizbattleapp.database.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class TeacherLoginScreen {
    private Stage stage;

    public TeacherLoginScreen(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        // Main container
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(60));
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Logo/Title
        Label logo = new Label("QB");
        logo.setStyle("-fx-background-color: #4361ee; -fx-text-fill: white; " +
                "-fx-font-size: 32px; -fx-font-weight: bold; " +
                "-fx-padding: 15 25; -fx-background-radius: 12;");

        Label title = new Label("Classroom Quiz Battle");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitle = new Label("Teacher Portal");
        subtitle.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 14px;");

        // Login Card
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(380);
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 0, 4);");

        Label userLabel = new Label("Username");
        userLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");
        userLabel.setMaxWidth(Double.MAX_VALUE);

        TextField usernameField = new TextField();
        usernameField.setPromptText("teacher_admin");
        usernameField.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: #606080; -fx-padding: 12; " +
                "-fx-background-radius: 8; -fx-font-size: 14px; -fx-border-width: 0;");

        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");
        passLabel.setMaxWidth(Double.MAX_VALUE);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("••••••••");
        passwordField.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: #606080; -fx-padding: 12; " +
                "-fx-background-radius: 8; -fx-font-size: 14px; -fx-border-width: 0;");

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 12px;");

        Button loginBtn = new Button("Login as Teacher");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-background-color: #4361ee; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 13; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");

        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(
                "-fx-background-color: #3451d1; -fx-text-fill: white; " +
                        "-fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 13; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;"));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(
                "-fx-background-color: #4361ee; -fx-text-fill: white; " +
                        "-fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 13; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;"));

        loginBtn.setOnAction(e -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText().trim();
            if (user.equals("teacher_admin") && pass.equals("1234")) {
                DatabaseHelper.initializeDatabase();
                new TeacherDashboard(stage).show();
            } else {
                errorLabel.setText("Invalid username or password!");
            }
        });

        Label hint = new Label("LAN-based · No internet required");
        hint.setStyle("-fx-text-fill: #606080; -fx-font-size: 11px;");

        card.getChildren().addAll(userLabel, usernameField, passLabel,
                passwordField, errorLabel, loginBtn);

        root.getChildren().addAll(logo, title, subtitle, card, hint);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Quiz Battle — Teacher Login");
        stage.setScene(scene);
        stage.show();
    }
}