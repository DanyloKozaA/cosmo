package com.example.roller.convertor;

import com.example.roller.entity.AccountStatement;
import com.example.roller.entity.Advice;
import com.example.roller.entity.Transaction;
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

import static com.example.roller.util.Util.*;


@Component
public class Convertor {
    List<Advice> adviceList = new ArrayList<>();
    ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    public Convertor() {
    }

    public void processFiles(String inputPdfPath, String bankName) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        List<File> pdfList = extractPagesToSeparatePDFs(inputPdfPath);
        ExecutorService executorService = Executors.newFixedThreadPool(5);


        for (int i = 0; i < pdfList.size(); i++) {
            File pdf = pdfList.get(i);
            int pageIndex = i + 1; //
            executorService.submit(() -> {
                try {
                    Tesseract threadTesseract = new Tesseract();
                    threadTesseract.setOcrEngineMode(1);
                    //Egor
                    threadTesseract.setDatapath("G:\\Tesseract-OCR\\tessdata");
                    //Danylo
                   // threadTesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
                    threadTesseract.setLanguage("eng");
                    threadTesseract.setTessVariable("preserve_interword_spaces", "1");


                    BufferedImage image = convertPDFToImage(pdf);
                    //Egor
                    File outputFile = new File("C:\\Users\\GameOn\\Desktop\\AAA\\meow" + pageIndex + ".png");
                    //Danylo
                   // File outputFile = new File("C:\\Users\\Danylo\\Downloads\\outputdfgdfg" + pageIndex + ".png");
                    image = binarizeImage(image);
                    ImageIO.write(image, "png", outputFile);

                    String text = threadTesseract.doOCR(image);


                    if ("UBS".equals(bankName)) {
                        processTextUBS(text);
                    }


                } catch (IOException | TesseractException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(260, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                System.out.println("SHUT DOWN");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();

        }
    }




    private void processTextUBS(String text) throws IOException {
    List lines = Arrays.stream(text.split("\\n")).toList();

    ArrayList<String> arrayList = new ArrayList<>(lines);


        String currentPage = null;
        String totalPages = null;
        String clientNo = null;



        for (int i = 0; i < arrayList.size(); i++) {
            String line = arrayList.get(i).replaceAll("[^a-zA-Z0-9 ,/.'-]", "");

            HashMap pageInfo = getPageInfo(line);

            if (pageInfo != null){
                currentPage = (String) pageInfo.get("currentPage");
                totalPages = (String) pageInfo.get("totalPages");
            }


            if (getClientNo(line) != null){
                clientNo = getClientNo(line);
            }



            if (approxContains(line,"Account Statement",2)){
                for (int j = 0; j < arrayList.size(); j++) {
                    String innerLine = arrayList.get(j);
                    Transaction transaction = extractTransactions(innerLine);
                    if (transaction != null){
                        transaction.setStatus("true");
                        transactions.add(transaction);
                    }else {
                      transaction = notOrdinaryTransaction(innerLine);
                       if (transaction != null){
                           transaction.setStatus("false");
                           transactions.add(transaction);
                       }
                    }
                }
                AccountStatement accountStatement = new AccountStatement();
                accountStatement.setTransactions(transactions);


                if (findDates(line).size() > 0) {
                    System.out.println(findDates(line));
                } else {
                    System.out.println( findDates(arrayList.get(i + 1)));
                }
                //getStatemets()
            }



            if(approxContains(line, "Interest calculation" ,3) && extractAdviceDate(arrayList.get(i+1))!=null ) {
                      for (int j = i; j < arrayList.size(); j++) {
                          String innerLine = arrayList.get(j);
                  if (approxContains(innerLine, "Interest calculation balance", 2)) {
                        Advice advice = extractAdviceCalculator(innerLine);
                        if (advice != null) {
                            advice.setType("Interest calculator");
                            adviceList.add(advice);
                            break;
                        }
                    }
                }
            }



            if(approxContains(line,"Custody fee",2) && extractAdviceCustodyFeeDate(arrayList.get(i+1))!=null){
                String innerLine= arrayList.get(i+1);
                String data = extractAdviceCustodyFeeDate(innerLine);
                for (int j = 0; j < arrayList.size(); j++) {
                    String innerLine2 = arrayList.get(j);
                    Advice advice = new Advice();
                    if(data!=null) {
                        advice.setValueDate(data);

                        if (approxContains(innerLine2, "Debit custody fee", 2)) {
                            String amount = extractAdviceCustodyFeeAmount(innerLine2);
                            if (amount != null) {
                                advice.setAmount(amount);
                                advice.setType("Custody fee");
                                adviceList.add(advice);
                                break;
                            }
                        }
                    }
                }
            }



            if(approxContains(line,"Mandate fee",2) && extractAdviceCustodyFeeDate(arrayList.get(i+1))!=null) {
                String innerLine= arrayList.get(i+1);
                String data = extractAdviceCustodyFeeDate(innerLine);
                for (int j = 0; j < arrayList.size(); j++) {
                    String innerLine2 = arrayList.get(j);
                    Advice advice = new Advice();
                    if(data!=null) {
                        advice.setValueDate(data);
                      if(approxContains(innerLine2,"Debit mandate fee",2)){
                          String amount = extractAdviceMandateFeeAmount(innerLine2);
                          if(amount!=null){
                              advice.setAmount(amount);
                              advice.setType("Mandate fee");
                              adviceList.add(advice);
                              break;
                          }
                      }
                    }
                }
            }


            if(approxContains(line,"Debit Advice",2) && extractFiterforDebitAdvice(line)!=null){
                Advice advice = new Advice();
                for (int j = i; j < arrayList.size(); j++) {
                    String innerLine = arrayList.get(j);
                    if (approxContains(innerLine, "Total amount", 2)) {
                       String amount = extractAdviceDebitAdviceAmount(innerLine);
                         if(amount!=null){
                             String inerLine2 = arrayList.get(j+1);
                             String data = extractAdviceDebitAdviceDate(inerLine2);
                              if(data!=null){
                                  advice.setType("Debit Advice");
                                  advice.setAmount(amount);
                                  advice.setValueDate(data);
                                  adviceList.add(advice);
                                  break;
                              }
                         }
                    }
                }

            }



            if (approxContains(line, "Closing of Service Price", 3) && extractAdviceDate(arrayList.get(i + 1)) != null) {
                String innerLine = arrayList.get(i + 1);
                String data = extractAdviceDate(innerLine);

                for (int j = 0; j < arrayList.size(); j++) {
                    String innerLine2 = arrayList.get(j);
                    Advice advice = new Advice();
                    if (data != null) {
                         advice.setValueDate(data);
                         advice.setType("Closing of Service Price");
                        if (approxContains(innerLine2, "Balance of closing of service prices", 3)) {
                            String amount = extractAdviceClosServPriceAmount(innerLine2);
                            if (amount != null) {
                                advice.setAmount(amount);
                                adviceList.add(advice);
                            }
                        }
                    }
                }
            }



                        if (approxContains(line, "Confirmation ",2) && filterForConfMetal(arrayList.get(i+1))!=null && !approxContains(line,"Confirmation of Amendment",2)) {
                            for (int j = i; j < arrayList.size(); j++) {
                                String innerLine = arrayList.get(j);
                                String amount = extractAdviceConfMetalAmount(innerLine);
                                if(amount!=null){
                                    Advice advice = new Advice();
                                    advice.setAmount(amount);
                                    advice.setType("Confirmation Precious Metal");
                                    String innerLine2 = arrayList.get(j+1);
                                    String date = extractAdviceConfarmationMetalData(innerLine2);
                                       if(date!=null){
                                           advice.setValueDate(date);
                                           adviceList.add(advice);
                                       }
                                }
                                }
                            }



            if (approxContains(line, "Confirmation ",2) && filterForConfStandard(arrayList.get(i+1))!=null) {
                for (int j = i; j < arrayList.size(); j++) {
                    String innerLine = arrayList.get(j);
                    String amount = extractAdviceConfirmationAmount(innerLine);
                    if(amount!=null){
                        Advice advice = new Advice();
                        advice.setAmount(amount);
                        advice.setType("Confirmation Standard");
                        String innerLine2 = arrayList.get(j+3);
                        String date = extractAdviceConfarmationData(innerLine2);
                        if(date!=null){
                            advice.setValueDate(date);
                            adviceList.add(advice);
                        }
                    }
                }
            }



            if (approxContains(line, "Confirmation ",2) && filterForConfFX(arrayList.get(i+1))!=null) {
                for (int j = i; j < arrayList.size(); j++) {
                    String innerLine = arrayList.get(j);
                    String amount = extractAdviceConfirmationFXAmount(innerLine);
                    if(amount!=null){
                        Advice advice = new Advice();
                        advice.setAmount(amount);
                        advice.setType("Confirmation FX");
                        String innerLine2 = arrayList.get(j+1);
                        String date = extractAdviceConfarmationMetalData(innerLine2);
                        if(date!=null){
                            advice.setValueDate(date);
                            adviceList.add(advice);
                        }
                    }
                }
            }



            if (approxContains(line, "Confirmation of Amendment",2) && filterForConfMetal(arrayList.get(i+1))!=null) {
                for (int j = i; j < arrayList.size(); j++) {
                    String innerLine = arrayList.get(j);
                    String amount = extractAdviceConfMetalAmount(innerLine);
                    if(amount!=null){
                        Advice advice = new Advice();
                        advice.setAmount(amount);
                        advice.setType("Confirmation of Amendment");
                        String innerLine2 = arrayList.get(j+1);
                        String date = extractAdviceConfarmationMetalData(innerLine2);
                        if(date!=null){
                            advice.setValueDate(date);
                            adviceList.add(advice);
                        }
                    }
                }
            }




            if (approxContains(line, "Contract note", 2) && extractFiterforDateWithNote(line)!=null) {
                for (int j = i; j < arrayList.size(); j++) {
                    String innerLine = arrayList.get(j);
                    if (approxContains(innerLine, "In favour of account", 2) || approxContains(innerLine,"To the debit of account",2)) {
                        Advice advice = extractAdviceCotractNote(innerLine);
                        if (advice != null) {
                            advice.setType("Contract Note");
                            adviceList.add(advice);
                            break;
                        }
                    }
                }
            }


                     if(approxContains(line,"Advice/Statement",2)) {
                         for (int j = i; j < arrayList.size(); j++) {
                             String innerLine = arrayList.get(j);
                             if (approxContains(innerLine, "CREDIT ACCOUNT", 2)) {
                                 Advice advice;
                                 advice = extractAdviceStatement(innerLine);
                                 if (advice != null) {
                                     advice.setType("Advice/Statement");
                                     adviceList.add(advice);
                                 }
                             }
                         }
                     }

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

}
