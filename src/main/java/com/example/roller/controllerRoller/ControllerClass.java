package com.example.roller.controllerRoller;

import com.example.roller.convertor.Convertor;
import com.example.roller.entity.UBS.AllFilesUBS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.io.*;
import java.util.*;


@Controller
public class ControllerClass {
    private final Convertor convertor;
    @Autowired
    public ControllerClass(Convertor convertor) {
        this.convertor = convertor;
    }

    @QueryMapping
    public AllFilesUBS getAllFilesUBS(@Argument String pdf) {
        try {
            // Decode the Base64-encoded PDF
            byte[] pdfBytes = Base64.getDecoder().decode(pdf);

            // Create a temporary file for the PDF
            File tempPdfFile = File.createTempFile("tempPdf", ".pdf");
            tempPdfFile.deleteOnExit(); // Ensure the file is deleted when the JVM exits

            // Write the PDF bytes to the temporary file
            try (OutputStream os = new FileOutputStream(tempPdfFile)) {
                os.write(pdfBytes);
                System.out.println("Temporary PDF file created at: " + tempPdfFile.getAbsolutePath());
            }

            // Pass the temporary file to processFilesUBS
            return convertor.processFilesUBS(tempPdfFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    @QueryMapping
//    public ArrayList<CosmoFile> sort(@Argument String data) {
//
//        ArrayList<CosmoFile> filesList = null;
//        try {
//
//            ObjectMapper objectMapper = new ObjectMapper();
//         objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//            CosmoFile[] cosmoFiles = objectMapper.readValue(data, CosmoFile[].class);
//            ArrayList<CosmoFile> cosmoFilesList = new ArrayList<>(Arrays.asList(cosmoFiles));
//            System.out.println(cosmoFilesList);
//            convertor.sort(cosmoFilesList);
//            return null;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

}