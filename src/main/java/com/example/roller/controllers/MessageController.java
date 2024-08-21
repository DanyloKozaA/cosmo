package com.example.roller.controllers;
import com.example.roller.service.MessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/message")
public class MessageController {

    @Autowired
    private MessagingService messagingService;

    @PostMapping("/send")
    public String sendMessage(@RequestBody String message) {
        return messagingService.sendMessage(message);
    }
}
