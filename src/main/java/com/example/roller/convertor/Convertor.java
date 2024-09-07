package com.example.roller.convertor;

import com.example.roller.util.Util;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class Convertor {

    public Convertor() {
    }

    int dfg = 0;

    public void processFiles(String inputPdfPath, String bankName) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//        try {
//            Directory directory = FSDirectory.open(Files.createTempDirectory("temp"));
//            InputStream affFileStream = new FileInputStream("src/main/resources/hunspell/en_US.aff");
//
//            InputStream dicFileStream = new FileInputStream("src/main/resources/hunspell/en_US.dic");
//            Dictionary dictionary = new Dictionary(directory, "spellCheck", affFileStream, dicFileStream);
//
//            Hunspell spellChecker = new Hunspell(dictionary);
//
//            String correctWord = "guava";
//            String misspelledWord = "recieve";
//
//            System.out.println(String.format("Is %s spelled correctly?: %b", correctWord, spellChecker.spell(correctWord)));
//            System.out.println(String.format("Is %s spelled correctly?: %b", misspelledWord, spellChecker.spell(misspelledWord)));
//            System.out.println(String.format("Did you mean: %s", spellChecker.suggest(misspelledWord)));
//        } catch (IOException e) {
//        System.out.println(e);
//        e.printStackTrace();
//    } catch (ParseException e) {
//            throw new RuntimeException(e);
//        }


        List<File> pdfList = Util.extractPagesToSeparatePDFs(inputPdfPath);
        ExecutorService executorService = Executors.newFixedThreadPool(5);


        for (int i = 0; i < pdfList.size(); i++) {
            File pdf = pdfList.get(i);
            int pageIndex = i + 1; //
            executorService.submit(() -> {
                try {
                    Tesseract threadTesseract = new Tesseract();
                    threadTesseract.setOcrEngineMode(1);
                    threadTesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
                    threadTesseract.setLanguage("eng");
                    threadTesseract.setTessVariable("preserve_interword_spaces", "1");


                    BufferedImage image = Util.convertPDFToImage(pdf);
                    File outputFile = new File("C:\\Users\\Danylo\\Downloads\\outputdfgdfg" + pageIndex + ".png");
                    image = Util.binarizeImage(image);
                    ImageIO.write(image, "png", outputFile);

                    String text = threadTesseract.doOCR(image);

//                    System.out.println(Util.getPageNumber(text));

                    if ("UBS".equals(bankName)) {
                        processTextUBS(text);
                    }


                } catch (IOException | TesseractException e) {
                    System.out.println(e);
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.out.println(e);
            executorService.shutdownNow();
        }
    }


      public static String extractTransactions(String line) {
//          System.out.println(line);
        String transactionPattern = "(\\d{2}\\.\\d{2}\\.\\d{2})\\s+.*?\\s+([\\d,]+\\.\\d{2})\\s*[^\\w]*\\s*(\\d{2}\\.\\d{2}\\.\\d{2})\\s+([\\d ]+\\d{3}\\.\\d{2})";

        Pattern pattern = Pattern.compile(transactionPattern);
        Matcher matcher = pattern.matcher(line);

        if (matcher.matches()) {
//            System.out.println("Matched line: " + line);

            // Optionally, extract the matched groups
            String date1 = matcher.group(1);
            String value1 = matcher.group(2);
            String date2 = matcher.group(3);
            String value2 = matcher.group(4);

//            // Print extracted groups (or use them as needed)
//            System.out.println("First Date: " + date1);
//            System.out.println("First Value: " + value1);
//            System.out.println("Second Date: " + date2);
//            System.out.println("Second Value: " + value2);
        } else {
//            System.out.println("No match found for the line.");
        }

        return line;
    }





    private void processTextUBS(String text) throws IOException {
    List lines = Arrays.stream(text.split("\\n")).toList();

   ArrayList<String> arrayList = new ArrayList<>(lines);
        // Loop through each line


        String currentPage = null;
        String totalPages = null;
        String clientNo = null;

        for (int i = 0; i < arrayList.size(); i++) {
            String line = arrayList.get(i).replaceAll("[^a-zA-Z0-9 ,/.'-]", "");;

            HashMap pageInfo = Util.getPageInfo(line);
            if (pageInfo != null){
                currentPage = (String) pageInfo.get("currentPage");
                totalPages = (String) pageInfo.get("totalPages");
            }


            if (Util.getClientNo(line) != null){
                clientNo = Util.getClientNo(line);
            }

//            System.out.println(line);
            extractTransactions(line);
//            System.out.println(line);
//
//            if (approxContains(line, "accountstatement", 3)) {
//                AccountStatement accountStatement = new AccountStatement();
//                System.out.println("accountStatement");
//
//                if (Util.findDates(line).size() > 0) {
//                    System.out.println("second or other list of account statement");
//
//                    if (approxContains(arrayList.get(i + 1), "futureaccountbookings", 3)) {
//                        System.out.println("Future Account Booking");
//                    }
//
//                } else {
//                    System.out.println("first list of account statement");
//                }
//
//                //getStatemets()
//
//            }
//
//
//
//            else if (approxContains(line, "contractnote", 3)) {
//                System.out.println("contractnote");
//                   if (approxContains(arrayList.get(i + 1), "Your change in capital", 3)) {
//                       System.out.println("contact note with one transactionImact");
//                }else{
//                       System.out.println("contact note with many transactionImacts");
//                   }
//            } else  if (approxContains(line, "debitadvice", 3)) {
//                System.out.println("debitadvice");
//            }else  if (approxContains(line, "advicestatement", 3)) {
//                System.out.println("advicestatement");
//            }
//
//
//
//            //general login
//            if (line.contains("Form without")){
//                pageNumber = Util.getPageNumber(line);
//                maxPageNumber = Util.getMaxPageNumber(line);
////                System.out.println(pageNumber);
////                System.out.println(maxPageNumber);
//
//            }
//            if (line.contains("Client no")){
//                clientNo = Util.getClientNo(line);
////                System.out.println(clientNo);
////                System.out.println("dfff");
//            }
        }
        System.out.println(currentPage + " currentPage");
        System.out.println(totalPages + " totalPages");
        System.out.println(clientNo + " clientNo");

    }

    public static int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            for (int j = 0; j <= len2; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }

        return dp[len1][len2];
    }

    public static boolean approxContains(String text, String substring, int maxMistakes) {

        text = text.replaceAll("[^a-zA-Z]", "").toLowerCase();
        substring = substring.replaceAll("[^a-zA-Z]", "").toLowerCase();

        int subLen = substring.length();

        for (int i = 0; i <= text.length() - subLen; i++) {
            String window = text.substring(i, i + subLen);
            if (levenshteinDistance(window, substring) <= maxMistakes) {
                return true;
            }
        }

        return false;
    }


    private String getNoSpacesLowerCaseIgnoreSymbols(String originalString) {
        return originalString.replaceAll("[^a-zA-Z]", "").toLowerCase();
    }






}
