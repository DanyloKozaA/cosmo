package com.example.roller.service;
import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MessagingService {

    private final OpenAIService openAIService;

    public MessagingService(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    public String sendMessage(String userMessage) {
        try {
            return openAIService.askGPT(userMessage);
        } catch (IOException e) {
            // Handle the exception, maybe log it
            return "Error occurred while sending the message: " + e.getMessage();
        }
    }
}
