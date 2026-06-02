package com.example.classroomquizbattleapp.model;

public class Question {
    public static final String TYPE_MCQ = "MCQ";
    public static final String TYPE_TRUE_FALSE = "TRUE_FALSE";

    private int id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private int timeLimit;
    private String questionType;

    public Question() {}

    public Question(String questionText, String optionA, String optionB,
                    String optionC, String optionD, String correctAnswer,
                    int timeLimit, String questionType) {
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.timeLimit = timeLimit;
        this.questionType = questionType;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String q) { this.questionText = q; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String o) { this.optionA = o; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String o) { this.optionB = o; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String o) { this.optionC = o; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String o) { this.optionD = o; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String a) { this.correctAnswer = a; }
    public int getTimeLimit() { return timeLimit; }
    public void setTimeLimit(int t) { this.timeLimit = t; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String t) { this.questionType = t; }
}