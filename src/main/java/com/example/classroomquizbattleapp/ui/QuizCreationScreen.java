package com.example.classroomquizbattleapp.ui;

import com.example.classroomquizbattleapp.database.DatabaseHelper;
import com.example.classroomquizbattleapp.model.Question;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class QuizCreationScreen {
    private Stage stage;
    private List<Question> questions = new ArrayList<>();

    public QuizCreationScreen(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Top Bar
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #16213e;");

        Button backBtn = new Button("← Back");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0a0b0; -fx-font-size: 14px; -fx-cursor: hand;");
        backBtn.setOnAction(e -> new TeacherDashboard(stage).show());

        Label title = new Label("Create New Quiz");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        topBar.getChildren().addAll(backBtn, title);
        root.setTop(topBar);

        // Main Content
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        // Quiz Title Field
        Label quizTitleLabel = new Label("Quiz Title");
        quizTitleLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 13px;");
        TextField quizTitleField = new TextField();
        quizTitleField.setPromptText("e.g. Chapter 5 — OOP Concepts");
        quizTitleField.setMaxWidth(500);
        quizTitleField.setStyle("-fx-background-color: #16213e; -fx-text-fill: white; -fx-padding: 12; -fx-background-radius: 8; -fx-font-size: 14px;");

        // Question Type Selector
        Label typeLabel = new Label("Question Type");
        typeLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 13px;");

        HBox typeRow = new HBox(10);
        ToggleGroup typeGroup = new ToggleGroup();

        ToggleButton mcqBtn = makeTypeBtn("MCQ", typeGroup);
        ToggleButton tfBtn = makeTypeBtn("True / False", typeGroup);
        mcqBtn.setSelected(true);

        typeRow.getChildren().addAll(mcqBtn, tfBtn);

        // Question Form Card
        VBox questionCard = new VBox(12);
        questionCard.setPadding(new Insets(20));
        questionCard.setMaxWidth(600);
        questionCard.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12;");

        Label addQLabel = new Label("Add Question");
        addQLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        TextField questionField = makeField("Question text");

        // MCQ Options
        VBox mcqOptions = new VBox(8);
        TextField optAField = makeField("Option A");
        TextField optBField = makeField("Option B");
        TextField optCField = makeField("Option C");
        TextField optDField = makeField("Option D");

        Label correctLabelMCQ = new Label("Correct Answer");
        correctLabelMCQ.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");
        ComboBox<String> correctBoxMCQ = new ComboBox<>();
        correctBoxMCQ.getItems().addAll("A", "B", "C", "D");
        correctBoxMCQ.setValue("A");
        correctBoxMCQ.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white;");

        mcqOptions.getChildren().addAll(optAField, optBField, optCField, optDField, correctLabelMCQ, correctBoxMCQ);

        // True/False Options
        VBox tfOptions = new VBox(8);
        tfOptions.setVisible(false);
        tfOptions.setManaged(false);

        Label tfCorrectLabel = new Label("Correct Answer");
        tfCorrectLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");
        ComboBox<String> correctBoxTF = new ComboBox<>();
        correctBoxTF.getItems().addAll("True", "False");
        correctBoxTF.setValue("True");
        correctBoxTF.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white;");

        Label tfInfo = new Label("Students will choose between True and False.");
        tfInfo.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");

        tfOptions.getChildren().addAll(tfInfo, tfCorrectLabel, correctBoxTF);

        // Toggle between MCQ and True/False
        mcqBtn.setOnAction(e -> {
            mcqOptions.setVisible(true);
            mcqOptions.setManaged(true);
            tfOptions.setVisible(false);
            tfOptions.setManaged(false);
        });

        tfBtn.setOnAction(e -> {
            mcqOptions.setVisible(false);
            mcqOptions.setManaged(false);
            tfOptions.setVisible(true);
            tfOptions.setManaged(true);
        });

        // Time Limit
        Label timeLimitLabel = new Label("Time Limit (seconds)");
        timeLimitLabel.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");
        ComboBox<Integer> timeBox = new ComboBox<>();
        timeBox.getItems().addAll(10, 15, 20, 30, 45, 60);
        timeBox.setValue(30);
        timeBox.setStyle("-fx-background-color: #0f3460;");

        // Question count label
        Label countLabel = new Label("Questions added: 0");
        countLabel.setStyle("-fx-text-fill: #4361ee; -fx-font-size: 13px;");

        // Buttons
        HBox btnRow = new HBox(10);
        Button addQBtn = new Button("+ Add Question");
        addQBtn.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");

        Button saveQuizBtn = new Button("Save Quiz ✓");
        saveQuizBtn.setStyle("-fx-background-color: #4361ee; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");

        addQBtn.setOnAction(e -> {
            String qText = questionField.getText().trim();
            if (qText.isEmpty()) { showAlert("Please write the question text!"); return; }

            Question q;
            if (tfBtn.isSelected()) {
                String correct = correctBoxTF.getValue().equals("True") ? "A" : "B";
                q = new Question(qText, "True", "False", "-", "-", correct, timeBox.getValue(), "TRUE_FALSE");
            } else {
                String a = optAField.getText().trim();
                String b = optBField.getText().trim();
                if (a.isEmpty() || b.isEmpty()) { showAlert("Please fill at least Option A and B!"); return; }
                String c = optCField.getText().trim().isEmpty() ? "-" : optCField.getText().trim();
                String d = optDField.getText().trim().isEmpty() ? "-" : optDField.getText().trim();
                q = new Question(qText, a, b, c, d, correctBoxMCQ.getValue(), timeBox.getValue(), "MCQ");
            }

            questions.add(q);
            countLabel.setText("Questions added: " + questions.size());
            questionField.clear();
            optAField.clear(); optBField.clear(); optCField.clear(); optDField.clear();
            correctBoxMCQ.setValue("A");
            correctBoxTF.setValue("True");
        });

        saveQuizBtn.setOnAction(e -> {
            String qTitle = quizTitleField.getText().trim();
            if (qTitle.isEmpty()) { showAlert("Please enter a quiz title!"); return; }
            if (questions.isEmpty()) { showAlert("Please add at least one question!"); return; }

            // Create the quiz entry and secure the auto-generated database ID
            int quizId = DatabaseHelper.createQuiz(qTitle);

            // Save each item attached directly to this secure ID
            for (Question q : questions) {
                DatabaseHelper.addQuestion(quizId, q);
            }

            showAlert("Quiz saved! (" + questions.size() + " questions)");
            new TeacherDashboard(stage).show();
        });

        btnRow.getChildren().addAll(addQBtn, saveQuizBtn);

        questionCard.getChildren().addAll(
                addQLabel, questionField,
                typeLabel, typeRow,
                mcqOptions, tfOptions,
                timeLimitLabel, timeBox,
                countLabel, btnRow
        );

        content.getChildren().addAll(quizTitleLabel, quizTitleField, questionCard);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #1a1a2e;");
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Quiz Battle — Create Quiz");
        stage.setScene(scene);
        stage.show();
    }

    private ToggleButton makeTypeBtn(String text, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(group);
        btn.setStyle("-fx-background-color: #0f3460; -fx-text-fill: #a0a0b0; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");
        btn.selectedProperty().addListener((obs, old, selected) -> {
            if (selected) btn.setStyle("-fx-background-color: #4361ee; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");
            else btn.setStyle("-fx-background-color: #0f3460; -fx-text-fill: #a0a0b0; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");
        });
        return btn;
    }

    private TextField makeField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-prompt-text-fill: #606080; -fx-padding: 10; -fx-background-radius: 6; -fx-font-size: 13px;");
        return field;
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}