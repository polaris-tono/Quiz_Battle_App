package com.example.classroomquizbattleapp.ui;

import com.example.classroomquizbattleapp.database.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TeacherDashboard {
    private Stage stage;

    public TeacherDashboard(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Sidebar
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(200);
        sidebar.setPadding(new Insets(30, 15, 30, 15));
        sidebar.setStyle("-fx-background-color: #16213e;");

        Label appName = new Label("Quiz Battle");
        appName.setStyle("-fx-text-fill: #4361ee; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label dashLabel = makeSidebarItem("Dashboard", true);
        Label newQuizLabel = makeSidebarItem("New Quiz", false);
        Label resultsLabel = makeSidebarItem("Results", false);
        Label logoutLabel = makeSidebarItem("Logout", false);

        newQuizLabel.setOnMouseClicked(e -> new QuizCreationScreen(stage).show());
        logoutLabel.setOnMouseClicked(e -> new TeacherLoginScreen(stage).show());

        // FIX: Wire up the results button click event to launch the results viewer
        resultsLabel.setOnMouseClicked(e -> showResultsPopup());

        sidebar.getChildren().addAll(appName, new Label(""),
                dashLabel, newQuizLabel, resultsLabel, logoutLabel);
        root.setLeft(sidebar);

        // Main Content
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label welcome = new Label("Welcome back, Teacher");
        welcome.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");

        // Stats Row Calculations
        var quizzes = DatabaseHelper.getAllQuizzes();

        // FIX: Dynamically scan database record aggregates instead of leaving static dashes
        int totalSessionsRun = 0;
        int totalStudentsTested = 0;

        try (java.sql.Connection conn = DatabaseHelper.connect();
             java.sql.Statement stmt = conn.createStatement()) {

            // Count unique match games played by calculating distinct quiz games saved
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT quiz_title || timestamp) FROM results")) {
                if (rs.next()) totalSessionsRun = rs.getInt(1);
            }
            // Total individual student profiles recorded across history
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM results")) {
                if (rs.next()) totalStudentsTested = rs.getInt(1);
            }
        } catch (Exception ex) {
            System.err.println("Error calculating live stats counters: " + ex.getMessage());
        }

        HBox stats = new HBox(20);
        stats.getChildren().addAll(
                makeStatCard("Total Quizzes", String.valueOf(quizzes.size()), "#4361ee"),
                makeStatCard("Sessions Run", String.valueOf(totalSessionsRun), "#7209b7"),
                makeStatCard("Students Tested", String.valueOf(totalStudentsTested), "#f72585")
        );

        // Recent Quizzes
        Label recentLabel = new Label("My Quizzes");
        recentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox quizList = new VBox(10);
        if (quizzes.isEmpty()) {
            Label empty = new Label("No quizzes yet. Create your first quiz!");
            empty.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 14px;");
            quizList.getChildren().add(empty);
        } else {
            for (String[] quiz : quizzes) {
                HBox quizRow = makeQuizRow(quiz[0], quiz[1]);
                quizList.getChildren().add(quizRow);
            }
        }

        content.getChildren().addAll(welcome, stats, recentLabel, quizList);
        root.setCenter(content);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Quiz Battle — Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private Label makeSidebarItem(String text, boolean active) {
        Label label = new Label(text);
        String base = "-fx-text-fill: " + (active ? "white" : "#a0a0b0") + "; " +
                "-fx-font-size: 14px; -fx-padding: 10 15; -fx-cursor: hand; " +
                "-fx-background-radius: 8; -fx-max-width: infinity; " +
                (active ? "-fx-background-color: #4361ee;" : "");
        label.setStyle(base);
        label.setMaxWidth(Double.MAX_VALUE);
        if (!active) {
            label.setOnMouseEntered(e -> label.setStyle(base +
                    "-fx-background-color: #0f3460;"));
            label.setOnMouseExited(e -> label.setStyle(base));
        }
        return label;
    }

    private VBox makeStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(20));
        card.setPrefWidth(180);
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12;");

        Label val = new Label(value);
        val.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 32px; -fx-font-weight: bold;");

        Label lbl = new Label(title);
        lbl.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");

        card.getChildren().addAll(val, lbl);
        return card;
    }

    private HBox makeQuizRow(String id, String title) {
        HBox row = new HBox();
        row.setPadding(new Insets(15));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Button hostBtn = new Button("Host");
        hostBtn.setStyle("-fx-background-color: #4361ee; -fx-text-fill: white; " +
                "-fx-padding: 6 16; -fx-background-radius: 6; -fx-cursor: hand;");
        hostBtn.setOnAction(e -> new SessionHostScreen(stage, Integer.parseInt(id), title).show());

        row.getChildren().addAll(titleLabel, hostBtn);
        return row;
    }

    // FIX: Completely implement the missing Results screen view as a modern overlay window
    private void showResultsPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(stage);
        popupStage.setTitle("Historical Match Performance Logs");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: #1a1a2e;");

        Label header = new Label("Student Results History Table");
        header.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        layout.getChildren().add(header);

        VBox rowsContainer = new VBox(8);

        // Fetch raw history records line by line from the DB results database table
        String sql = "SELECT student_name, score, quiz_title, timestamp FROM results ORDER BY timestamp DESC";
        try (java.sql.Connection conn = DatabaseHelper.connect();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {

            int index = 0;
            while (rs.next()) {
                index++;
                HBox entryRow = new HBox(20);
                entryRow.setPadding(new Insets(10));
                entryRow.setStyle("-fx-background-color: #16213e; -fx-background-radius: 6;");
                entryRow.setAlignment(Pos.CENTER_LEFT);

                Label nameLbl = new Label("👤 " + rs.getString("student_name"));
                nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
                nameLbl.setPrefWidth(120);

                Label quizLbl = new Label("📝 Quiz: " + rs.getString("quiz_title"));
                quizLbl.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 13px;");
                HBox.setHgrow(quizLbl, Priority.ALWAYS);

                Label scoreLbl = new Label("🏆 " + rs.getInt("score") + " pts");
                scoreLbl.setStyle("-fx-text-fill: #f72585; -fx-font-size: 13px; -fx-font-weight: bold;");

                Label dateLbl = new Label(rs.getString("timestamp").substring(0, 16));
                dateLbl.setStyle("-fx-text-fill: #606080; -fx-font-size: 11px;");

                entryRow.getChildren().addAll(nameLbl, quizLbl, scoreLbl, dateLbl);
                rowsContainer.getChildren().add(entryRow);
            }

            if (index == 0) {
                Label fallback = new Label("No records found yet. Finish a live game session first!");
                fallback.setStyle("-fx-text-fill: #606080; -fx-font-style: italic;");
                rowsContainer.getChildren().add(fallback);
            }
        } catch (Exception ex) {
            Label err = new Label("Error reading log data: " + ex.getMessage());
            err.setStyle("-fx-text-fill: #f72585;");
            rowsContainer.getChildren().add(err);
        }

        ScrollPane scrollablePane = new ScrollPane(rowsContainer);
        scrollablePane.setFitToWidth(true);
        scrollablePane.setPrefHeight(350);
        scrollablePane.setStyle("-fx-background: #1a1a2e; -fx-background-color: transparent;");

        Button closeButton = new Button("Dismiss Log");
        closeButton.setStyle("-fx-background-color: #4361ee; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        closeButton.setOnAction(ev -> popupStage.close());

        layout.getChildren().addAll(scrollablePane, closeButton);

        Scene scene = new Scene(layout, 550, 480);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }
}