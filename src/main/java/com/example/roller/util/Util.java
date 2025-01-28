package com.example.roller.util;

import com.example.roller.entity.UBS.Transaction;
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

    public static String getMonthAccountStatement(String input){
        try{
            Pattern pattern = Pattern.compile("Monthly: number (\\d+)");
            Matcher matcher = pattern.matcher(input);

            if (matcher.find()) {
                String month = matcher.group(0);
                return month;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
    public static String findDates(String input) {

        String dateRangePattern = "\\d{2}.\\d{2}.\\d{4}.*?(\\d{2}.\\d{2}.\\d{4})";
        Pattern pattern = Pattern.compile(dateRangePattern);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
           return matcher.group(1);
        }

        String dateRangePattern1 = "(\\d{2}.\\d{2}.\\d{4})";
        Pattern pattern1 = Pattern.compile(dateRangePattern1);
        Matcher matcher1 = pattern1.matcher(input);

        while (matcher1.find()) {
            return matcher1.group(1);
        }

      return null;
    }

    public static double StringToNumber(String a){
        a = a.replace("'", "").replace(" ", "");
        double number = Double.parseDouble(a);
        return number;
    }




    public static HashMap getPageInfo(String line) {
        try {
            Pattern pattern = Pattern.compile("Page (\\d+)/(\\d+)");
            Matcher matcher = pattern.matcher(line);

            HashMap <String, String> info = null;

            if (matcher.find()) {
                String currentPage = matcher.group(1);
                String totalPages = matcher.group(2);
                info = new HashMap<>();
                info.put("currentPage",currentPage);
                info.put("totalPages",totalPages);
                return info;
            }

            Pattern secondPattern = Pattern.compile("Form without signature.*(\\d+)/(\\d+)");
            Matcher secondMatcher = secondPattern.matcher(line);

            if (secondMatcher.find()) {
                String currentPage = secondMatcher.group(1);
                String totalPages = secondMatcher.group(2);
                info = new HashMap<>();
                info.put("currentPage",currentPage);
                info.put("totalPages",totalPages);
                return info;
            }

            return null;
        }catch (Exception e){
            System.out.println("error");
            System.out.println(e);
            e.printStackTrace();
            return null;
        }

    }

    public static String getClientNo(String line){
        Pattern pattern = Pattern.compile("Client no\\.\\s*(\\d+-\\d+)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String clientNumber = matcher.group(1).replaceAll("-", "");
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
    public static String getInterest(String line){
        String pattern = "[A-Z]{3}\\s+([\\d\\s,]+\\.\\d{2})";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(line);

        if (matcher.find()) {
           return matcher.group(1);
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
    public static List<File> extractPagesToSeparatePDFs(File decodedPdf) throws IOException {
        List<File> pdfFiles = new ArrayList<>();
        File pdfFile = decodedPdf;
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

    public static String extractAmountAdviceTotalInterest(String input){

        String regex = "(\\d+[\\d\\s]*\\.?\\d*)";


        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(input);

        // Loop to find all matches
        while (matcher.find()) {
            // Remove spaces from the matched number and print it
            String number = matcher.group().replaceAll("\\s", "");
            return number;
        }
        return null;
    }
    public static BufferedImage convertPDFToImage(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            return pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
        }
    }


    public static String extractDateAdvice(String line){
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







    public static Transaction extractTransactionCosmoFile(String line) {
        try {
            String transactionPattern = "(\\d{2}\\.\\d{2}\\.\\d{2}).*?([\\d ]+\\.\\d{2}).*?(\\d{2}\\.\\d{2}\\.\\d{2}).*?([\\d ]+\\.\\d{2})";

            Pattern pattern = Pattern.compile(transactionPattern);
            Matcher matcher = pattern.matcher(line);


            if (matcher.find()) {
                String cashEffect = matcher.group(2);
                String valueDate = matcher.group(3);
                String balance = matcher.group(4).trim();

                String formattedValueDate = formatDate(valueDate);


                Transaction transaction = new Transaction();
                transaction.setAmount(cashEffect.trim());
                transaction.setValueDate(formattedValueDate);
                transaction.setBalance(balance);
                return transaction;
            }


        } catch (Exception e) {
            System.out.println(e + " error");
        }
        return null;
    }

    private static String formatDate(String valueDate) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy");
        Date date = inputFormat.parse(valueDate);
        return outputFormat.format(date);
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

    public static String getIban(String line){
        String regex = "([A-Z]{2}[0-9]{2}(?:\\s?[0-9A-Z]{4})+)\s?([A-Z])";

        Pattern patternDate = Pattern.compile(regex);
        Matcher matcherDate = patternDate.matcher(line);

        if (matcherDate.find()){
            String iban = matcherDate.group(1) +" "+ matcherDate.group(2);
            return iban;
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
            String amount = matcher.group(2);

            return amount.trim();
        }else {
            return null;
        }
    }

    //all numbers    ([\d ]*\d+\.\d+)

    public static Boolean stringStartFromProducedOn(String line){
        String regex = "^Produced on.*";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        if(matcher.matches()){
            return true;
        }else {
            return false;
        }
    }
    public static String extractAdviceTotalInterestAmount(String line){
        String regex = "Total.*interest.*(\\d+).*([A-Z]{3})\\s+([\\d ]*\\d+\\.\\d+)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        if(matcher.find()){
            return matcher.group(3).trim();
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
            String amount = matcher.group(3).trim();
            return amount;
        }else {
           return null;
        }
    }

    public static HashMap extractDataAdviceStatement(String line){
        String regex = "(\\d{2}\\.\\d{2}\\.\\d{4})\\s+[A-Z]{3}\\s+([\\d ]+\\d+\\.\\d{2})";


        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()){
            String date = matcher.group(1);
            String amount = matcher.group(2).trim();
            HashMap<String,String> dateAmount = new HashMap<>();
              dateAmount.put("date",date);
              dateAmount.put("amount",amount);
            return dateAmount;
        }else {
            return null;
        }
    }



    public static HashMap<String, String> getConfirmationOfDelivery(String line) {
        HashMap<String, String> resultMap = new HashMap<>();

        String regex = ".*?Value date\\s*(\\d{2}\\.\\d{2}\\.\\d{4}).*?([A-Z]{3})\\s*([\\d\\s,]+(?:\\.\\d{2})?)";

        String valueDate = null;
        String currency = null;
        String amount = null;

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
             valueDate = matcher.group(1);
             currency = matcher.group(2);
             amount = matcher.group(3).trim();
        }

        String regex1 = ".*?Value date\\s*(\\d{2}\\.\\d{2}\\.\\d{4}).*?([\\d\\s,]+(?:\\.\\d{2})?)";

        Pattern pattern1 = Pattern.compile(regex1);
        Matcher matcher1 = pattern1.matcher(line);

        if (matcher1.find() && amount == null) {
            valueDate = matcher1.group(1);
            amount = matcher1.group(2).trim();
        }

        resultMap.put("valueDate", valueDate);
        resultMap.put("currency", currency);
        resultMap.put("amount", amount);
        return resultMap;
    }

    public static HashMap<String, String> extractDataContractNote(String line) {
        // Updated regex to capture any 3-letter currency code
        String regex = "(In favour of account|To the debit of account).*?Value date\\s*(\\d{2}\\.\\d{2}\\.\\d{4}).*?([A-Z]{3})\\s*([\\d\\s,]+(?:\\.\\d{2})?)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        HashMap<String, String> resultMap = new HashMap<>();

        if (matcher.find()) {
            String valueDate = matcher.group(2); // Capture Value date
            String currency = matcher.group(3); // Capture the currency
            String amount = matcher.group(4).trim(); // Capture and clean the amount

            resultMap.put("valueDate", valueDate);
            resultMap.put("currency", currency);
            resultMap.put("amount", amount);
        }
        return resultMap;
    }

    public static boolean  containsAdviceStatementText(String line){
        String regex = "Advice.*Statement";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()){
            return true;
        }else {
            return false;
        }
    }

    public static String extractAmountConfirmation(String line){
        String regex = "[A-Z]{3}\\s[1-9][0-9]{0,2}(?:,[0-9]{3})*(?:\\.\\d{2})?";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()){
            String amount = matcher.group(0).trim();
            return amount;
        }else {
            return null;
        }
    }


    private static String getMonthNumberFromString(String month) {
        switch (month.toLowerCase()) {
            case "january": return "01";
            case "february": return "02";
            case "march": return "03";
            case "april": return "04";
            case "may": return "05";
            case "june": return "06";
            case "july": return "07";
            case "august": return "08";
            case "september": return "09";
            case "october": return "10";
            case "november": return "11";
            case "december": return "12";
            default: return null; // Handle invalid month
        }
    }


    public static String getSettlementDate(String line) {
        String regex = "\\b(\\d{1,2})\\s+(January|February|March|April|May|June|July|August|September|October|November|December)\\s+(\\d{4})\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String day = matcher.group(1);
            String monthString = matcher.group(2);
            String year = matcher.group(3);
            String monthNumber = getMonthNumberFromString(monthString);
            String formattedDate =   day +"." + monthNumber+"." + year;
            return formattedDate;
        } else {
            return null;
        }
    }


    public static String extractProducedOn(String input) {
        String regex = ".*Produced\\s*on\\s*([12]?[0-9]|3[01])\\s+(January|February|March|April|May|June|July|August|September|October|November|December)\\s+(\\d{4}).*";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input.trim()); // Trim input to remove extra spaces

        if (matcher.find()) {
            String day = matcher.group(1).trim(); // Trim in case of extra spaces
            String month = matcher.group(2).trim();
            String year = matcher.group(3).trim();

            // Add leading zero only if day is a single digit
            if (day.length() == 1) {
                day = "0" + day;
            }

            String monthNumber = getMonthNumberFromString(month);
            return day + "." + monthNumber + "." + year;
        } else {
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
            String amount = matcher.group(4).trim();
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


    public static String extractInterestCalculationAdviceAmount(String line) {
        try {
            String advicePattern = "^(?!\\d{2}\\.\\d{2}\\.\\d{2})(\\D+\\s{2,})\\s*(\\d{2}\\.\\d{2}\\.\\d{2})\\s+([\\d ]*\\d+\\.\\d+)";
            Pattern pattern = Pattern.compile(advicePattern);
            Matcher mather = pattern.matcher(line);
            if (mather.find()) {
                String amount = mather.group(3).trim();
                return amount;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

}
