package com.example.roller.convertor;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;



@Component
public class Convertor {
    private final Queue<File> pdfFilesQueue = new ConcurrentLinkedQueue<>();
    private final List<File> imageFilesList = new CopyOnWriteArrayList<>();
    private final Tesseract tesseract;

    public Convertor() {
        tesseract = new Tesseract();
        tesseract.setDatapath("G:/Tesseract-OCR/tessdata"); // Укажите правильный путь к tessdata
        tesseract.setLanguage("eng"); // Укажите язык
    }

    public void processFilesInSequence(String inputPdfPath) throws IOException {
        extractPagesToSeparatePDFs(inputPdfPath);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future;

        while ((future = executor.submit(this::processNextFile)).isDone()) {
            try {
                future.get(); // Ожидание завершения текущего потока
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Попытка немедленного завершения работы
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void extractPagesToSeparatePDFs(String inputPdfPath) throws IOException {
        File pdfFile = new File(inputPdfPath);
        if (!pdfFile.exists()) {
            throw new IOException("Файл не найден: " + inputPdfPath);
        }

        try (PDDocument document = PDDocument.load(pdfFile)) {
            int totalPages = document.getNumberOfPages();

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                PDPage page = document.getPage(pageIndex);

                try (PDDocument newDoc = new PDDocument()) {
                    newDoc.addPage(page);
                    File newFile = File.createTempFile("page_" + (pageIndex + 1), ".pdf");
                    newDoc.save(newFile);
                    newDoc.close();
                    pdfFilesQueue.add(newFile); // Добавляем файл в очередь
                }
            }
        }
    }

    private void processNextFile() {
        File pdfFile = pdfFilesQueue.poll();
        if (pdfFile != null) {
            try {
                convertPDFToImage(pdfFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void convertPDFToImage(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int totalPages = document.getNumberOfPages();

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 300, ImageType.RGB);
                File imageFile = File.createTempFile(pdfFile.getName().replace(".pdf", "_page_" + (pageIndex + 1)), ".jpg");
                ImageIO.write(image, "jpg", imageFile);
                imageFilesList.add(imageFile);

                // Выполнение OCR для извлечения текста с изображения
                try {
                    String result = tesseract.doOCR(image);
                    System.out.println("Текст из " + imageFile.getName() + ":");
                    System.out.println(result);
                    System.out.println("----");
                } catch (TesseractException e) {
                    System.err.println("Ошибка обработки файла " + imageFile.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    public List<File> getImageFilesList() {
        return imageFilesList;
    }
}
//C:/Users/GameOn/Desktop/AAA/1.pdf