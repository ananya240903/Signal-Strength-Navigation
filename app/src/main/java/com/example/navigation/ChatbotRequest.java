package com.example.navigation;

public class ChatbotRequest {
    private String message;

    // Constructor
    public ChatbotRequest(String message) {
        this.message = message;
    }

    // Getter
    public String getMessage() {
        return message;
    }

    // Setter
    public void setMessage(String message) {
        this.message = message;
    }
}

