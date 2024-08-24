package com.example.roller.convertor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Convertor {

    public static void convertPDFToJPG(String pdfFilePath, String outputDir) throws IOException {
        File pdfFile = new File(pdfFilePath);
        if (!pdfFile.exists()) {
            throw new IOException("File not found: " + pdfFilePath);
        }

        // Загружаем PDF-документ
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300); // разрешение 300 DPI
                String fileName = getUniqueFileName(outputDir, "page", "jpg");
                ImageIO.write(bim, "jpg", new File(fileName));
            }
        }
    }

    private static String getUniqueFileName(String directory, String baseName, String extension) {
        String fileName = directory + "/" + baseName + "." + extension;
        File file = new File(fileName);
        int count = 1;

        // Если файл с таким именем уже существует, добавляем +1 к имени
        while (file.exists()) {
            fileName = directory + "/" + baseName + "(" + count + ")" + "." + extension;
            file = new File(fileName);
            count++;
        }

        return fileName;
    }

    public static void main(String[] args) {
        try {
            String pdfFilePath = "C:/Users/GameOn/Desktop/AAA/output.pdf";  // Укажите путь к вашему PDF файлу
            String outputDir = "C:/Users/GameOn/Desktop/AAA";  // Укажите путь к директории для сохранения изображений
            convertPDFToJPG(pdfFilePath, outputDir);
            System.out.println("PDF successfully converted to JPG!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}