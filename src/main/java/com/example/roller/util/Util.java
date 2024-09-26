package com.example.roller.util;

import com.example.roller.entity.Advice;
import com.example.roller.entity.Transaction;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
            HashMap <String, String>info = new HashMap<>();
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
        line = line.replaceAll("\\s+", "");
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


    public static String extractFiterforDebitAdvice(String line){
        String regex = "(\\D{5}\\s{1,3}\\D{6}\\s+\\D{8}\\s{1,3}\\D{2})\\s+(\\d{1,2}\\s{1,3}\\D+\\s{1,3}\\d{4})";
        Pattern patternDate = Pattern.compile(regex);
        Matcher matcherDate = patternDate.matcher(line);

        if (matcherDate.find()){
            String date = matcherDate.group(2);
            return date;
        }else {
            return null;
        }
    }


    public static Transaction notOrdinaryTransaction(String line){
        String regex = "(.{2}\\..{2}\\..{2})[\\D ]+\\s*\\.*\\D*\\s*([\\d ]*.+\\..{2})\\s*\\.?.*\\D*\\s*(.{2}\\..{2}\\..{2})\\s*\\.*\\D*\\s*([\\d ]+.+\\..{2})";
        Pattern patternDate = Pattern.compile(regex);
        Matcher matcherDate = patternDate.matcher(line);

        if (matcherDate.find()){
            String cashEffect = matcherDate.group(2).replace(" ", "");
            String valueDate = matcherDate.group(3).replace(" ", "");
            String balance = matcherDate.group(4).replace(" ", "");

            Transaction transaction = new Transaction();
            transaction.setCashEffect(cashEffect);
            transaction.setValueDate(valueDate);
            transaction.setBalance(balance);

            return transaction;
        }else {
            return null;
        }
    }



    public static Transaction extractTransactions(String line) {
        String transactionPattern = "(\\d{2}\\.\\d{2}\\.\\d{2})[\\D ]+\\s*\\.*\\D*\\s*([\\d ]*\\d+\\.\\d{2})\\s*\\.*\\D*\\s*(\\d{2}\\.\\d{2}\\.\\d{2})\\s*\\.*\\D*\\s*([\\d ]+\\d{3}\\.\\d{2})";
        Pattern pattern = Pattern.compile(transactionPattern);
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String cashEffect = matcher.group(2).replace(" ", "");
            String valueDate = matcher.group(3).replace(" ", "");
            String balance = matcher.group(4).replace(" ", "");

            Transaction transaction = new Transaction();
            transaction.setCashEffect(cashEffect);
            transaction.setValueDate(valueDate);
            transaction.setBalance(balance);


            System.out.println("First Value: " + cashEffect);
            System.out.println("Second Date: " + valueDate);
            System.out.println("Second Value: " + balance);
            return transaction;
        } else {
            return null;
        }
    }



    public static String filterForConfStandard(String line){
        String regex = "(\\w{8}\\s{1,2}\\w{5}\\s{1,2}.\\w{7}.\\s{1,3}\\w{8}\\s{1,3}\\w{6})";
        Pattern patternDate = Pattern.compile(regex);
        Matcher matcherDate = patternDate.matcher(line);

        if (matcherDate.find()){
            String date = matcherDate.group(1);
            return date;
        }else {
            return null;
        }
    }


    public static String filterForConfMetal(String line){
        String regex = "(\\w{8}\\s{1,2}\\w{5}\\s{1,2}.\\w{7}.\\s{1,3}\\w{4})";
        Pattern patternDate = Pattern.compile(regex);
        Matcher matcherDate = patternDate.matcher(line);

        if (matcherDate.find()){
            String date = matcherDate.group(1);
            return date;
        }else {
            return null;
        }
    }



    public static String filterForConfFX(String line){
        String regex = "^(\\D{2}\\s{1,3}\\D{7})";
        Pattern patternDate = Pattern.compile(regex);
        Matcher matcherDate = patternDate.matcher(line);

        if (matcherDate.find()){
            String date = matcherDate.group(1);
            return date;
        }else {
            return null;
        }
    }



    public static String extractFiterforDateWithNote(String line){
        String regex = "(\\D{8}\\s{1,3}\\D{4}\\s+\\D+[\\s]*)\\s+(\\d{1,2}\\s{1,3}\\D+\\s{1,3}\\d{4})";
        Pattern patternDate = Pattern.compile(regex);
        Matcher matcherDate = patternDate.matcher(line);

        if (matcherDate.find()){
            String date = matcherDate.group(2);
            return date;
        }else {
            return null;
        }
    }



    public static String extractAdviceDate(String line){
        String regex = "(\\D{2}\\s{1,3}\\D{2})\\s{1,3}(\\d{2}\\.\\d{2}\\.\\d{4})";
        Pattern patternDate = Pattern.compile(regex);
        Matcher matcherDate = patternDate.matcher(line);

        if (matcherDate.find()){
            String date = matcherDate.group(2);

            return date;
        }else {
            return null;
        }
    }


    public static String extractAdviceConfMetalAmount(String line){

        String regex = "(\\w{11}\\s{1,2}\\w{8}\\s{1,3}\\w{6}\\s+\\W\\s{1,2}\\w{3}\\s{1,2})([\\d,]*\\d+\\.\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()){
            String amount = matcher.group(2).replace(",","");

            return amount;
        }else {
            return null;
        }
    }


    public static String extractAdviceDebitAdviceAmount(String line){

        String regex = "(\\D{5}\\s{1,3}\\D{6}\\s+\\D{3})\\s+([\\d ]*\\d+\\.\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()){
            String amount = matcher.group(2).replace(" ","");

            return amount;
        }else {
            return null;
        }
    }


    public static String extractAdviceDebitAdviceDate(String line){

        String regex = "(\\D{3}\\W\\s+)(\\d{2}\\.\\d{2}\\.\\d{4})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()){
            String date = matcher.group(2);
            return date;
        }else {
            return null;
        }
    }



    public static String extractAdviceClosServPriceAmount(String line){

        String regex = "(\\D{7}\\s{1,3}\\D{2}\\s{1,3}\\D{7}\\s{1,3}\\D{2}\\s{1,3}\\D{7}\\s{1,3}\\D{6})\\s+([A-Z]{3})\\s+([\\d ]*\\d+\\.\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()){
            String amount = matcher.group(3).replace(" ","");
            return amount;
        }else {
           return null;
        }
    }

    public static HashMap extractAdviceStatement(String line){
        String regex = "(\\D{6}\\s{1,3}\\D{7}\\s{1,4}\\d{3}\\W\\d{6}\\.\\d{2}\\D)\\s+\\D{5}\\s{1,4}(\\d{2}\\.\\d{2}\\.\\d{4})\\s+[A-Z]\\D{3}\\s+([\\d ]*\\d+\\.\\d{1,2})";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()){
            String date = matcher.group(2);
            String amount = matcher.group(3).replace(" ","");
            HashMap<String,String> dateAmount = new HashMap<>();
              dateAmount.put("date",date);
              dateAmount.put("amount",amount);
            return dateAmount;
        }else {
            return null;
        }
    }

    public static HashMap extractAdviceCotractNote(String line){
        String regex = "(\\D{5}\\s{1,3}\\D{4}\\s{1,3})(\\d{2}\\.\\d{2}\\.\\d{4})\\s+[A-Z]\\D{3}\\s+([\\d ]*\\d+\\.\\d{1,2})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()){
            String date = matcher.group(2);
            String amount = matcher.group(3).replace(" ","");
            HashMap<String,String> dateAmount = new HashMap<>();
            dateAmount.put("date",date);
            dateAmount.put("amount",amount);
             return dateAmount;
        }else {
        return null;
    }
     }


    public static String extractAdviceConfarmationMetalData(String line){
        String regex = "(\\w{10}\\s{1,2}\\w{4}\\s+.)\\s+(\\d{1,2})\\s{1,3}(\\w+)\\s{1,3}(\\d{4})";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String data;
            String month = filterForDate(matcher.group(3).trim().toLowerCase(Locale.ROOT));
            if (month != null) {
                if(matcher.group(2).length()<2){
                    String zero = "0" + matcher.group(2);
                    data = zero + "." + month + "." + matcher.group(4);
                    return data;
                }else {
                    data = matcher.group(2) + "." + month + "." + matcher.group(4);
                    return data;
                }
            }else {
                return null;
            }
        }else {
            return null;
        }
    }


    public static String filterForDate(String date){
        switch (date) {
            case "january":
                date = "01";
                break;
            case "february":
                date = "02";
                break;
            case "march":
                date = "03";
                break;
            case "april":
                date = "04";
                break;
            case "may":
                date = "05";
                break;
            case "june":
                date = "06";
                break;
            case "july":
                date = "07";
                break;
            case "august":
                date = "08";
                break;
            case "september":
                date = "09";
                break;
            case "october":
                date = "10";
                break;
            case "november":
                date = "11";
                break;
            case "december":
                date = "12";
                break;
            default:
                date = null;
        }
        return date;
    }



    public static String extractAdviceConfarmationData(String line) {
        String regex = "(\\D{7}\\s{1,3}\\D{7}\\s{1,3}\\D{4})\\s+\\W\\s*(\\d{1,2})\\s*(\\D+)\\s*(\\d{4})";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String data;
            String month = filterForDate(matcher.group(3).trim().toLowerCase(Locale.ROOT));
            if (month != null) {
                if(matcher.group(2).length()<2){
                    String zero = "0" + matcher.group(2);
                    data = zero + "." + month + "." + matcher.group(4);
                    return data;
                }else {
                    data = matcher.group(2) + "." + month + "." + matcher.group(4);
                    return data;
                }
            }else {
                return null;
            }
        }else {
            return null;
        }

    }



    public static String extractAdviceConfirmationAmount(String line){
        String regex = "^(\\D{7}\\s+\\W\\s{1,3}[A-Z]\\D{3})\\s*([\\d, ]*\\d+\\.\\d+)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()){
            String amount = matcher.group(2).replace(",","");
            return amount;
        }else {
            return null;
        }
    }



    public static String extractAdviceConfirmationFXAmount(String line){
        String regex = "(\\D{6}\\s{1,3}\\D{3}\\s{1,3}\\D{8}\\s{1,3}\\D{7}\\s{1,3}\\D{2}\\s{1,3}\\D{12}\\s+.\\s{1,3}\\D{3})\\s{1,3}([\\d, ]*\\d+\\.\\d+)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()){
            String amount = matcher.group(2).replace(",","");
            return amount;
        }else {
            return null;
        }
    }



    public static String extractAdviceMandateFeeAmount(String line){
        String regex = "(\\D{5}\\s{1,3}\\D{7}\\s{1,3}\\D{3}\\s+[\\d ]*\\d+\\.\\d+\\s*\\D{3}[\\d ]*\\d+\\.\\d+\\s*\\D{3})\\s+([\\d ]*\\d+\\.\\d+)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()){
            String amount = matcher.group(2).replace(" ","");
            return amount;
        }else {
            return null;
        }
    }



    public static String extractAdviceCustodyFeeAmount(String line){
        String regex = "(\\D{5}\\s{1,3}\\D{7}\\s{1,3}\\D{3})\\s+([\\d ]*\\d+\\.\\d+\\s{1,3}[A-Z]\\D{3})\\s+([\\d ]*\\d+\\.\\d+\\s{1,3}[A-Z]\\D{3})\\s+([\\d ]*\\d+\\.\\d+)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()){
            String amount = matcher.group(4).replace(" ","");
            return amount;
        }else {
            return null;
        }
    }


    public static String extractAdviceCustodyFeeDate(String line){
        String regex = "(\\D{9}\\s{1,3}\\D{2})\\s{1,3}(\\d{2}\\.\\d{2}\\.\\d{4})[\\s]*\\W[\\s]*(\\d{2}\\.\\d{2}\\.\\d{4})";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()){
            String date = matcher.group(3);
            return date;
        }else {
            return null;
        }
    }



    public static HashMap extractAdviceCalculator(String line){

        String advicePattern = "^(?!\\d{2}\\.\\d{2}\\.\\d{2})(\\D+\\s{2,})\\s*(\\d{2}\\.\\d{2}\\.\\d{2})\\s+([\\d ]*\\d+\\.\\d+)";
        Pattern pattern = Pattern.compile(advicePattern);
        Matcher mather = pattern.matcher(line);
        if(mather.find()){
            String amount = mather.group(3).replace(" ", "");
            String valueDate = mather.group(2);
            HashMap dateAmount = new HashMap();
            dateAmount.put("amount", amount);
            dateAmount.put("date", valueDate);

            System.out.println("Advice work!");
            return dateAmount;
        }else {
             return null;
        }

    }

}
