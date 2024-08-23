/*package com.example.roller.controllers;
import com.example.roller.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    @Autowired
    private OpenAIService openAIService;

    @PostMapping("/ask")
    public String askGPT(@RequestBody String prompt) {
        try {
            return openAIService.askGPT(prompt);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}*/
