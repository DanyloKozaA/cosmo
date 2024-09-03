package com.example.roller;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SpringBootApplication
@EnableAsync
public class RollerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RollerApplication.class, args);
    }
}
