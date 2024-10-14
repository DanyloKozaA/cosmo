package com.example.roller.convertor;

import com.example.roller.entity.AccountStatement;
import com.example.roller.entity.Advice;
import com.example.roller.entity.CosmoFile;
import com.example.roller.entity.Transaction;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import static com.example.roller.util.Util.*;


@Component
public class Convertor {
    private final ArrayList<CosmoFile> filesList = new ArrayList<>();

    public Convertor() {
    }

    public ArrayList<CosmoFile> processFiles(String inputPdfPath, String bankName) throws IOException {
        try {

            List<File> pdfList = extractPagesToSeparatePDFs(inputPdfPath);
            int cores = Runtime.getRuntime().availableProcessors();
            ExecutorService executorService = Executors.newFixedThreadPool(cores/2);
            System.out.println(executorService);


            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < pdfList.size(); i++) {
                File pdf = pdfList.get(i);
                int index = i;

                Future<?> future = executorService.submit(() -> {
                    System.out.println(executorService);
                    try {
                        Tesseract threadTesseract = new Tesseract();
                        threadTesseract.setOcrEngineMode(1);
                        threadTesseract.setDatapath("src/main/resources/Tesseract-OCR/tessdata");
                        threadTesseract.setLanguage("eng");
                        threadTesseract.setTessVariable("preserve_interword_spaces", "1");

                        BufferedImage image = convertPDFToImage(pdf);
                        String encodedImage = encodeImage(image, "jpg");
                        image.flush();
                        String text = threadTesseract.doOCR(image);
                        processTextUBS(text, index, encodedImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                futures.add(future);
            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println(e + " error during task execution");
                    e.printStackTrace();
                }
            }

            // Shutdown the executor service
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(300, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
//                    System.out.println("Executor service shut down now after timeout");
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                System.out.println("Executor service interrupted during shutdown");
            }

//            System.out.println(filesList.size());
            return filesList;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public static String encodeImage(BufferedImage image, String formatName) {
        String base64Image = "";
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, formatName, outputStream);

            byte[] imageBytes = outputStream.toByteArray();
            base64Image = Base64.getEncoder().encodeToString(imageBytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64Image;
    }

    private String processTextUBS(String text,Integer index,String encodedImage){
        try {
            List lines = Arrays.stream(text.split("\\n")).toList();
            ArrayList<String> arrayList = new ArrayList<>(lines);
            boolean found = false;
            String currentPage = null;
            String maxPage = null;
            String clientNo = null;

            for(int b = arrayList.size() - 1; b >= 0; b--){
                String line = arrayList.get(b).replaceAll("[^a-zA-Z0-9 ,/.'-]", "");
                HashMap info = getPageInfo(line);
                if (info != null) {
                     currentPage = (String) info.get("currentPage");
                     maxPage = (String) info.get("totalPages");
                     break;
                }
            }


            for (int i = 0; i < arrayList.size(); i++) {

                String line = arrayList.get(i).replaceAll("[^a-zA-Z0-9 ,/.'-]", "");


                if (clientNo == null){
                    clientNo = getClientNo(line);
                }


                if (approxContains(line, "Account Statement", 2)) {
                    AccountStatement accountStatement = new AccountStatement();
                    String period = findDates(line);
                    if (period == null){
                         period = findDates(arrayList.get(i +1));
                    }
                    accountStatement.setPeriod(period);
                    ArrayList<Transaction> transactions = new ArrayList<>();
                    ArrayList<Advice> advices = new ArrayList<>();

                    for (int j = i; j < arrayList.size() - i; j++) {
                        String innerLine = arrayList.get(j);
                        Transaction transaction = extractTransactions(innerLine);
                        if (transaction != null){
                            transactions.add(transaction);
                        }

                        if (approxContains(innerLine, "Closing of Service Price", 3) && extractAdviceDate(arrayList.get(j + 1)) != null) {
                            Advice advice = new Advice();
                            advice.setType("Closing of Service Price");
                            advice.setValueDate(extractAdviceDate(arrayList.get(j + 1)));

                            for (int b = j; b < arrayList.size(); b++) {
                                String innerLine2 = arrayList.get(b);
                                    if (approxContains(innerLine2, "Balance of closing of service prices", 3)) {
                                        String amount = extractAdviceClosServPriceAmount(innerLine2);
                                        if (amount != null) {
                                            advice.setAmount(amount);
                                            advices.add(advice);
                                            break;
                                        }
                                    }
                            }
                        }
                        if (approxContains(innerLine, "Interest calculation", 3) && extractAdviceDate(arrayList.get(j + 1)) != null) {
                            Advice advice = new Advice();
                            advice.setType("Interest calculation");
                            advice.setValueDate(extractAdviceDate(arrayList.get(j + 1)));
                            for (int b = j; b < arrayList.size(); b++) {
                                String innerLine2 = arrayList.get(b);
                                if (approxContains(innerLine2, "Interest calculation balance", 2)) {
                                    advice.setAmount(extractInterestCalculationAdviceAmount(innerLine2));
                                     advices.add(advice);
                                    break;
                                }
                            }

                        }
                    }

                    accountStatement.setTransactions(transactions);
                    accountStatement.setAdvices(advices);
                    found = true;
                    accountStatement.setInitialIndex(index);
                    accountStatement.setEncodedImage(encodedImage);
                    filesList.add(accountStatement);
                    break;
                }



                if (approxContains(line, "Custody fee", 2)  && extractAdviceCustodyFeeDate(arrayList.get(i + 1)) != null || approxContains(line, "Mandate fee", 2)  && extractAdviceCustodyFeeDate(arrayList.get(i + 1)) != null) {
                    Advice advice = new Advice();
                    String innerLine = arrayList.get(i + 1);
                    String data = extractAdviceCustodyFeeDate(innerLine);
                    for (int j = i; j < arrayList.size(); j++) {
                            String innerLine2 = arrayList.get(j);
                            if (approxContains(innerLine2, "Debit custody fee", 2) || approxContains(innerLine2, "Debit mandate fee", 2)) {
                                String amount = extractAdviceCustodyFeeAmount(innerLine2);
                                if (amount != null) {
                                    advice.setAmount(amount);
                                    advice.setValueDate(data);
                                    advice.setType(innerLine2);
                                    break;
                                }
                            }
                    }
                    found = true;
                    advice.setInitialIndex(index);
                    advice.setEncodedImage(encodedImage);
                    filesList.add(advice);
                    break;
                }
//
//


                if (approxContains(line, "Debit Advice", 2) && extractDateAdvice(line) != null) {
                    Advice advice = new Advice();
                    advice.setType("Debit Advice");
                    for (int j = i; j < arrayList.size(); j++) {
                        String innerLine = arrayList.get(j);

                        if (approxContains(innerLine, "Total amount", 2)) {
                            String amount = extractAdviceDebitAdviceAmount(innerLine);
                            if (amount != null) {
                                String innerLine2 = arrayList.get(j + 1);
                                String data = extractAdviceDebitAdviceDate(innerLine2);

                                if (data != null) {
                                    advice.setAmount(amount);
                                    advice.setValueDate(data);
                                    break;
                                }
                            }
                        }
                    }
                    found = true;
                    advice.setInitialIndex(index);
                    advice.setEncodedImage(encodedImage);
                    filesList.add(advice);
                    break;
                }





                if (line.equals("Confirmation") && arrayList.get(i + 1).contains("Produced on")) {
                    Advice advice = new Advice();
                    advice.setType("Confirmation");
                    for (int j = i; j < arrayList.size(); j++) {
                        String innerLine = arrayList.get(j);

                        if (approxContains(innerLine, "Settlement Date", 2)) {
                            advice.setValueDate(getSettlementDate(innerLine));
                        }
                        else if (approxContains(innerLine, "Transaction Currency Amount", 2) || approxContains(innerLine, "Premium", 2)  ||approxContains(innerLine, "Amount and Currency Payable by Counterparty", 2)) {
                            String amount = extractAmountConfirmation(innerLine);
                            if (amount != null){
                                advice.setAmount(amount.substring(4));
                            }
                        }


                    }
                    found = true;
                    advice.setInitialIndex(index);
                    advice.setEncodedImage(encodedImage);
                    filesList.add(advice);
                    break;
                }

                if (approxContains(line, "Confirmation of Amendment", 2) && approxContains(arrayList.get(i + 1), "Produced on", 2)) {
                    Advice advice = new Advice();
                    advice.setType("Confirmation of Amendment");
                    for (int j = i; j < arrayList.size(); j++) {
                        String innerLine = arrayList.get(j);

                        if (approxContains(innerLine, "Settlement Date", 2)) {
                            advice.setValueDate(getSettlementDate(innerLine));
                        }
                        else if (approxContains(innerLine, "Transaction Currency Amount", 2) || approxContains(innerLine, "Premium", 2)  ||approxContains(innerLine, "Amount and Currency Payable by Counterparty", 2)) {
                            String amount = extractAmountConfirmation(innerLine);
                            if (amount != null){
                                advice.setAmount(amount.substring(4));
                            }
                        }

                    }
                    found = true;
                    advice.setInitialIndex(index);
                    advice.setEncodedImage(encodedImage);
                    filesList.add(advice);
                    break;
                }




                if (approxContains(line, "Contract note", 2) && approxContains(line, "Produced on", 2)) {
                    Advice advice = new Advice();
                    advice.setType("Contract Note");
                    advice.setPage(currentPage);
                    advice.setInitialIndex(index);
                    advice.setMaxPages(maxPage);
                    advice.setClientNo(clientNo);
                    for (int j = i; j < arrayList.size(); j++) {
                        String innerLine = arrayList.get(j);
                        if (approxContains(innerLine, "In favour of account", 2) || approxContains(innerLine, "To the debit of account", 2)) {
                            HashMap data = extractDataContractNote(innerLine);

                                String valueDate = (String) data.get("valueDate");
                                String amount = (String) data.get("amount");
                                advice.setValueDate(valueDate);
                                advice.setAmount(amount);
                                break;
                        }
                    }
                    found = true;
                    advice.setEncodedImage(encodedImage);
                    filesList.add(advice);
                    break;
                }
//
                if (approxContains(line, "Advice/Statement", 2) && approxContains(line, "Produced on", 2)) {
                    Advice advice = new Advice();
                    advice.setType("Advice/Statement");
                    advice.setPage(currentPage);
                    advice.setMaxPages(maxPage);
                    advice.setClientNo(clientNo);
                    advice.setInitialIndex(index);
                    for (int j = i; j < arrayList.size(); j++) {
                        String innerLine = arrayList.get(j);
                        if (approxContains(innerLine, "CREDIT ACCOUNT", 2)) {
                            HashMap data = extractDataAdviceStatement(innerLine);
                            if (data != null) {
                                String valueDate = (String) data.get("date");
                                String amount = (String) data.get("amount");
                                advice.setAmount(amount);
                                advice.setValueDate(valueDate);
                                break;
                            }
                        }
                    }
                    found = true;
                    advice.setEncodedImage(encodedImage);
                    filesList.add(advice);
                    break;
                }
            }
            if (!found){
                CosmoFile cosmoFile = new CosmoFile();
                cosmoFile.setPage(currentPage);
                cosmoFile.setMaxPages(maxPage);
                cosmoFile.setClientNo(clientNo);
                cosmoFile.setInitialIndex(index);
                cosmoFile.setType("not found");
                cosmoFile.setEncodedImage(encodedImage);
                filesList.add(cosmoFile);
            }

            return null;
        }catch (Exception e){
            System.out.println(e + "error");
            return null;
        }
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
        try {

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
        } catch (Exception e) {
            System.out.println(e + " error");
            return false;
        }
    }

    public ArrayList<CosmoFile> sort(ArrayList<CosmoFile> filesList){
      try{
          ArrayList<CosmoFile> sorted = new ArrayList<CosmoFile>();

          ArrayList<ArrayList<AccountStatement>> accountStatementsGroups = new ArrayList<>();
          for (int i = 0; i < filesList.size(); i++) {
                if (filesList.get(i) instanceof AccountStatement){
                    AccountStatement accountStatement = (AccountStatement) filesList.get(i);
                   if (accountStatementsGroups.isEmpty()){
                       ArrayList<AccountStatement> group = new ArrayList<AccountStatement>();
                       group.add(accountStatement);
                       accountStatementsGroups.add(group);
                   }else{
                       Boolean foundGroup = false;
                       for (int j = 0; j < accountStatementsGroups.size(); j++) {
                           ArrayList<AccountStatement> group = accountStatementsGroups.get(j);
                           AccountStatement groupedAccountStatement = group.get(0);
                           if (accountStatement.getPeriod().replaceAll("\\D", "").equals(groupedAccountStatement.getPeriod().replaceAll("\\D", ""))){
                               accountStatementsGroups.get(j).add(accountStatement);
                               foundGroup = true;
                               break;
                           };
                       }
                       if (!foundGroup){
                           ArrayList<AccountStatement> group = new ArrayList<AccountStatement>();
                           group.add(accountStatement);
                           accountStatementsGroups.add(group);
                       }
                   }

                }
            }
          accountStatementsGroups.forEach((group -> {
              group.forEach((accountStatement -> {
                  ArrayList<Transaction> transactions = accountStatement.getTransactions();
                  transactions.forEach((transaction -> {
                      for (CosmoFile cosmoFile : filesList) {;
                          if (cosmoFile instanceof Advice advice) {
                              if (advice.getAmount() == null || advice.getValueDate() == null) {
                                  advice.setStatus(false);
                              } else if (transaction.getAmount() == null || transaction.getValueDate() == null) {
                                  transaction.setStatus(false);
                              }
                              if (advice.getAmount() != null && advice.getValueDate() != null && transaction.getAmount() != null && transaction.getValueDate() != null) {
                                  if (advice.getAmount().replaceAll("\\D", "").equals(transaction.getAmount().replaceAll("\\D", "")) && advice.getValueDate().replaceAll("\\D", "").equals(transaction.getValueDate().replaceAll("\\D", ""))) {
                                      sorted.add(advice);
                                  }else{
//                                      System.out.println(advice);
//                                      System.out.println(transaction);
                                  }
                              }

                          }

                      }
                  }));
              }));
          }));
          System.out.println(sorted.size() + "sortedSize");
            return null;
        }catch (Exception e){
            System.out.println(e +"error");
            return null;
        }
    }

}
