package com.example.roller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class RollerApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(RollerApplication.class, args);


       /* MessagingService messagingService = context.getBean(MessagingService.class);

        String response = messagingService.sendMessage("What is the capital of France?");
        System.out.println("GPT Response: " + response);*/
    }

}
