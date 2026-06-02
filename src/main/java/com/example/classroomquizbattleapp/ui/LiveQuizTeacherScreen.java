package com.example.classroomquizbattleapp.ui;

import com.example.classroomquizbattleapp.database.DatabaseHelper;
import com.example.classroomquizbattleapp.model.Question;
import com.example.classroomquizbattleapp.model.Student;
import com.example.classroomquizbattleapp.server.ClientHandler;
import com.example.classroomquizbattleapp.server.QuizServer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;

public class LiveQuizTeacherScreen {
    private Stage stage;
    private QuizServer server;
    private String sessionCode;
    private List<Question> questions;
    private int currentIndex = 0;
    private Label questionLabel, timerLabel, progressLabel;
    private int timeLeft;
    private Thread timerThread;

    public LiveQuizTeacherScreen(Stage stage, QuizServer server, String sessionCode) {
        this.stage = stage;
        this.server = server;
        this.sessionCode = sessionCode;
        this.questions = server.getQuestions();
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Top Bar
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(20));
        topBar.setStyle("-fx-background-color: #16213e;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        // FIX (Issue 2): Initialize label as "Loading..." — showQuestion() will set the
        // real value. The old code set it here with questions.size() which showed 0
        // if the list was empty due to the DB path bug.
        progressLabel = new Label("Loading...");
        progressLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        timerLabel = new Label("⏱ 30");
        timerLabel.setStyle("-fx-text-fill: #4361ee; -fx-font-size: 24px; -fx-font-weight: bold;");
        HBox.setHgrow(progressLabel, Priority.ALWAYS);

        topBar.getChildren().addAll(progressLabel, timerLabel);
        root.setTop(topBar);

        // Center
        VBox content = new VBox(25);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.CENTER);

        questionLabel = new Label();
        questionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 22px; " +
                "-fx-font-weight: bold; -fx-wrap-text: true; -fx-text-alignment: center;");
        questionLabel.setMaxWidth(700);
        questionLabel.setWrapText(true);

        GridPane optionsGrid = new GridPane();
        optionsGrid.setHgap(15);
        optionsGrid.setVgap(15);
        optionsGrid.setAlignment(Pos.CENTER);

        // Bottom
        HBox bottomBar = new HBox(15);
        bottomBar.setPadding(new Insets(20));
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-background-color: #16213e;");

        Label studentsLabel = new Label("Students: " + server.getClients().size());
        studentsLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 14px;");

        Button nextBtn = new Button("Next Question →");
        nextBtn.setStyle("-fx-background-color: #4361ee; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 35; " +
                "-fx-background-radius: 10; -fx-cursor: hand;");

        nextBtn.setOnAction(e -> {
            if (timerThread != null) timerThread.interrupt();
            collectAnswers();
            currentIndex++;
            if (currentIndex < questions.size()) {
                showQuestion(questions.get(currentIndex), optionsGrid, nextBtn);
            } else {
                showLeaderboard();
            }
        });

        bottomBar.getChildren().addAll(studentsLabel, nextBtn);
        root.setBottom(bottomBar);

        content.getChildren().addAll(questionLabel, optionsGrid);
        root.setCenter(content);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Quiz Battle — Live Quiz");
        stage.setScene(scene);
        stage.show();

        // FIX (Issue 2): Guard against empty questions list to prevent
        // IndexOutOfBoundsException crash.
        if (questions.isEmpty()) {
            progressLabel.setText("No questions found!");
            questionLabel.setText("This quiz has no questions. Please go back and add some.");
            nextBtn.setDisable(true);
        } else {
            showQuestion(questions.get(0), optionsGrid, nextBtn);
        }
    }

    private void showQuestion(Question q, GridPane grid, Button nextBtn) {
        server.broadcastQuestion(q, currentIndex + 1);
        for (ClientHandler c : server.getClients()) c.resetAnswer();

        Platform.runLater(() -> {
            // This now correctly shows the real count every time.
            progressLabel.setText("Question " + (currentIndex + 1) + " of " + questions.size());
            questionLabel.setText(q.getQuestionText());

            grid.getChildren().clear();
            String[] opts   = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};
            String[] labels = {"A", "B", "C", "D"};
            String[] colors = {"#4361ee", "#7209b7", "#f72585", "#4cc9f0"};

            for (int i = 0; i < 4; i++) {
                VBox optBox = new VBox(5);
                optBox.setPadding(new Insets(15));
                optBox.setMinWidth(250);
                optBox.setAlignment(Pos.CENTER_LEFT);
                boolean isCorrect = labels[i].equals(q.getCorrectAnswer());
                optBox.setStyle("-fx-background-color: " + colors[i] + "; " +
                        "-fx-background-radius: 10;" +
                        (isCorrect ? " -fx-border-color: #00ff88; -fx-border-width: 3; -fx-border-radius: 10;" : ""));

                Label optLabel = new Label(labels[i] + "  " + opts[i]);
                optLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
                optBox.getChildren().add(optLabel);
                grid.add(optBox, i % 2, i / 2);
            }
        });

        startTimer(q.getTimeLimit(), nextBtn);
    }

    private void startTimer(int seconds, Button nextBtn) {
        timeLeft = seconds;
        timerThread = new Thread(() -> {
            while (timeLeft > 0) {
                try {
                    Thread.sleep(1000);
                    timeLeft--;
                    Platform.runLater(() -> {
                        timerLabel.setText("⏱ " + timeLeft);
                        if (timeLeft <= 5) {
                            timerLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 24px; -fx-font-weight: bold;");
                        }
                    });
                } catch (InterruptedException e) { return; }
            }
            Platform.runLater(() -> nextBtn.fire());
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }

    private void collectAnswers() {
        Question q = questions.get(currentIndex);
        for (ClientHandler c : server.getClients()) {
            if (c.getStudent() != null) {
                // FIX (Issue 7): getCurrentAnswer() can be empty string — equals() is safe,
                // but this makes intent explicit: only score non-empty correct answers.
                String answer = c.getCurrentAnswer();
                if (!answer.isEmpty() && answer.equals(q.getCorrectAnswer())) {
                    c.getStudent().addScore(10);
                }
            }
        }
    }

    private void showLeaderboard() {
        List<Student> students = server.getStudentList();
        for (Student s : students) {
            DatabaseHelper.saveResult(s.getName(), s.getScore(), sessionCode);
        }
        server.broadcastLeaderboard(students);
        Platform.runLater(() -> new LeaderboardScreen(stage, students, server).show());
    }
}
