package com.example.classroomquizbattleapp.server;

import com.example.classroomquizbattleapp.model.Question;
import com.example.classroomquizbattleapp.model.Student;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuizServer {
    private static final int PORT_START = 5000;
    private static final int PORT_END   = 5010;

    // FIX (Issue 5): CopyOnWriteArrayList is thread-safe for the common case of
    // frequent reads (broadcast) and infrequent writes (join/leave), eliminating
    // ConcurrentModificationException.
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    private List<Question> questions;
    private String sessionCode;
    private ServerSocket serverSocket;
    private boolean running = false;
    private int boundPort = PORT_START;

    // FIX (Issue 4): Track the currently active question so late-joining students
    // can receive it immediately upon joining.
    private Question currentQuestion = null;
    private int currentQuestionNumber = 0;

    public QuizServer(List<Question> questions, String sessionCode) {
        this.questions = questions;
        this.sessionCode = sessionCode;
    }

    public void start() {
        running = true;
        new Thread(() -> {
            // FIX (Issue 11): Try ports 5000–5010 instead of failing hard on one port.
            for (int port = PORT_START; port <= PORT_END; port++) {
                try {
                    serverSocket = new ServerSocket(port);
                    boundPort = port;
                    System.out.println("Server started on port " + port);
                    break;
                } catch (IOException e) {
                    System.out.println("Port " + port + " busy, trying next...");
                    if (port == PORT_END) {
                        System.err.println("No available ports in range " + PORT_START + "-" + PORT_END);
                        return;
                    }
                }
            }
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(socket, this);
                    clients.add(handler);
                    new Thread(handler).start();
                    System.out.println("Client connected: " + socket.getInetAddress());
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }
        }).start();
    }

    public void broadcastQuestion(Question q, int questionNumber) {
        // FIX (Issue 4): Store the current question for late joiners.
        this.currentQuestion = q;
        this.currentQuestionNumber = questionNumber;
        broadcast(buildQuestionMessage(q, questionNumber));
    }

    // FIX (Issue 4): Extracted to a reusable method so ClientHandler can call it too.
    public String buildQuestionMessage(Question q, int questionNumber) {
        return "QUESTION|" + questionNumber + "|" + q.getQuestionText() + "|"
                + q.getOptionA() + "|" + q.getOptionB() + "|"
                + q.getOptionC() + "|" + q.getOptionD() + "|"
                + q.getTimeLimit() + "|"
                + (q.getQuestionType() != null ? q.getQuestionType() : "MCQ");
    }

    public void broadcastLeaderboard(List<Student> students) {
        StringBuilder sb = new StringBuilder("LEADERBOARD");
        students.sort(Comparator.comparingInt(Student::getScore).reversed());
        for (Student s : students) {
            sb.append("|").append(s.getName()).append(":").append(s.getScore());
        }
        // Clear current question so late joiners after quiz ends don't get a question.
        currentQuestion = null;
        broadcast(sb.toString());
    }

    public void broadcast(String message) {
        // Safe: CopyOnWriteArrayList iterates a snapshot, no ConcurrentModificationException.
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void stop() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public List<ClientHandler> getClients() { return clients; }
    public List<Question> getQuestions() { return questions; }
    public String getSessionCode() { return sessionCode; }
    public int getBoundPort() { return boundPort; }

    // FIX (Issue 4): Getters for current question state.
    public Question getCurrentQuestion() { return currentQuestion; }
    public int getCurrentQuestionNumber() { return currentQuestionNumber; }

    public List<Student> getStudentList() {
        List<Student> students = new ArrayList<>();
        for (ClientHandler c : clients) {
            if (c.getStudent() != null) students.add(c.getStudent());
        }
        return students;
    }
}
