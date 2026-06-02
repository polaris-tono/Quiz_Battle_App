package com.example.classroomquizbattleapp.ui;

import com.example.classroomquizbattleapp.model.Student;
import com.example.classroomquizbattleapp.server.QuizServer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;

public class LeaderboardScreen {
    private Stage stage;
    private List<Student> students;
    private QuizServer server;

    public LeaderboardScreen(Stage stage, List<Student> students, QuizServer server) {
        this.stage = stage;
        this.students = students;
        this.server = server;
    }

    public void show() {
        students.sort((a, b) -> b.getScore() - a.getScore());

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Top
        VBox topBar = new VBox(5);
        topBar.setPadding(new Insets(25));
        topBar.setAlignment(Pos.CENTER);
        topBar.setStyle("-fx-background-color: #16213e;");

        Label title = new Label("Final Leaderboard");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitle = new Label("Quiz Complete! 🎉");
        subtitle.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 14px;");

        topBar.getChildren().addAll(title, subtitle);
        root.setTop(topBar);

        // Leaderboard list
        VBox listBox = new VBox(12);
        listBox.setPadding(new Insets(30));
        listBox.setAlignment(Pos.TOP_CENTER);

        // Top 3 podium
        if (students.size() >= 1) {
            HBox podium = new HBox(15);
            podium.setAlignment(Pos.CENTER);

            if (students.size() >= 2) {
                podium.getChildren().add(makePodiumCard(students.get(1), "2", "#C0C0C0", 100));
            }
            podium.getChildren().add(makePodiumCard(students.get(0), "1", "#FFD700", 130));
            if (students.size() >= 3) {
                podium.getChildren().add(makePodiumCard(students.get(2), "3", "#CD7F32", 80));
            }
            listBox.getChildren().add(podium);
        }

        // Rest of students
        for (int i = 3; i < students.size(); i++) {
            HBox row = makeRankRow(i + 1, students.get(i));
            listBox.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #1a1a2e;");
        root.setCenter(scroll);

        // Bottom
        HBox bottomBar = new HBox(15);
        bottomBar.setPadding(new Insets(20));
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-background-color: #16213e;");

        Button backBtn = new Button("Back to Dashboard");
        backBtn.setStyle("-fx-background-color: #4361ee; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 35; " +
                "-fx-background-radius: 10; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            if (server != null) server.stop();
            new TeacherDashboard(stage).show();
        });

        bottomBar.getChildren().add(backBtn);
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Quiz Battle — Leaderboard");
        stage.setScene(scene);
        stage.show();
    }

    private VBox makePodiumCard(Student student, String rank, String color, double height) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(160);
        card.setPrefHeight(height);
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12; " +
                "-fx-border-color: " + color + "; -fx-border-width: 3; -fx-border-radius: 12;");

        Label avatar = new Label(student.getName().substring(0, Math.min(3, student.getName().length())).toUpperCase());
        avatar.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #1a1a2e; " +
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 14; " +
                "-fx-background-radius: 50;");

        Label rankLabel = new Label("#" + rank);
        rankLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label nameLabel = new Label(student.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label scoreLabel = new Label(student.getScore() + " pts");
        scoreLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        card.getChildren().addAll(avatar, rankLabel, nameLabel, scoreLabel);
        return card;
    }

    private HBox makeRankRow(int rank, Student student) {
        HBox row = new HBox(15);
        row.setPadding(new Insets(12, 20, 12, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(600);
        row.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label rankLabel = new Label(String.valueOf(rank));
        rankLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 16px; " +
                "-fx-font-weight: bold; -fx-min-width: 30;");

        Label nameLabel = new Label(student.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label scoreLabel = new Label(student.getScore() + " pts");
        scoreLabel.setStyle("-fx-text-fill: #4361ee; -fx-font-size: 14px; -fx-font-weight: bold;");

        row.getChildren().addAll(rankLabel, nameLabel, scoreLabel);
        return row;
    }
}