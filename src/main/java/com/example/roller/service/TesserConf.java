package com.example.roller.service;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.*;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.lang.model.util.Elements;
import javax.swing.text.Element;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class TesserConf {
    private List<File> imageFilesList;

    public TesserConf(List<File> imageFilesList) {
        this.imageFilesList = imageFilesList;
    }

    public void performOCR() {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("G:\\Tesseract-OCR\\tessdata");
        tesseract.setLanguage("eng");

        for (File imageFile : imageFilesList) {
            try {
                BufferedImage image = ImageIO.read(imageFile);
                String result = tesseract.doOCR(image);
                System.out.println("Text from " + imageFile.getName() + ":");
                System.out.println(result);
                System.out.println("----");
            } catch (TesseractException | IOException e) {
                System.err.println("Error processing file " + imageFile.getName() + ": " + e.getMessage());
            }
        }
    }
}