package com.example.classroomquizbattleapp.client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class QuizClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageListener listener;
    private String studentName;
    private String sessionCode;

    // FIX (Issue 3): Buffer messages that arrive before the listener is attached,
    // so the JOINED reply is never silently dropped.
    private final List<String> messageBuffer = new ArrayList<>();

    public interface MessageListener {
        void onMessageReceived(String message);
        void onDisconnected();
    }

    // FIX (Issue 1): Accept sessionCode so it can be sent to the server for validation.
    public QuizClient(String studentName, String sessionCode) {
        this.studentName = studentName;
        this.sessionCode = sessionCode;
    }

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // FIX (Issue 1): Include sessionCode in JOIN message.
            out.println("JOIN|" + studentName + "|" + sessionCode);
            startListening();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void startListening() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    // FIX (Issue 3): If listener not attached yet, buffer the message.
                    synchronized (this) {
                        if (listener != null) {
                            listener.onMessageReceived(message);
                        } else {
                            messageBuffer.add(message);
                        }
                    }
                }
            } catch (IOException e) {
                if (listener != null) listener.onDisconnected();
            }
        }).start();
    }

    public void sendAnswer(String answer) {
        // FIX (Issue 14): Don't send empty answers.
        if (out != null && answer != null && !answer.isEmpty()) {
            out.println("ANSWER|" + answer);
        }
    }

    // FIX (Issue 3): When listener is set, flush any buffered messages immediately.
    public synchronized void setMessageListener(MessageListener listener) {
        this.listener = listener;
        for (String msg : messageBuffer) {
            listener.onMessageReceived(msg);
        }
        messageBuffer.clear();
    }

    public void disconnect() {
        try { if (socket != null) socket.close(); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
