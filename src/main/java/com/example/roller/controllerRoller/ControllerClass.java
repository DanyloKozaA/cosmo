package com.example.roller.controllerRoller;



import com.example.roller.convertor.Convertor;
import com.example.roller.entity.Transaction;
import com.example.roller.service.TesserConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;


@Controller
public class ControllerClass {
    private final Convertor convertor;

    @Autowired
    public ControllerClass(Convertor convertor) {
        this.convertor = convertor;
    }

    @QueryMapping
    public String getAllFiles(@Argument String path) {
        try {
            // Step 1: Extract pages to separate PDF files
            convertor.extractPagesToSeparatePDFs(path);

            // Step 2: Convert all PDFs in the list to images
            convertor.convertPDFsToImages();

            // Get the list of image files
            List<File> imageFilesList = convertor.getImageFilesList();

            // Step 3: Perform OCR on all image files
            TesserConf ocrProcessor = new TesserConf(imageFilesList);
            ocrProcessor.performOCR();

            System.out.println("PDF pages successfully converted to images and text extracted!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "sorted result";
    }

    @MutationMapping
    public Transaction createTransaction(
            @Argument String date,
            @Argument String business,
            @Argument Float price,
            @Argument Float debit,
            @Argument Float credit,
            @Argument String valueDate,
            @Argument Float balance,
            @Argument String TitleId) {
        try {
            LocalDate dateValue = LocalDate.parse(date);
            String businessValue = business;
            float priceValue = price;
            float debitValue = debit;
            float creditValue = credit;
            LocalDate valueDateValue = LocalDate.parse(valueDate);
            float balanceValue = balance;
            String TitleIdValue = TitleId;

            return new Transaction(dateValue.toString(), businessValue, priceValue, debitValue, creditValue, valueDateValue.toString(), balanceValue, TitleIdValue);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating transaction: " + e.getMessage());
        }
    }
}
