package com.example.roller;

import com.example.roller.controllerRoller.ControllerClass;
import com.example.roller.convertor.Convertor;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import nu.pattern.OpenCV;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@SpringBootApplication
@EnableAsync
public class RollerApplication {
    public static void main(String[] args) throws IOException, TesseractException {
        SpringApplication.run(RollerApplication.class, args);
        Convertor convertor = new Convertor();
        ControllerClass controller = new ControllerClass(convertor);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//controller.getAllFiles("C:\\Users\\Danylo\\Downloads\\2024 03 UBS Advices 60R.USD.pdf","UBS");
    controller.getAllFiles("C:\\Users\\Danylo\\Downloads\\2023 06 30 Statement 60P.USD.pdf","UBS");


    }
}
