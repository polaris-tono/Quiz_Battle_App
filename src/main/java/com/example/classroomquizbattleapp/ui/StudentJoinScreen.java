package com.example.classroomquizbattleapp.ui;

import com.example.classroomquizbattleapp.client.QuizClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class StudentJoinScreen {
    private Stage stage;

    public StudentJoinScreen(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(60));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label logo = new Label("QB");
        logo.setStyle("-fx-background-color: #f72585; -fx-text-fill: white; " +
                "-fx-font-size: 32px; -fx-font-weight: bold; " +
                "-fx-padding: 15 25; -fx-background-radius: 12;");

        Label title = new Label("Join a Quiz");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");

        Label subtitle = new Label("Enter the code your teacher shared");
        subtitle.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 14px;");

        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(400);
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 16;");

        Label nameLabel = new Label("Your Display Name");
        nameLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Ali Raza");
        nameField.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: #606080; -fx-padding: 12; " +
                "-fx-background-radius: 8; -fx-font-size: 14px;");

        Label codeLabel = new Label("Session Code");
        codeLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");
        codeLabel.setMaxWidth(Double.MAX_VALUE);

        TextField codeField = new TextField();
        codeField.setPromptText("e.g. 4829");
        codeField.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: #606080; -fx-padding: 12; " +
                "-fx-background-radius: 8; -fx-font-size: 20px; -fx-alignment: center;");

        Label hostLabel = new Label("Teacher's PC IP (leave blank for same PC)");
        hostLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");
        hostLabel.setMaxWidth(Double.MAX_VALUE);

        TextField hostField = new TextField("localhost");
        hostField.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; " +
                "-fx-padding: 12; -fx-background-radius: 8; -fx-font-size: 14px;");

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 12px;");

        Button joinBtn = new Button("Join Session");
        joinBtn.setMaxWidth(Double.MAX_VALUE);
        joinBtn.setStyle("-fx-background-color: #f72585; -fx-text-fill: white; " +
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 13; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");

        joinBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String code = codeField.getText().trim();
            String host = hostField.getText().trim();

            // FIX (Issue 14): Client-side validation before even attempting connection.
            if (name.isEmpty()) { errorLabel.setText("Please enter your name!"); return; }
            if (code.isEmpty()) { errorLabel.setText("Please enter session code!"); return; }
            if (host.isEmpty()) host = "localhost";

            // FIX (Issue 1): Pass sessionCode to QuizClient so it's sent in the JOIN message.
            QuizClient client = new QuizClient(name, code);

            // FIX (Issue 3): Build the screen and attach the listener BEFORE connecting,
            // so no server messages (like JOINED or ERROR) are dropped due to race condition.
            LiveQuizStudentScreen screen = new LiveQuizStudentScreen(stage, client, name);
            screen.attachListener();

            boolean connected = client.connect(host, 5000);

            if (connected) {
                screen.show();
            } else {
                errorLabel.setText("Could not connect! Check IP and try again.");
            }
        });

        card.getChildren().addAll(nameLabel, nameField, codeLabel,
                codeField, hostLabel, hostField, errorLabel, joinBtn);

        root.getChildren().addAll(logo, title, subtitle, card);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Quiz Battle — Student Join");
        stage.setScene(scene);
        stage.show();
    }
}
