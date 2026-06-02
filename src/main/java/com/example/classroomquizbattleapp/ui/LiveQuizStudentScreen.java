package com.example.classroomquizbattleapp.ui;

import com.example.classroomquizbattleapp.client.QuizClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LiveQuizStudentScreen {
    private Stage stage;
    private QuizClient client;
    private String studentName;
    private Label scoreLabel, questionLabel, timerLabel, statusLabel;
    private VBox optionsBox;
    private boolean answered = false;

    // FIX (Issue — student local score): Removed local score tracking entirely.
    // The student's true score comes from the server leaderboard, not local counting.
    // Local counting was wrong because it added 10 for EVERY answer, even wrong ones.

    public LiveQuizStudentScreen(Stage stage, QuizClient client, String studentName) {
        this.stage = stage;
        this.client = client;
        this.studentName = studentName;
    }

    // FIX (Issue 3): Separated listener attachment from show() so StudentJoinScreen
    // can call attachListener() BEFORE client.connect(), eliminating the race condition
    // where JOINED arrives before the listener is registered.
    public void attachListener() {
        client.setMessageListener(new QuizClient.MessageListener() {
            @Override
            public void onMessageReceived(String message) {
                Platform.runLater(() -> handleMessage(message));
            }
            @Override
            public void onDisconnected() {
                Platform.runLater(() -> {
                    if (statusLabel != null)
                        statusLabel.setText("Disconnected from server.");
                });
            }
        });
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Top Bar
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #16213e;");

        Label nameLabel = new Label("👤 " + studentName);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        scoreLabel = new Label("Score: 0");
        scoreLabel.setStyle("-fx-text-fill: #4361ee; -fx-font-size: 15px; -fx-font-weight: bold;");

        timerLabel = new Label("⏱ --");
        timerLabel.setStyle("-fx-text-fill: #f72585; -fx-font-size: 18px; -fx-font-weight: bold;");

        topBar.getChildren().addAll(nameLabel, scoreLabel, new Label("   "), timerLabel);
        root.setTop(topBar);

        // Center
        VBox center = new VBox(20);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(40));

        statusLabel = new Label("⏳ Waiting for teacher to start the quiz...");
        statusLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 18px;");

        questionLabel = new Label("");
        questionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; " +
                "-fx-wrap-text: true; -fx-text-alignment: center;");
        questionLabel.setMaxWidth(700);
        questionLabel.setWrapText(true);

        optionsBox = new VBox(12);
        optionsBox.setAlignment(Pos.CENTER);
        optionsBox.setMaxWidth(600);

        center.getChildren().addAll(statusLabel, questionLabel, optionsBox);
        root.setCenter(center);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Quiz Battle — " + studentName);
        stage.setScene(scene);
        stage.show();

        // NOTE: Listener is already attached via attachListener() before show() is called.
        // Do NOT call client.setMessageListener() here again — that would overwrite the
        // already-buffered messages and re-introduce the race condition.
    }

    private void handleMessage(String message) {
        if (message == null || message.isEmpty()) return;

        if (message.startsWith("QUESTION|")) {
            answered = false;
            String[] parts = message.split("\\|");

            // FIX (Issue 7): Validate message has all expected parts before parsing.
            if (parts.length < 9) {
                statusLabel.setText("Received malformed question. Please wait...");
                return;
            }

            String qText    = parts[2];
            String optA     = parts[3];
            String optB     = parts[4];
            String optC     = parts[5];
            String optD     = parts[6];
            int timeLimit;
            try {
                timeLimit = Integer.parseInt(parts[7]);
            } catch (NumberFormatException e) {
                timeLimit = 30; // safe default
            }
            String qType = parts[8];

            statusLabel.setText("");
            questionLabel.setText(qText);
            optionsBox.getChildren().clear();

            if (qType.equals("TRUE_FALSE")) {
                showTrueFalseOptions(optA, optB);
            } else {
                showMCQOptions(optA, optB, optC, optD);
            }

            startCountdown(timeLimit);

        } else if (message.startsWith("LEADERBOARD|")) {
            showLeaderboard(message);

        } else if (message.startsWith("JOINED|")) {
            // FIX (Issue 3): This now always fires correctly because listener was
            // attached before connect() was called.
            statusLabel.setText("✅ Connected! Waiting for quiz to start...");

        } else if (message.startsWith("ERROR|")) {
            // FIX (Issue 1): Show server-side errors (e.g. wrong session code) to user.
            String[] parts = message.split("\\|");
            String errorMsg = parts.length > 1 ? parts[1] : "Unknown error";
            statusLabel.setText("❌ " + errorMsg);
            statusLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 18px;");
        }
    }

    private void showMCQOptions(String a, String b, String c, String d) {
        String[] opts   = {a, b, c, d};
        String[] labels = {"A", "B", "C", "D"};
        String[] colors = {"#4361ee", "#7209b7", "#f72585", "#4cc9f0"};

        for (int i = 0; i < 4; i++) {
            if (opts[i] == null || opts[i].equals("-")) continue;
            final String answer = labels[i];
            Button optBtn = new Button(labels[i] + "   " + opts[i]);
            optBtn.setMaxWidth(Double.MAX_VALUE);
            optBtn.setStyle("-fx-background-color: " + colors[i] + "; -fx-text-fill: white; " +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 14; " +
                    "-fx-background-radius: 10; -fx-cursor: hand; -fx-alignment: center-left;");
            optBtn.setOnAction(e -> submitAnswer(answer, optBtn));
            optionsBox.getChildren().add(optBtn);
        }
    }

    private void showTrueFalseOptions(String optA, String optB) {
        HBox tfRow = new HBox(20);
        tfRow.setAlignment(Pos.CENTER);

        Button trueBtn = new Button("✓  TRUE");
        trueBtn.setPrefWidth(200);
        trueBtn.setPrefHeight(80);
        trueBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-font-size: 20px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12; -fx-cursor: hand;");

        Button falseBtn = new Button("✗  FALSE");
        falseBtn.setPrefWidth(200);
        falseBtn.setPrefHeight(80);
        falseBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-size: 20px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12; -fx-cursor: hand;");

        trueBtn.setOnAction(e -> submitAnswer("A", trueBtn));
        falseBtn.setOnAction(e -> submitAnswer("B", falseBtn));

        tfRow.getChildren().addAll(trueBtn, falseBtn);
        optionsBox.getChildren().add(tfRow);
    }

    private void submitAnswer(String answer, Button clicked) {
        if (!answered) {
            answered = true;
            client.sendAnswer(answer);
            clicked.setStyle(clicked.getStyle() +
                    "-fx-border-color: #00ff88; -fx-border-width: 3; -fx-border-radius: 12;");
            optionsBox.getChildren().forEach(node -> {
                if (node instanceof Button btn && btn != clicked) {
                    btn.setOpacity(0.4);
                    btn.setDisable(true);
                } else if (node instanceof HBox row) {
                    row.getChildren().forEach(child -> {
                        if (child instanceof Button btn && btn != clicked) {
                            btn.setOpacity(0.4);
                            btn.setDisable(true);
                        }
                    });
                }
            });
            // FIX (local score bug): Do NOT add score here — student score is determined
            // server-side in collectAnswers() and shown via the leaderboard broadcast.
            statusLabel.setText("✅ Answer submitted! Waiting for next question...");
        }
    }

    private void startCountdown(int seconds) {
        timerLabel.setStyle("-fx-text-fill: #f72585; -fx-font-size: 18px; -fx-font-weight: bold;");
        final int[] timeLeft = {seconds};
        timerLabel.setText("⏱ " + seconds);
        Thread t = new Thread(() -> {
            while (timeLeft[0] > 0) {
                try {
                    Thread.sleep(1000);
                    timeLeft[0]--;
                    Platform.runLater(() -> {
                        timerLabel.setText("⏱ " + timeLeft[0]);
                        if (timeLeft[0] <= 5) {
                            timerLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 18px; -fx-font-weight: bold;");
                        }
                    });
                } catch (InterruptedException e) { return; }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void showLeaderboard(String message) {
        String[] parts = message.split("\\|");
        VBox leaderboard = new VBox(15);
        leaderboard.setAlignment(Pos.CENTER);
        leaderboard.setPadding(new Insets(40));
        leaderboard.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("🏆 Final Leaderboard");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");
        leaderboard.getChildren().add(title);

        for (int i = 1; i < parts.length; i++) {
            String[] entry = parts[i].split(":");
            // FIX (Issue 7): Validate entry format before using it.
            if (entry.length == 2) {
                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(12, 20, 12, 20));
                row.setMaxWidth(500);
                boolean isMe = entry[0].equals(studentName);
                row.setStyle("-fx-background-color: " + (isMe ? "#4361ee" : "#16213e") + "; -fx-background-radius: 10;");

                Label rank = new Label("#" + i);
                rank.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 16px; -fx-font-weight: bold; -fx-min-width: 35;");

                Label name = new Label(entry[0] + (isMe ? " (You)" : ""));
                name.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                HBox.setHgrow(name, Priority.ALWAYS);

                Label pts = new Label(entry[1] + " pts");
                pts.setStyle("-fx-text-fill: " + (isMe ? "white" : "#4361ee") + "; -fx-font-size: 14px; -fx-font-weight: bold;");

                // FIX: Update score label to show actual server-confirmed score.
                if (isMe) {
                    scoreLabel.setText("Score: " + entry[1]);
                }

                row.getChildren().addAll(rank, name, pts);
                leaderboard.getChildren().add(row);
            }
        }

        ScrollPane scroll = new ScrollPane(leaderboard);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #1a1a2e;");
        Scene scene = new Scene(scroll, 900, 600);
        stage.setTitle("Quiz Battle — Results");
        stage.setScene(scene);
    }
}
