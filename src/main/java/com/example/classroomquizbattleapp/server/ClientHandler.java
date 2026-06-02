package com.example.classroomquizbattleapp.server;

import com.example.classroomquizbattleapp.model.Question;
import com.example.classroomquizbattleapp.model.Student;
import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private QuizServer server;
    private PrintWriter out;
    private BufferedReader in;
    private Student student;
    private String currentAnswer = "";

    public ClientHandler(Socket socket, QuizServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message;
            while ((message = in.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Student disconnected.");
        } finally {
            server.getClients().remove(this);
            // FIX (Issue 6): Always close socket and streams to prevent resource leak.
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                System.out.println("Error closing client resources: " + e.getMessage());
            }
        }
    }

    private void handleMessage(String message) {
        // FIX (Issue 7): Always check array length before indexing to prevent
        // ArrayIndexOutOfBoundsException from malformed messages.
        if (message == null || message.isEmpty()) return;

        String[] parts = message.split("\\|");

        if (message.startsWith("JOIN|")) {
            // FIX (Issue 7): Need at least JOIN|name|code = 3 parts.
            if (parts.length < 3) {
                sendMessage("ERROR|Malformed JOIN message");
                return;
            }
            String name = parts[1].trim();
            String receivedCode = parts[2].trim();

            // FIX (Issue 14): Reject empty names.
            if (name.isEmpty()) {
                sendMessage("ERROR|Name cannot be empty");
                return;
            }

            // FIX (Issue 1): Validate session code on the server side.
            if (!receivedCode.equals(server.getSessionCode())) {
                sendMessage("ERROR|Invalid session code");
                System.out.println("Rejected student '" + name + "' — wrong code: " + receivedCode);
                return;
            }

            student = new Student(name);

            // FIX (Issue 1 / old Issue from report): Send JOINED only to THIS client,
            // not broadcast to everyone. Original code used server.broadcast() which
            // spammed all connected students with each other's join events.
            sendMessage("JOINED|" + name);

            // FIX (Issue 4): If a question is already active, send it immediately
            // to this late-joining student so they aren't stuck on waiting screen.
            Question activeQuestion = server.getCurrentQuestion();
            if (activeQuestion != null) {
                sendMessage(server.buildQuestionMessage(activeQuestion, server.getCurrentQuestionNumber()));
            }

            System.out.println(name + " joined with valid code!");

        } else if (message.startsWith("ANSWER|")) {
            // FIX (Issue 7): Check parts length before accessing index 1.
            if (parts.length >= 2 && !parts[1].trim().isEmpty()) {
                currentAnswer = parts[1].trim();
            }
        }
        // Unknown messages are silently ignored — no crash.
    }

    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }

    public Student getStudent() { return student; }
    public String getCurrentAnswer() { return currentAnswer; }
    public void resetAnswer() { currentAnswer = ""; }
}
