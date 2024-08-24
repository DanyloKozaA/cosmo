package com.example.roller.convertor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Component
public class Convertor {
    private List<File> pdfFilesList = new ArrayList<>();
    private List<File> imageFilesList = new ArrayList<>();

    public void extractPagesToSeparatePDFs(String inputPdfPath) throws IOException {
        File pdfFile = new File(inputPdfPath);
        if (!pdfFile.exists()) {
            throw new IOException("File not found: " + inputPdfPath);
        }

        try (PDDocument document = PDDocument.load(pdfFile)) {
            int totalPages = document.getNumberOfPages();
            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                PDPage page = document.getPage(pageIndex);
                try (PDDocument newDoc = new PDDocument()) {
                    newDoc.addPage(page);
                    File newFile = File.createTempFile("page_" + (pageIndex + 1), ".pdf");
                    newDoc.save(newFile);
                    pdfFilesList.add(newFile);
                    System.out.println("Saved page " + (pageIndex + 1) + " to " + newFile.getAbsolutePath());
                }
            }
        }
    }

    public void convertPDFsToImages() throws IOException {
        for (File pdfFile : pdfFilesList) {
            try (PDDocument document = PDDocument.load(pdfFile)) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                int totalPages = document.getNumberOfPages();
                for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                    try {
                        BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 300, ImageType.RGB);
                        File imageFile = File.createTempFile(pdfFile.getName().replace(".pdf", "_page_" + (pageIndex + 1)), ".jpg");
                        ImageIO.write(image, "jpg", imageFile);
                        imageFilesList.add(imageFile);
                        System.out.println("Saved image " + imageFile.getAbsolutePath());
                    } catch (IOException e) {
                        System.err.println("Error processing page " + (pageIndex + 1) + " of file " + pdfFile.getName() + ": " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading PDF file " + pdfFile.getName() + ": " + e.getMessage());
            }
        }
    }

    public List<File> getPdfFilesList() {
        return pdfFilesList;
    }

    public List<File> getImageFilesList() {
        return imageFilesList;
    }
}

//C:/Users/GameOn/Desktop/AAA/1.pdf