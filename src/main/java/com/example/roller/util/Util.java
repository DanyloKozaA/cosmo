package com.example.roller.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    public static Number getPageNumber(String line) {
        line = line.replaceAll("\\s+", "");;
        String regex = "Page(\\d+)/(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            int currentPage = Integer.parseInt(matcher.group(1));
            return currentPage;
        } else {
            return null;
        }
    }

    public static String getClientNo(String line){
        Pattern pattern = Pattern.compile("Client no\\.\\s*(\\d+-\\d+)");

        // Create a matcher object.
        Matcher matcher = pattern.matcher(line);

        // Check if the pattern matches and extract the client number.
        if (matcher.find()) {
            String clientNumber = matcher.group(1); // The client number is captured in group 1
            System.out.println("Client number: " + clientNumber);
            return clientNumber;
        } else {
            System.out.println("Client number not found.");
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
    public static boolean isTransaction(String input) {
        // Updated regular expression for matching amounts with flexible space placement
        String datePattern = "\\d{2}\\.\\d{2}\\.\\d{2}\\s+.+?\\s+\\d{1,4}(?:\\s\\d{1,3})*(?:\\.\\d{2})?\\s+\\d{2}\\.\\d{2}\\.\\d{2}\\s+\\d{1,4}(?:\\s\\d{1,3})*(?:\\.\\d{2})?";

        // Compile the regex pattern
        Pattern pattern = Pattern.compile(datePattern);

        // Create a matcher object
        Matcher matcher = pattern.matcher(input);
//        System.out.println(input);

        // Check if the string matches the pattern from the beginning
        return matcher.find();
    }


}
