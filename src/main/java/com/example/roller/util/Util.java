package com.example.roller.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static List<String> findDates(String input) {
        List<String> dates = new ArrayList<>();

        // Regex pattern to match dates in the format dd.MM.yyyy or dd.MM.yyyy-dd.MM.yyyy
        String datePattern = "\\b\\d{2}\\.\\d{2}\\.\\d{4}(?:-\\d{2}\\.\\d{2}\\.\\d{4})?\\b";
        Pattern pattern = Pattern.compile(datePattern);
        Matcher matcher = pattern.matcher(input);

        // Find all matches and add them to the list
        while (matcher.find()) {
            dates.add(matcher.group());
        }

        return dates;
    }

    public static double StringToNumber(String a){
        a = a.replace("'", "").replace(" ", "");
        double number = Double.parseDouble(a);
        return number;
    }
    public static Date StringToDate(String dateStr){
        String pattern = "dd-MM-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        try {
            // Преобразуем строку в дату
            Date date = simpleDateFormat.parse(dateStr);
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String removeUntilLetterOrNumber(String input) {
        // Using regular expression to replace everything until the first letter or number
        return input.replaceAll("^[^a-zA-Z0-9]+", "");
    }

    public static HashMap getPageInfo(String line) {
        Pattern pattern = Pattern.compile("Page (\\d+)/(\\d+)");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String currentPage = matcher.group(1);
            String totalPages = matcher.group(2);
            HashMap info = new HashMap<>();
            info.put("currentPage",currentPage);
            info.put("totalPages",totalPages);
            return info;
        }
        return null;
    }

    public static String getClientNo(String line){
        Pattern pattern = Pattern.compile("Client no\\.\\s*(\\d+-\\d+)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String clientNumber = matcher.group(1);
            return clientNumber;
        } else {
            return null;
        }
    }


    public int findLineIndex(String text,String line){
        String[] lines = text.split("\n");

        // Find the position of "Beneficiary"
        int position = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(line)) {
                position = i;
                break;
            }
        }

        // Output the result
        if (position != -1) {
           return position;
        } else {
            return -1;
        }
    }
    public static Number getMaxPageNumber(String line){
        line = line.replaceAll("\\s+", "");;
        String regex = "Page(\\d+)/(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            int totalPages = Integer.parseInt(matcher.group(2));
            return totalPages;
        } else {
            return null;
        }
    }
    public static String extractValue(String line, String key) {
        if (line.contains(key)) {
            return line.split(key)[1].trim();
        }
        return "";
    }

    public static String extractAmount(String line) {
        // This method assumes that amounts are always preceded by "USD"
        if (line.contains("USD")) {
            String[] parts = line.split("USD");
            return parts.length > 1 ? parts[1].trim().split(" ")[0] : "";
        }
        return "";
    }

    public static BufferedImage binarizeImage(BufferedImage image) {
        BufferedImage grayscaleImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (r + g + b) / 3; // Simple grayscale conversion
                int grayRGB = (gray << 16) | (gray << 8) | gray; // Convert back to RGB
                grayscaleImage.setRGB(x, y, grayRGB);
            }
        }


        int threshold = 230; // You can adjust this threshold value
        BufferedImage binaryImage = new BufferedImage(grayscaleImage.getWidth(), grayscaleImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < grayscaleImage.getHeight(); y++) {
            for (int x = 0; x < grayscaleImage.getWidth(); x++) {
                int gray = grayscaleImage.getRGB(x, y) & 0xFF; // Extract gray value
                if (gray < threshold) {
                    binaryImage.setRGB(x, y, 0x000000); // Set to black
                } else {
                    binaryImage.setRGB(x, y, 0xFFFFFF); // Set to white
                }
            }
        }
        return binaryImage;
    }
    public static List<File> extractPagesToSeparatePDFs(String inputPdfPath) throws IOException {
        List<File> pdfFiles = new ArrayList<>();
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
                    pdfFiles.add(newFile);
                }
            }
        }
        return pdfFiles;
    }

    public static BufferedImage convertPDFToImage(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            return pdfRenderer.renderImageWithDPI(0, 800, ImageType.RGB);
        }
    }

}
