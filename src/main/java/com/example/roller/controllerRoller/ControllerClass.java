package com.example.roller.controllerRoller;



import com.amazonaws.services.s3.transfer.Upload;
import com.example.roller.convertor.Convertor;
import com.example.roller.entity.CosmoFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencv.core.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;


@Controller
public class ControllerClass {
    private final Convertor convertor;
    @Autowired
    public ControllerClass(Convertor convertor) {
        this.convertor = convertor;
    }

    @QueryMapping
    public String getAllFiles(@Argument String pdf, @Argument String bankName) {
        try {
            System.out.println("getAllFiles");
            byte[] pdfBytes = Base64.getDecoder().decode(pdf);

            String filePath = "generated_output.pdf";
            File pdfFile = new File(filePath);

            try (OutputStream os = new FileOutputStream(pdfFile)) {
                os.write(pdfBytes);
                System.out.println("PDF file has been successfully created at: " + pdfFile.getAbsolutePath());
            }
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(convertor.processFiles(pdfFile, bankName));

        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @QueryMapping
    public ArrayList<CosmoFile> sort(@Argument ArrayList<CosmoFile> filesList) {
        System.out.println("dd");
        try {
            return convertor.sort(filesList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}