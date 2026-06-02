package com.example.classroomquizbattleapp.ui;

import com.example.classroomquizbattleapp.database.DatabaseHelper;
import com.example.classroomquizbattleapp.model.Question;
import com.example.classroomquizbattleapp.model.Student;
import com.example.classroomquizbattleapp.server.QuizServer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.net.InetAddress;
import java.util.List;

public class SessionHostScreen {
    private Stage stage;
    private int quizId;
    private String quizTitle;
    private QuizServer server;
    private String sessionCode;
    private VBox studentListBox;
    private int studentCount = 0;
    private Label studentCountLabel;

    public SessionHostScreen(Stage stage, int quizId, String quizTitle) {
        this.stage = stage;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.sessionCode = String.valueOf((int)(Math.random() * 9000) + 1000);
    }

    public void show() {
        List<Question> questions = DatabaseHelper.getQuestions(quizId);
        server = new QuizServer(questions, sessionCode);
        server.start();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        HBox topBar = new HBox();
        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #16213e;");
        Label titleLabel = new Label("Live Session — Host View");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        topBar.getChildren().add(titleLabel);
        root.setTop(topBar);

        VBox content = new VBox(25);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_CENTER);

        Label codeLabel = new Label("Session Code");
        codeLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 14px;");

        HBox codeBox = new HBox(15);
        codeBox.setAlignment(Pos.CENTER);
        for (char c : sessionCode.toCharArray()) {
            Label digit = new Label(String.valueOf(c));
            digit.setStyle("-fx-background-color: #0f3460; -fx-text-fill: #4361ee; " +
                    "-fx-font-size: 48px; -fx-font-weight: bold; " +
                    "-fx-padding: 15 25; -fx-background-radius: 12;");
            codeBox.getChildren().add(digit);
        }

        // FIX (Issue 12): Show the teacher's actual LAN IP so students on other machines
        // know what IP to type in their host field.
        String localIp = "localhost";
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {}

        Label ipLabel = new Label("📡 Your IP: " + localIp + "   (tell students to enter this)");
        ipLabel.setStyle("-fx-text-fill: #4cc9f0; -fx-font-size: 13px; -fx-font-weight: bold;");

        studentCountLabel = new Label("Students joined: 0 / 30");
        studentCountLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 14px;");

        Label quizInfo = new Label(quizTitle + " · " + questions.size() + " Questions");
        quizInfo.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label waitingLabel = new Label("Waiting Room");
        waitingLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 13px;");

        studentListBox = new VBox(8);
        studentListBox.setAlignment(Pos.CENTER);

        ScrollPane studentScroll = new ScrollPane(studentListBox);
        studentScroll.setFitToWidth(true);
        studentScroll.setPrefHeight(120);
        studentScroll.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        HBox btnRow = new HBox(15);
        btnRow.setAlignment(Pos.CENTER);

        Button startBtn = new Button("Start Quiz");
        startBtn.setStyle("-fx-background-color: #4361ee; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 40; " +
                "-fx-background-radius: 10; -fx-cursor: hand;");

        Button cancelBtn = new Button("Cancel Session");
        cancelBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 12 30; " +
                "-fx-background-radius: 10; -fx-cursor: hand;");

        cancelBtn.setOnAction(e -> {
            server.stop();
            new TeacherDashboard(stage).show();
        });

        startBtn.setOnAction(e -> {
            if (server.getClients().isEmpty()) {
                showAlert("No students have joined yet!");
                return;
            }
            // FIX (Issue 2 guard): Prevent starting with no questions.
            if (questions.isEmpty()) {
                showAlert("This quiz has no questions! Please add questions before hosting.");
                return;
            }
            new LiveQuizTeacherScreen(stage, server, sessionCode).show();
        });

        // Poll for new students
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    int count = server.getStudentList().size();
                    if (count != studentCount) {
                        studentCount = count;
                        List<Student> students = server.getStudentList();
                        Platform.runLater(() -> {
                            studentCountLabel.setText("Students joined: " + count + " / 30");
                            studentListBox.getChildren().clear();
                            HBox row = new HBox(10);
                            row.setAlignment(Pos.CENTER);
                            for (Student s : students) {
                                Label nameTag = new Label(s.getName());
                                nameTag.setStyle("-fx-background-color: #0f3460; " +
                                        "-fx-text-fill: white; -fx-padding: 6 12; " +
                                        "-fx-background-radius: 20; -fx-font-size: 13px;");
                                row.getChildren().add(nameTag);
                            }
                            studentListBox.getChildren().add(row);
                        });
                    }
                } catch (InterruptedException ex) { break; }
            }
        }).start();

        btnRow.getChildren().addAll(startBtn, cancelBtn);
        content.getChildren().addAll(codeLabel, codeBox, ipLabel, studentCountLabel,
                quizInfo, waitingLabel, studentScroll, btnRow);
        root.setCenter(content);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Quiz Battle — Host Session");
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
