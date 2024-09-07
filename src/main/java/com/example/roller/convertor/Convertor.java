package com.example.roller.convertor;

import com.jhlabs.image.ContrastFilter;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.lucene.store.Directory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.imgscalr.Scalr;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.io.File;
import java.io.IOException;
import org.apache.lucene.store.FSDirectory;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.hunspell.Dictionary;
import org.apache.lucene.analysis.hunspell.Hunspell;
import java.text.ParseException;


@Component
public class Convertor {

    public Convertor() {
    }

    public void processFiles(String inputPdfPath, String bankName) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        try {
            Directory directory = FSDirectory.open(Files.createTempDirectory("temp"));
            InputStream affFileStream = new FileInputStream("src/main/resources/hunspell/en_US.aff");

            InputStream dicFileStream = new FileInputStream("src/main/resources/hunspell/en_US.dic");
            Dictionary dictionary = new Dictionary(directory, "spellCheck", affFileStream, dicFileStream);

            Hunspell spellChecker = new Hunspell(dictionary);

            String correctWord = "guava";
            String misspelledWord = "recieve";

            System.out.println(String.format("Is %s spelled correctly?: %b", correctWord, spellChecker.spell(correctWord)));
            System.out.println(String.format("Is %s spelled correctly?: %b", misspelledWord, spellChecker.spell(misspelledWord)));
            System.out.println(String.format("Did you mean: %s", spellChecker.suggest(misspelledWord)));
        } catch (IOException e) {
        System.out.println(e);
        e.printStackTrace();
    } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        List<File> pdfList = extractPagesToSeparatePDFs(inputPdfPath);
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

//
//                    Tesseract threadTesseract2 = new Tesseract();
//                    threadTesseract2.setOcrEngineMode(3);
//                    threadTesseract2.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
//                    threadTesseract2.setLanguage("eng");
//                    System.out.println(2);
////
//
////                    threadTesseract.setPageSegMode(6);
//         threadTesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.,- ");
                    BufferedImage image = convertPDFToImage(pdf);
                    File outputFile = new File("C:\\Users\\Danylo\\Downloads\\outputdfgdfg" + pageIndex + ".png");
                    image = binarizeImage(image);
                    ImageIO.write(image, "png", outputFile);

                    String text = threadTesseract.doOCR(image);
//
//
//                    BufferedImage preprocessedImage = preprocessImage(image);
//                    BufferedImage image3 = processImageForOCR("C:\\Users\\Danylo\\Downloads\\outputdfgdfg" + pageIndex + ".png");
//
//                    image = binarizeImage(image);
//                    File outputFile3 = new File("C:\\Users\\Danylo\\Downloads\\ddddddd" + pageIndex + ".png");
//                    ImageIO.write(image, "png", outputFile3);

//                    int width = image.getWidth();
//                    int height = image.getHeight();
//                    System.out.println(width);
//                    System.out.println(height);

//                    System.out.println(3);
//                    String text = threadTesseract.doOCR(preprocessedImage);
//                    String text2 = threadTesseract2.doOCR(image);
//                    String text3 = threadTesseract.doOCR(image3);
//                    System.out.println(text2);

//                    System.out.println(text2);
//                    System.out.println(text3);







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



    public static BufferedImage processImageForOCR(String inputImagePath) {
        // Convert BufferedImage to OpenCV Mat
        try {
            Mat sourceImage = Imgcodecs.imread(inputImagePath);

            if (sourceImage.empty()) {
                throw new IllegalArgumentException("BufferedImage is empty or invalid.");
            }

            // 1. Convert to Grayscale
            Mat grayscaleImage = new Mat();
            Imgproc.cvtColor(sourceImage, grayscaleImage, Imgproc.COLOR_BGR2GRAY);

            // 2. Increase Contrast (Histogram Equalization)
            Mat contrastEnhancedImage = new Mat();
            Imgproc.equalizeHist(grayscaleImage, contrastEnhancedImage);

            // 3. Resize Image (Increase Resolution if needed)
            Mat resizedImage = new Mat();
            Size scaleSize = new Size(sourceImage.width() * 2, sourceImage.height() * 2);  // Double the size
            Imgproc.resize(contrastEnhancedImage, resizedImage, scaleSize);

            // 4. Noise Reduction (GaussianBlur)
            Mat denoisedImage = new Mat();
            Imgproc.GaussianBlur(resizedImage, denoisedImage, new Size(5, 5), 0);

                int type = BufferedImage.TYPE_BYTE_GRAY;  // Default type is grayscale
                if (denoisedImage.channels() > 1) {
                    type = BufferedImage.TYPE_3BYTE_BGR;  // Change type if the image is not grayscale
                }

                BufferedImage image = new BufferedImage(denoisedImage.cols(), denoisedImage.rows(), type);
                byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            denoisedImage.get(0, 0, data);  // Copy data from the Mat to the BufferedImage
                return image;

        }catch (Exception e){
            System.out.println(e);
        }
        return null;
    }


    public static String findCommonText(String text1, String text2, String text3) {
        // Split the text into words or tokens (you can refine this based on your input)
        Set<String> set1 = new HashSet<>(Set.of(text1.split("\\s+")));
        Set<String> set2 = new HashSet<>(Set.of(text2.split("\\s+")));
        Set<String> set3 = new HashSet<>(Set.of(text3.split("\\s+")));

        // Retain only common words in all three sets
        set1.retainAll(set2);
        set1.retainAll(set3);

        // Recreate the string from common words
        return String.join(" ", set1);
    }
    public static String extractTransactions(String line) {

        // Clean up the input line by removing unwanted characters
        System.out.println(line);

//        System.out.println(line);
        // Updated regex pattern as per the user's input

        String transactionPattern = "(\\d{2}\\.\\d{2}\\.\\d{2})\\s+.*?\\s+([\\d,]+\\.\\d{2})\\s*[^\\w]*\\s*(\\d{2}\\.\\d{2}\\.\\d{2})\\s+([\\d ]+\\d{3}\\.\\d{2})";


        // Compile the new pattern
        Pattern pattern = Pattern.compile(transactionPattern);
        Matcher matcher = pattern.matcher(line);

//        System.out.println(line);
        if (matcher.matches()) {
            System.out.println("Matched linefffffffffffffffffffffffff: " + line);

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



    private BufferedImage preprocessImage(BufferedImage image) {
        // Convert to grayscale
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = grayImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        // Binarization (thresholding)
        BufferedImage binaryImage = new BufferedImage(grayImage.getWidth(), grayImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = binaryImage.createGraphics();
        g2.drawImage(grayImage, 0, 0, null);
        g2.dispose();

   grayImage = reduceNoise(grayImage);
//   grayImage = enhanceContrast(grayImage);


        return grayImage;
    }

    private BufferedImage preprocessImageForOCR(BufferedImage image) {
        // De-skew the image if necessary
//        image = deskewImage(image);
//
//        // Apply binarization (convert to black and white)
////    image = binarizeImage(image);
////
//        // Apply noise reduction
//        image = reduceNoise(image);
//
//        // Enhance contrast
//        image = enhanceContrast(image);

        return image;
    }

    // Method to de-skew an image (this is a placeholder, a real implementation would be more complex)
    private BufferedImage deskewImage(BufferedImage image) {
        // Skew correction logic goes here
        // For simplicity, we'll assume the image is already properly oriented
        return image;
    }

    // Method to binarize an image
    private BufferedImage binarizeImage(BufferedImage image) {
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

        // Apply binarization (thresholding)
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

    // Method to reduce noise in an image
    private BufferedImage reduceNoise(BufferedImage image) {
        // Reduce noise using a simple mean filter or any other noise reduction technique
        // For example, using Scalr (imgscalr library)
        return Scalr.apply(image, Scalr.OP_ANTIALIAS);
    }

    // Method to enhance contrast in an image
    private BufferedImage enhanceContrast(BufferedImage image) {
        // Apply a contrast filter
        ContrastFilter contrast = new ContrastFilter();
        contrast.setContrast(1.5f); // Increase contrast by 50%
        return contrast.filter(image, null);
    }

    // Example method to save the processed image to a file (for testing purposes)
    private void saveImageToFile(BufferedImage image, String outputFilePath) throws IOException {
        ImageIO.write(image, "png", new File(outputFilePath));
    }


    private void processTextUBS(String text) throws IOException {
    List lines = Arrays.stream(text.split("\\n")).toList();

   ArrayList<String> arrayList = new ArrayList<>(lines);
        // Loop through each line


        Number pageNumber;
        Number maxPageNumber;
        String clientNo;

        for (int i = 0; i < arrayList.size(); i++) {
            String line = arrayList.get(i).replaceAll("[^a-zA-Z0-9 ,/.'-]", "");;
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
//
//        System.out.println("ffsdsfsdfsdf");

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

    private List<File> extractPagesToSeparatePDFs(String inputPdfPath) throws IOException {
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

    private BufferedImage convertPDFToImage(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            return pdfRenderer.renderImageWithDPI(0, 800, ImageType.RGB);
        }
    }




}
