package com.example.roller.convertor;

import com.example.roller.entity.UBS.*;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
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


    public Convertor() {
    }

    private static boolean isEnglishTextValid(String text) {
        if (text.contains("Page")) {
            return true;
        } else if (text.contains("Produced on")) {
            return true;
        } else if (text.contains("Account")) {
            return true;
        }
        return false;
    }

    private static BufferedImage rotateImage(BufferedImage img, int angle) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage rotatedImage = new BufferedImage(width, height, img.getType());
        var g2d = rotatedImage.createGraphics();
        g2d.rotate(Math.toRadians(angle), width / 2.0, height / 2.0);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        return rotatedImage;
    }


    public AllFilesUBS processFilesUBS(File decodedPdf) throws IOException {
        try {
            ArrayList<AccountStatement> accountStatements = new ArrayList<AccountStatement>();
            ArrayList<Advice> advices = new ArrayList<Advice>();
            ArrayList<OutOfSorting> outOfSorting = new ArrayList<OutOfSorting>();


            List<File> pdfList = extractPagesToSeparatePDFs(decodedPdf);
            int cores = Runtime.getRuntime().availableProcessors();
            ExecutorService executorService = Executors.newFixedThreadPool(cores / 2);

            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < pdfList.size(); i++) {
                File pdf = pdfList.get(i);
                int index = i;

                Future<?> future = executorService.submit(() -> {
                    try {
                        Tesseract threadTesseract = new Tesseract();
                        threadTesseract.setOcrEngineMode(1);
                        threadTesseract.setDatapath("src/main/resources/Tesseract-OCR/tessdata");
                        threadTesseract.setLanguage("eng");
                        threadTesseract.setTessVariable("preserve_interword_spaces", "1");

                        BufferedImage image = convertPDFToImage(pdf);
                        String text = threadTesseract.doOCR(image);

                        if (isEnglishTextValid(text)) {
//                            System.out.println("valid");
                        } else {
                            BufferedImage newImage = rotateImage(image, 180);
                            text = threadTesseract.doOCR(newImage);
                            if (!isEnglishTextValid(text)) {
                                text = null;
                            } else {
                                image = newImage;
                            }
                        }



                        String encodedImage = encodeImage(image, "jpg");
                        if (text != null) {
                            AccountStatement accountStatement = getAccountStatementUBS(text);
                            Advice advice = (accountStatement == null) ? getAdviceUBS(text) : null;

                            if (advice != null) {
                                advice.setEncodedImage(encodedImage);
                                advice.setIndex(index);

                                if (advice.getPage() == null || advice.getMaxPage() == null) {
                                    HashMap pageInfo = getPage(threadTesseract, image);
                                    if (pageInfo != null && pageInfo.get("currentPage") != null) {
                                        advice.setPage(pageInfo.get("currentPage").toString());
                                        advice.setMaxPage(pageInfo.get("totalPages").toString());
                                    }
                                }
                                advices.add(advice);
                            } else if (accountStatement != null) {
                                accountStatement.setEncodedImage(encodedImage);
                                accountStatement.setIndex(index);

                                if (accountStatement.getPage() == null || accountStatement.getMaxPage() == null) {
                                    HashMap pageInfo = getPage(threadTesseract, image);
                                    if (pageInfo != null && pageInfo.get("currentPage") != null) {
                                        accountStatement.setPage(pageInfo.get("currentPage").toString());
                                        accountStatement.setMaxPage(pageInfo.get("totalPages").toString());
                                    }
                                }
                                accountStatements.add(accountStatement);
                            }else{
                                OutOfSorting out = new OutOfSorting();
                               out.setEncodedImage(encodedImage);
                                out.setIndex(index);
                                outOfSorting.add(out);
                            }


                        } else {
                            OutOfSorting out = new OutOfSorting();
                           out.setEncodedImage(encodedImage);
                            out.setIndex(index);
                            outOfSorting.add(out);
                        }

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

//         System.out.println(filesList.size() + "size");
//
////            System.out.println(filesList);
//            for (int i = 0; i < accountStatements.size() - 1; i++) {
//                for (int j = 0; j < accountStatements.size() - i - 1; j++) {
//                    if (accountStatements.get(j).getInitialIndex() > accountStatements.get(j + 1).getInitialIndex()) {
//                        // Swap cosmoList[j] and cosmoList[j + 1]
//                        CosmoFile temp = accountStatements.get(j);
//                        accountStatements.set(j, accountStatements.get(j + 1));
//                        accountStatements.set(j + 1, temp);
//                    }
//                }
//            }

            AllFilesUBS allFilesUBS = new AllFilesUBS();
            allFilesUBS.setAdvices(advices);
            allFilesUBS.setAccountStatements(accountStatements);
            allFilesUBS.setOutOfSorting(outOfSorting);
            return allFilesUBS;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public HashMap getPage(Tesseract tesseract,BufferedImage image) throws TesseractException, IOException {
        int width = image.getWidth();
        int height = image.getHeight();

        // Calculate 12% dimensions
        int cropWidth = (int) (width * 0.15); // 12% of width
        int cropHeight = (int) (height * 0.15); // 12% of height

        // Calculate starting coordinates for the crop
        int cropX = width - cropWidth;  // Start at (total width - 12%)
        int cropY = height - cropHeight; // Start at (total height - 12%)

        // Crop the image
        BufferedImage croppedImage = image.getSubimage(cropX, cropY, cropWidth, cropHeight);
        String text = tesseract.doOCR(croppedImage);
        return  getPageInfo(text);
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


    private AccountStatement getAccountStatement(String line, List<String> lines, Integer index) {
        AccountStatement accountStatement = new AccountStatement();
        accountStatement.setName("Account Statement");
        String period = findDates(line);
        if (period == null) {
            period = findDates(lines.get(index + 1));
        }

        if (period != null) {
            accountStatement.setValueDate(period);
            ArrayList<Transaction> transactions = new ArrayList<>();
            ArrayList<Advice> advices = new ArrayList<>();

            for (int j = index; j < lines.size() - index; j++) {
                String innerLine = lines.get(j);
                Transaction transaction = extractTransactionCosmoFile(innerLine);
                if (transaction != null) {
                    transactions.add(transaction);
                }

                if (approxContains(innerLine, "Closing of Service Price", 3) && extractAdviceDate(lines.get(j + 1)) != null) {
                    Advice advice = new Advice();
                    advice.setName("Advice");
                    advice.setValueDate(extractAdviceDate(lines.get(j + 1)));

                    for (int b = j; b < lines.size(); b++) {
                        String innerLine2 = lines.get(b);
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
                if (approxContains(innerLine, "Interest calculation", 3) && extractAdviceDate(lines.get(j + 1)) != null) {
                    Advice advice = new Advice();
                    advice.setName("Advice");
                    advice.setValueDate(extractAdviceDate(lines.get(j + 1)));
                    for (int b = j; b < lines.size(); b++) {
                        String innerLine2 = lines.get(b);
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
            return accountStatement;
        }
        return null;
    }


    private Advice getCustodyMandateFeeAdvice(String line, List<String> lines, Integer index) {
        Advice advice = new Advice();
        String innerLine = lines.get(index + 1);
        String data = extractAdviceCustodyFeeDate(innerLine);
        for (int j = index; j < lines.size(); j++) {
            String innerLine2 = lines.get(j);
            if (approxContains(innerLine2, "Debit custody fee", 2)) {
                String amount = extractAdviceCustodyFeeAmount(innerLine2);
                if (amount != null) {
                    advice.setAmount(amount);
                    advice.setValueDate(data);
                    advice.setName("Debit custody fee");
                    break;
                }
            } else if (approxContains(innerLine2, "Debit mandate fee", 2)) {
                String amount = extractAdviceCustodyFeeAmount(innerLine2);
                if (amount != null) {
                    advice.setAmount(amount);
                    advice.setValueDate(data);
                    advice.setName("Debit mandate fee");
                    break;
                }
            }
        }
        return advice;
    }

    private Advice getCreditAdvice(String line, List<String> lines, Integer index) {
        Advice advice = new Advice();
        advice.setName("Credit Advice");
        for (int j = index; j < lines.size(); j++) {
            String innerLine = lines.get(j);

            if (approxContains(innerLine, "Total amount", 2)) {
                String amount = extractAdviceDebitAdviceAmount(innerLine);
                if (amount != null) {
                    String innerLine2 = lines.get(j + 1);
                    String data = extractAdviceDebitAdviceDate(innerLine2);

                    if (data != null) {
                        advice.setAmount(amount);
                        advice.setValueDate(data);
                        break;
                    }
                }
            }
        }
        return advice;
    }
    private Advice getDebitAdvice(String line, List<String> lines, Integer index) {
        Advice advice = new Advice();
        advice.setName("Debit Advice");
        for (int j = index; j < lines.size(); j++) {
            String innerLine = lines.get(j);

            if (approxContains(innerLine, "Total amount", 2)) {
                String amount = extractAdviceDebitAdviceAmount(innerLine);
                if (amount != null) {
                    String innerLine2 = lines.get(j + 1);
                    String data = extractAdviceDebitAdviceDate(innerLine2);

                    if (data != null) {
                        advice.setAmount(amount);
                        advice.setValueDate(data);
                        break;
                    }
                }
            }
        }
        return advice;
    }

    private Advice getTotalInterestAdvice(String line, List<String> lines, Integer index) {
        Advice advice = new Advice();
        advice.setName("Interest");
        String amount = extractAdviceTotalInterestAmount(line);
        if (amount != null) {
            advice.setAmount(amount);
        }
        return advice;
    }



    private Advice getConfirmationAdvice(String line, List<String> lines, Integer index) {
        Advice advice = new Advice();


        if (lines.get(index + 1).contains("Produced on")) {
            advice.setConfirmationNumber(lines.get(index - 2));
        }
        if (lines.get(index + 2).contains("Produced on")) {
            advice.setConfirmationNumber(lines.get(index + 4));

        }

        advice.setName("Confirmation");
        for (int j = index; j < lines.size(); j++) {
            String innerLine = lines.get(j);
            if (approxContains(innerLine, "Settlement Date", 2) && advice.getValueDate() == null) {
                advice.setValueDate(getSettlementDate(innerLine));
            } else if (approxContains(innerLine, "Transaction Currency Amount", 2) ||
                    approxContains(innerLine, "Premium", 2)
                    || approxContains(innerLine, "Amount and Currency Payable by Counterparty", 2)
                    || approxContains(innerLine, "Settlement Currency Amount", 2)
            ) {
                String amount = extractAmountConfirmation(innerLine);
                if (amount != null) {
                    advice.setAmount(amount.substring(4));
                }
            }


        }
        return advice;
    }


    private Advice getConfirmationOfAmendmentAdvice(String line, List<String> lines, Integer index) {
        Advice advice = new Advice();
        advice.setName("Confirmation Of Amendment");
        for (int j = index; j < lines.size(); j++) {
            String innerLine = lines.get(j);

            if (approxContains(innerLine, "Settlement Date", 2)) {
                advice.setValueDate(getSettlementDate(innerLine));
            } else if (approxContains(innerLine, "Transaction Currency Amount", 2)
                    || approxContains(innerLine, "Premium", 2)
                    || approxContains(innerLine, "Amount and Currency Payable by Counterparty", 2)
                    || approxContains(innerLine, "Settlement Currency Amount", 2)
            ) {
                String amount = extractAmountConfirmation(innerLine);
                if (amount != null) {
                    advice.setAmount(amount.substring(4));
                }
            }

        }
        return advice;
    }

    private Advice getConfirmationDeliveryAdvice(String line, List<String> lines, Integer index) {
        Advice advice = new Advice();
        if (approxContains(line,"Confirmation of outgoing delivery",1)){
            advice.setName("Confirmation of outgoing delivery");
        }else{
            advice.setName("Confirmation of incoming delivery");
        }

        for (int j = index; j < lines.size(); j++) {
            String innerLine = lines.get(j);
            if (approxContains(innerLine, "Value date", 2)) {
                HashMap data = getConfirmationOfDelivery(innerLine);
                String valueDate = (String) data.get("valueDate");
                String amount = (String) data.get("amount");
                advice.setValueDate(valueDate);
                advice.setAmount(amount);
                break;
            }
        }
        return advice;
    }

    private Advice getContractNoteAdvice(String line, List<String> lines, Integer index) {
        Advice advice = new Advice();
        advice.setName("Contract Note");
        for (int j = index; j < lines.size(); j++) {
            String innerLine = lines.get(j);
            if (advice.getInterest() == null && approxContains(innerLine, "Interest from", 2)){
                advice.setInterest(getInterest(innerLine));
            }
            if (approxContains(innerLine, "In favour of account", 2) || approxContains(innerLine, "To the debit of account", 2)) {
                HashMap data = extractDataContractNote(innerLine);
                String valueDate = (String) data.get("valueDate");
                String amount = (String) data.get("amount");
                advice.setValueDate(valueDate);
                advice.setAmount(amount);
                break;
            }
        }
        return advice;
    }

    private Advice getAdviceStatementAdvice(String line, List<String> lines, Integer index) {
        Advice advice = new Advice();
        advice.setName("Advice/Statement");
        for (int j = index; j < lines.size(); j++) {
            String innerLine = lines.get(j);
            if (approxContains(innerLine, "CREDIT ACCOUNT", 2) || approxContains(innerLine, "To the debit of account", 2)) {
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
        return advice;
    }

    private AccountStatement getAccountStatementUBS(String text) {
        try {
            List<String> lines = Arrays.stream(text.split("\\n")).toList();
            ArrayList<String> arrayList = new ArrayList<>(lines);

            String currentPage = null;
            String maxPage = null;
            String clientNo = null;
            String producedOn = null;
            HashMap info = null;
            String iban = null;

            AccountStatement accountStatement = null;


            for (int i = 0; i < arrayList.size(); i++) {
                String line = arrayList.get(i).replaceAll("[^a-zA-Z0-9 ,/.'-]", "");


                if (clientNo == null) {
                    clientNo = getClientNo(line);
                }
                if (iban == null) {
                    iban = getIban(line);
                }

                if (producedOn == null) {
                    producedOn = extractProducedOn(line);
                }

                if (info == null) {
                    info = getPageInfo(line);
                    if (currentPage == null && info != null){
                        currentPage = (String) info.get("currentPage");
                        maxPage = (String) info.get("totalPages");
                    }
                }


                if (approxContains(line, "Account Statement", 2) && accountStatement == null) {
                    accountStatement = getAccountStatement(line, lines, i);
                }
            }

            if (accountStatement != null){

                accountStatement.setPage(currentPage);
                accountStatement.setMaxPage(maxPage);
                accountStatement.setClientNo(clientNo);
                accountStatement.setProducedOn(producedOn);
                accountStatement.setIban(iban);
                return accountStatement;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private Advice getAdviceUBS(String text) {
        try {
            List<String> lines = Arrays.stream(text.split("\\n")).toList();
            ArrayList<String> arrayList = new ArrayList<>(lines);
            String currentPage = null;
            String maxPage = null;
            String clientNo = null;
            String producedOn = null;


            Advice advice = null;


            for (int i = 0; i < arrayList.size(); i++) {
                String line = arrayList.get(i).replaceAll("[^a-zA-Z0-9 ,/.'-]", "");

                if (clientNo == null) {
                    clientNo = getClientNo(line);
                }

                if (producedOn == null) {
                    producedOn = extractProducedOn(line);
                }

                HashMap info = null;
                if (info == null) {
                    info = getPageInfo(line);
                    if (info != null){
                        currentPage = (String) info.get("currentPage");
                        maxPage = (String) info.get("totalPages");
                    }
                }

                if (advice == null){
                        if (approxContains(line, "Custody fee", 2) && extractAdviceCustodyFeeDate(arrayList.get(i + 1)) != null || approxContains(line, "Mandate fee", 2) && extractAdviceCustodyFeeDate(arrayList.get(i + 1)) != null) {
                            advice = getCustodyMandateFeeAdvice(line, lines, i);
                        }
                        else if (approxContains(line, "Debit Advice", 1) && extractDateAdvice(line) != null) {
                            advice = getDebitAdvice(line, lines, i);
                        }
                        else if (approxContains(line, "Credit Advice", 1) && extractDateAdvice(line) != null) {
                            advice = getCreditAdvice(line, lines, i);
                        }
                        else if (approxContains(line, "Total interest", 1) && producedOn != null) {
                            advice = getTotalInterestAdvice(line, lines, i);
                        }
                        else if (line.equals("Confirmation") && (arrayList.get(i + 1).contains("Produced on")  || arrayList.get(i + 2).contains("Produced on")) ) {
                            advice = getConfirmationAdvice(line, lines, i);
                        }
                        else if (approxContains(line, "Confirmation of Amendment", 2) && approxContains(arrayList.get(i + 1), "Produced on", 2)) {
                            advice = getConfirmationOfAmendmentAdvice(line, lines, i);
                        }
                        else if (approxContains(line, "Contract note", 2) && approxContains(line, "Produced on", 2)) {
                            advice = getContractNoteAdvice(line, lines, i);
                        }
                        else if ((approxContains(line, "Confirmation of outgoing delivery", 2)  || approxContains(line, "Confirmation of incoming delivery", 2))
                                && approxContains(line, "Produced on", 2)) {
                            advice = getConfirmationDeliveryAdvice(line, lines, i);
                        }
                        else if (containsAdviceStatementText(line) && approxContains(line, "Produced on", 2)) {
                            advice = getAdviceStatementAdvice(line, lines, i);

                        }

                    }

            }
            if (advice != null){

                advice.setPage(currentPage);
                advice.setMaxPage(maxPage);
                advice.setClientNo(clientNo);
                advice.setProducedOn(producedOn);
                return advice;
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
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

//    public ArrayList<CosmoFile> findAdvices(AccountStatement accountStatement,ArrayList<CosmoFile> filesList){
//        ArrayList<CosmoFile> advices = new ArrayList<>();
//        ArrayList<CosmoFile> transactions = accountStatement.getTransactions();
//        for (int i = 0; i < transactions.size(); i++) {
//            CosmoFile transactionCosmoFile = transactions.get(i);
//            Transaction transaction = (Transaction) transactionCosmoFile.appObject;
//            for (int j = 0; j < filesList.size(); j++) {
//                if (!filesList.get(i).name.equals("Account Statement")){
//                    CosmoFile adviceCosmoFile = filesList.get(i);
//                    Advice advice = (Advice) adviceCosmoFile.appObject;
//                    if (advice.getAmount().equals(transaction.getAmount()) && adviceCosmoFile.getValueDate().equals(transactionCosmoFile.getValueDate())){
//                        advices.add(adviceCosmoFile);
//                    }
//
//                }
//
//            }
//        }
//        return advices;
//    }
//
//    public ArrayList<CosmoFile> sort(ArrayList<CosmoFile> filesList) {
//        ArrayList<ArrayList<CosmoFile>> bundles = new ArrayList<>();
//
//        for (int i = 0; i < filesList.size(); i++) {
//            CosmoFile cosmoFile = filesList.get(i);
//            System.out.println(cosmoFile.getAppObject());
//            if (cosmoFile.appObject instanceof AccountStatement){
//                System.out.println("11");
//                AccountStatement accountStatement = (AccountStatement) cosmoFile.getAppObject();
//
//            }
//        }
//
//        return null;
//    }

}
