package com.example.classroomquizbattleapp.database;

import com.example.classroomquizbattleapp.model.Question;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:C:/QuizBattleApp/quiz.db";

    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found!");
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        String createQuizzesTable = "CREATE TABLE IF NOT EXISTS quizzes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL" +
                ");";

        String createQuestionsTable = "CREATE TABLE IF NOT EXISTS questions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "quiz_id INTEGER NOT NULL," +
                "question_text TEXT NOT NULL," +
                "option_a TEXT NOT NULL," +
                "option_b TEXT NOT NULL," +
                "option_c TEXT NOT NULL," +
                "option_d TEXT NOT NULL," +
                "correct_answer TEXT NOT NULL," +
                "time_limit INTEGER NOT NULL," +
                "question_type TEXT NOT NULL," +
                "FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE" +
                ");";

        // CRITICAL UPDATE: Explicitly defining column as quiz_title to match queries
        String createResultsTable = "CREATE TABLE IF NOT EXISTS results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_name TEXT NOT NULL," +
                "score INTEGER NOT NULL," +
                "quiz_title TEXT NOT NULL," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createQuizzesTable);
            stmt.execute(createQuestionsTable);
            stmt.execute(createResultsTable);
            System.out.println("Database tables checked/initialized cleanly.");
        } catch (SQLException e) {
            System.err.println("Initialization error: " + e.getMessage());
        }
    }

    public static int createQuiz(String title) {
        String insertSql = "INSERT INTO quizzes (title) VALUES (?)";
        try (Connection conn = connect()) {
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, title);
                pstmt.executeUpdate();
            }

            String idSql = "SELECT last_insert_rowid()";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(idSql)) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    System.out.println("Successfully created Quiz with real Database ID via rowid: " + newId);
                    return newId;
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to create quiz: " + e.getMessage());
        }
        return -1;
    }

    public static void addQuestion(int quizId, Question q) {
        String sql = "INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer, time_limit, question_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quizId);
            pstmt.setString(2, q.getQuestionText());
            pstmt.setString(3, q.getOptionA());
            pstmt.setString(4, q.getOptionB());
            pstmt.setString(5, q.getOptionC());
            pstmt.setString(6, q.getOptionD());
            pstmt.setString(7, q.getCorrectAnswer());
            pstmt.setInt(8, q.getTimeLimit());
            pstmt.setString(9, q.getQuestionType());

            pstmt.executeUpdate();
            System.out.println("Saved question successfully under Quiz ID: " + quizId);
        } catch (SQLException e) {
            System.err.println("Failed to save question: " + e.getMessage());
        }
    }

    public static List<String[]> getAllQuizzes() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT id, title FROM quizzes ORDER BY id DESC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new String[]{String.valueOf(rs.getInt("id")), rs.getString("title")});
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch quizzes: " + e.getMessage());
        }
        return list;
    }

    public static List<Question> getQuestions(int quizId) {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE quiz_id = ? ORDER BY id ASC";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quizId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Question(
                            rs.getString("question_text"),
                            rs.getString("option_a"),
                            rs.getString("option_b"),
                            rs.getString("option_c"),
                            rs.getString("option_d"),
                            rs.getString("correct_answer"),
                            rs.getInt("time_limit"),
                            rs.getString("question_type")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch questions: " + e.getMessage());
        }
        return list;
    }

    public static void saveResult(String studentName, int score, String quizTitle) {
        String sql = "INSERT INTO results (student_name, score, quiz_title) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentName);
            pstmt.setInt(2, score);
            pstmt.setString(3, quizTitle);

            pstmt.executeUpdate();
            System.out.println("Match result saved successfully for: " + studentName);
        } catch (SQLException e) {
            System.err.println("Failed to save result: " + e.getMessage());
        }
    }
}