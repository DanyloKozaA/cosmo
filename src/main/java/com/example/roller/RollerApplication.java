package com.example.roller;

import com.example.roller.controllers.ChatController;
import com.example.roller.service.MessagingService;
import com.example.roller.service.OpenAIService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class RollerApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(RollerApplication.class, args);
        MessagingService messagingService = context.getBean(MessagingService.class);

        String response = messagingService.sendMessage("What is the capital of France?");
        System.out.println("GPT Response: " + response);
    }

}
