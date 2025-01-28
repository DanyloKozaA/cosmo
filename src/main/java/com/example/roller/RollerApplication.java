package com.example.roller;

import org.opencv.core.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class RollerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RollerApplication.class, args);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
}
