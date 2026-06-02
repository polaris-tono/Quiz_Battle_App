package com.example.classroomquizbattleapp;

import com.example.classroomquizbattleapp.ui.TeacherLoginScreen;
import com.example.classroomquizbattleapp.ui.StudentJoinScreen;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        // Role selection screen
        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(60));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label logo = new Label("QB");
        logo.setStyle("-fx-background-color: #4361ee; -fx-text-fill: white; " +
                "-fx-font-size: 40px; -fx-font-weight: bold; " +
                "-fx-padding: 20 35; -fx-background-radius: 16;");

        Label title = new Label("Classroom Quiz Battle");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 30px; -fx-font-weight: bold;");

        Label subtitle = new Label("Select your role to continue");
        subtitle.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 15px;");

        HBox btnRow = new HBox(20);
        btnRow.setAlignment(Pos.CENTER);

        // Teacher Button
        VBox teacherCard = new VBox(10);
        teacherCard.setAlignment(Pos.CENTER);
        teacherCard.setPadding(new Insets(30));
        teacherCard.setPrefWidth(220);
        teacherCard.setStyle("-fx-background-color: #16213e; -fx-background-radius: 16; " +
                "-fx-cursor: hand;");

        Label teacherIcon = new Label("👨‍🏫");
        teacherIcon.setStyle("-fx-font-size: 48px;");

        Label teacherTitle = new Label("I'm a Teacher");
        teacherTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label teacherSub = new Label("Create & host quizzes");
        teacherSub.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");

        teacherCard.getChildren().addAll(teacherIcon, teacherTitle, teacherSub);
        teacherCard.setOnMouseClicked(e -> new TeacherLoginScreen(stage).show());
        teacherCard.setOnMouseEntered(e -> teacherCard.setStyle(
                "-fx-background-color: #4361ee; -fx-background-radius: 16; -fx-cursor: hand;"));
        teacherCard.setOnMouseExited(e -> teacherCard.setStyle(
                "-fx-background-color: #16213e; -fx-background-radius: 16; -fx-cursor: hand;"));

        // Student Button
        VBox studentCard = new VBox(10);
        studentCard.setAlignment(Pos.CENTER);
        studentCard.setPadding(new Insets(30));
        studentCard.setPrefWidth(220);
        studentCard.setStyle("-fx-background-color: #16213e; -fx-background-radius: 16; " +
                "-fx-cursor: hand;");

        Label studentIcon = new Label("🧑‍🎓");
        studentIcon.setStyle("-fx-font-size: 48px;");

        Label studentTitle = new Label("I'm a Student");
        studentTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label studentSub = new Label("Join a quiz session");
        studentSub.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");

        studentCard.getChildren().addAll(studentIcon, studentTitle, studentSub);
        studentCard.setOnMouseClicked(e -> new StudentJoinScreen(stage).show());
        studentCard.setOnMouseEntered(e -> studentCard.setStyle(
                "-fx-background-color: #f72585; -fx-background-radius: 16; -fx-cursor: hand;"));
        studentCard.setOnMouseExited(e -> studentCard.setStyle(
                "-fx-background-color: #16213e; -fx-background-radius: 16; -fx-cursor: hand;"));

        btnRow.getChildren().addAll(teacherCard, studentCard);

        Label footer = new Label("LAN-based · No internet required · Java + JavaFX");
        footer.setStyle("-fx-text-fill: #404060; -fx-font-size: 11px;");

        root.getChildren().addAll(logo, title, subtitle, btnRow, footer);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Classroom Quiz Battle App");
        stage.setScene(scene);
        try {
            stage.getIcons().add(new javafx.scene.image.Image(
                    getClass().getResourceAsStream("/quiz_logo.png")));
        } catch (Exception e) {
            System.out.println("Logo not found!");
        }
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}