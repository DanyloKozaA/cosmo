package com.example.roller.convertor;

import com.example.roller.entity.AccountStatement;
import com.example.roller.entity.Advice;
import com.example.roller.entity.Transaction;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import static com.example.roller.util.Util.*;


@Component
public class Convertor {
    private final List<Object> fileList = new ArrayList<>();

    public Convertor() {
    }

        public List<Object> processFiles (String inputPdfPath, String bankName) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        List<File> pdfList = extractPagesToSeparatePDFs(inputPdfPath);
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        for (int i = 0; i < pdfList.size(); i++) {
            File pdf = pdfList.get(i);
            executorService.submit(() -> {
                try {
                    Tesseract threadTesseract = new Tesseract();
                    threadTesseract.setOcrEngineMode(1);
                    // Egor
                    threadTesseract.setDatapath("G:\\Tesseract-OCR\\tessdata");
                    // Danylo
                    // threadTesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
                    threadTesseract.setLanguage("eng");
                    threadTesseract.setTessVariable("preserve_interword_spaces", "1");

                    BufferedImage image = convertPDFToImage(pdf);

                    image = binarizeImage(image);
                    String text = threadTesseract.doOCR(image);
                    processTextUBS(text);


                } catch (IOException | TesseractException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(300, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                System.out.println("SHUT DOWN");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        return fileList;
    }




    private void processTextUBS(String text) throws IOException {
    List lines = Arrays.stream(text.split("\\n")).toList();
    ArrayList<String> arrayList = new ArrayList<>(lines);



        for (int i = 0; i < arrayList.size(); i++) {
            String line = arrayList.get(i).replaceAll("[^a-zA-Z0-9 ,/.'-]", "");


            if (approxContains(line, "Account Statement", 2)) {
                AccountStatement accountStatement = new AccountStatement();
                ArrayList<Transaction> pageTransactions = new ArrayList<>(); // Инициализируем новый список для каждой страницы

                for (int j = 0; j < arrayList.size(); j++) {

                    String innerLine = arrayList.get(j);
                    Transaction transaction = extractTransactions(innerLine);

                    if (transaction != null) {
                        transaction.setStatus("true");
                        pageTransactions.add(transaction);
                    } else {
                        transaction = notOrdinaryTransaction(innerLine);

                        if (transaction != null) {
                            transaction.setStatus("false");
                            pageTransactions.add(transaction);
                        }
                    }

                    HashMap info = getPageInfo(innerLine);
                    String clientNo = getClientNo(innerLine);

                    if (info != null) {
                        String currentPage =(String) info.get("currentPage");
                        String maxPage =(String) info.get("totalPages");
                        accountStatement.setPage(currentPage);
                        accountStatement.setMaxPages(maxPage);
                    }

                    if (clientNo != null) {
                        accountStatement.setClientNo(clientNo);
                    }
                }

                // После обработки всех строк страницы:
                accountStatement.setTransactions(pageTransactions);
                System.out.println(accountStatement + "AAJJAJA");

                    fileList.add(accountStatement);
                System.out.println(fileList + "!@#!@#!@#!@#@");

            }



            if(approxContains(line, "Interest calculation" ,3) && extractAdviceDate(arrayList.get(i+1))!=null ) {
                Advice advice = new Advice();
                      for (int j = i; j < arrayList.size(); j++) {
                          String innerLine = arrayList.get(j);
                          HashMap info = getPageInfo(innerLine);
                          if (info != null) {
                              String currentPage =(String) info.get("currentPage");
                              String maxPage =(String) info.get("totalPages");
                              advice.setPage(currentPage);
                              advice.setMaxPages(maxPage);
                          }
                          if (approxContains(innerLine, "Interest calculation balance", 2)) {
                              HashMap dateAmount = extractAdviceCalculator(innerLine);
                            if (dateAmount != null) {
                              String date =(String) dateAmount.get("date");
                              String amount =(String) dateAmount.get("amount");
                              advice.setAmount(amount);
                              advice.setValueDate(date);
                              advice.setType("Interest calculator");
                              fileList.add(advice);
                        }
                    }
                }
                break;
            }



            if(approxContains(line,"Custody fee",2) && extractAdviceCustodyFeeDate(arrayList.get(i+1))!=null){
                Advice advice = new Advice();
                String innerLine= arrayList.get(i+1);
                String data = extractAdviceCustodyFeeDate(innerLine);
                for (int j = 0; j < arrayList.size(); j++) {
                    String innerLine2 = arrayList.get(j);
                    HashMap info = getPageInfo(innerLine2);
                    if(data!=null) {
                        advice.setValueDate(data);
                        if (info != null) {
                            String currentPage =(String) info.get("currentPage");
                            String maxPage =(String) info.get("totalPages");
                            advice.setPage(currentPage);
                            advice.setMaxPages(maxPage);
                        }
                        if (approxContains(innerLine2, "Debit custody fee", 2)) {
                            String amount = extractAdviceCustodyFeeAmount(innerLine2);
                            if (amount != null) {
                                advice.setAmount(amount);
                                advice.setType("Custody fee");
                                fileList.add(advice);
                            }
                        }
                    }
                }
                break;
            }



            if(approxContains(line,"Mandate fee",2) && extractAdviceCustodyFeeDate(arrayList.get(i+1))!=null) {
                Advice advice = new Advice();
                String innerLine= arrayList.get(i+1);
                String data = extractAdviceCustodyFeeDate(innerLine);
                for (int j = 0; j < arrayList.size(); j++) {
                    String innerLine2 = arrayList.get(j);
                    HashMap info = getPageInfo(innerLine2);
                    if (info != null) {
                        String currentPage =(String) info.get("currentPage");
                        String maxPage =(String) info.get("totalPages");
                        advice.setPage(currentPage);
                        advice.setMaxPages(maxPage);
                        System.out.println(currentPage + maxPage + "AAAAAAAA1231231212312132");
                    }
                    if(data!=null) {
                        advice.setValueDate(data);
                      if(approxContains(innerLine2,"Debit mandate fee",2)){
                          String amount = extractAdviceMandateFeeAmount(innerLine2);
                          if(amount!=null){
                              advice.setAmount(amount);
                              advice.setType("Mandate fee");
                              fileList.add(advice);
                          }
                      }
                    }
                }
                break;
            }


            if(approxContains(line,"Debit Advice",2) && extractFiterforDebitAdvice(line)!=null){
                Advice advice = new Advice();
                for (int j = i; j < arrayList.size(); j++) {
                    String innerLine = arrayList.get(j);
                    HashMap info = getPageInfo(innerLine);
                    if (info != null) {
                        String currentPage =(String) info.get("currentPage");
                        String maxPage =(String) info.get("totalPages");
                        advice.setPage(currentPage);
                        advice.setMaxPages(maxPage);
                    }
                    if (approxContains(innerLine, "Total amount", 2)) {
                        String amount = extractAdviceDebitAdviceAmount(innerLine);
                         if(amount!=null){
                             String innerLine2 = arrayList.get(j+1);
                             String data = extractAdviceDebitAdviceDate(innerLine2);
                              if(data!=null){
                                  advice.setType("Debit Advice");
                                  advice.setAmount(amount);
                                  advice.setValueDate(data);
                                  fileList.add(advice);
                              }
                         }
                    }
                }
                break;
            }



            if (approxContains(line, "Closing of Service Price", 3) && extractAdviceDate(arrayList.get(i + 1)) != null) {
                Advice advice = new Advice();
                String innerLine = arrayList.get(i + 1);
                String data = extractAdviceDate(innerLine);

                for (int j = 0; j < arrayList.size(); j++) {
                    String innerLine2 = arrayList.get(j);
                    HashMap info = getPageInfo(innerLine2);
                    if (info != null) {
                        System.out.println("!QQQQQQQQQQ");
                        String currentPage =(String) info.get("currentPage");
                        String maxPage =(String) info.get("totalPages");
                        advice.setPage(currentPage);
                        advice.setMaxPages(maxPage);
                    }
                    if (data != null) {
                         advice.setValueDate(data);
                         advice.setType("Closing of Service Price");
                        if (approxContains(innerLine2, "Balance of closing of service prices", 3)) {
                            String amount = extractAdviceClosServPriceAmount(innerLine2);
                            if (amount != null) {
                                advice.setAmount(amount);
                                fileList.add(advice);
                            }
                        }
                    }
                }
                break;
            }



                        if (approxContains(line, "Confirmation ",2) && !approxContains(line,"Confirmation of Amendment",2)) {

                            Advice advice = new Advice();
                            for (int j = i; j < arrayList.size(); j++) {
                                String innerLine = arrayList.get(j);
                                HashMap info = getPageInfo(innerLine);
                                if (info != null) {
                                    String currentPage =(String) info.get("currentPage");
                                    String maxPage =(String) info.get("totalPages");
                                    advice.setPage(currentPage);
                                    advice.setMaxPages(maxPage);
                                }

                                if(approxContains(innerLine,"Transaction Currency Amount",2)){
                                     String amount = extractAdviceConfMetalAmount(innerLine);
                                     if(amount!=null){
                                         advice.setAmount(amount);
                                         advice.setType("Confirmation");
                                         String innerLine2 = arrayList.get(j+1);
                                         String date = extractAdviceConfarmationMetalData(innerLine2);
                                         if(date!=null){
                                             advice.setValueDate(date);
                                             System.out.println(advice + " CCCCC");
                                        }
                                    }
                                }


                                if(approxContains(innerLine,"Premium",2)){
                                    String amount = extractAdviceConfirmationAmount(innerLine);
                                    if(amount!=null){
                                        advice.setAmount(amount);
                                        advice.setType("Confirmation");
                                        String innerLine2 = arrayList.get(j+3);
                                        String date = extractAdviceConfarmationData(innerLine2);
                                        if(date!=null){
                                            advice.setValueDate(date);
                                            System.out.println(advice + "BBBBB");
                                        }
                                    }
                                }


                                if (approxContains(innerLine, "Amount and Currency Payable by Counterparty",2)){
                                    String amount = extractAdviceConfirmationFXAmount(innerLine);
                                    if(amount!=null){
                                        advice.setAmount(amount);
                                        advice.setType("Confirmation");
                                        String innerLine2 = arrayList.get(j+1);
                                        String date = extractAdviceConfarmationMetalData(innerLine2);
                                        if(date!=null){
                                            advice.setValueDate(date);
                                            System.out.println(advice + "AAAAA");
                                        }
                                    }

                                }
                            }
                            fileList.add(advice);

                            break;
                        }



            if (approxContains(line, "Confirmation of Amendment",2) && filterForConfMetal(arrayList.get(i+1))!=null) {
                Advice advice = new Advice();
                for (int j = i; j < arrayList.size(); j++) {
                    String innerLine = arrayList.get(j);
                    HashMap info = getPageInfo(innerLine);
                    String amount = extractAdviceConfMetalAmount(innerLine);
                    if (info != null) {
                        String currentPage =(String) info.get("currentPage");
                        String maxPage =(String) info.get("totalPages");
                        advice.setPage(currentPage);
                        advice.setMaxPages(maxPage);
                    }

                    if(amount!=null){
                        advice.setAmount(amount);
                        advice.setType("Confirmation of Amendment");
                        String innerLine2 = arrayList.get(j+1);
                        String date = extractAdviceConfarmationMetalData(innerLine2);
                        if(date!=null){
                            advice.setValueDate(date);
                            fileList.add(advice);
                        }
                    }
                }
                break;
            }




            if (approxContains(line, "Contract note", 2) && extractFiterforDateWithNote(line)!=null) {
                Advice advice = new Advice();
                for (int j = i; j < arrayList.size(); j++) {
                    String innerLine = arrayList.get(j);
                    HashMap info = getPageInfo(innerLine);
                    if (info != null) {
                        String currentPage =(String) info.get("currentPage");
                        String maxPage =(String) info.get("totalPages");
                        advice.setPage(currentPage);
                        advice.setMaxPages(maxPage);
                    }
                    if (approxContains(innerLine, "In favour of account", 2) || approxContains(innerLine,"To the debit of account",2)) {
                        HashMap dateAmount = extractAdviceCotractNote(innerLine);
                        if (dateAmount != null) {
                            String date =(String) dateAmount.get("date");
                            String amount =(String) dateAmount.get("amount");
                            advice.setValueDate(date);
                            advice.setAmount(amount);
                            advice.setType("Contract Note");
                            fileList.add(advice);
                        }
                    }
                }
                break;
            }


                     if(approxContains(line,"Advice/Statement",2)) {
                         Advice advice = new Advice();
                         for (int j = i; j < arrayList.size(); j++) {
                             String innerLine = arrayList.get(j);
                             HashMap info = getPageInfo(innerLine);
                             if (info != null) {
                                 String currentPage =(String) info.get("currentPage");
                                 String maxPage =(String) info.get("totalPages");
                                 advice.setPage(currentPage);
                                 advice.setMaxPages(maxPage);
                             }
                             if (approxContains(innerLine, "CREDIT ACCOUNT", 2)) {
                                 HashMap dateAmout = extractAdviceStatement(innerLine);
                                 if (dateAmout != null) {
                                     String date = (String) dateAmout.get("date");
                                     String amout = (String) dateAmout.get("amount");
                                     advice.setAmount(amout);
                                     advice.setValueDate(date);
                                     advice.setType("Advice/Statement");
                                     fileList.add(advice);
                                 }
                             }
                         }
                         break;
                     }
        }
        System.out.println(fileList + "AAAAAAAAAAAAA");
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

}
