package com.example.roller.config.configTesserator;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class TesserConf {
    public static void main(String[] args) {
        String tesseractPath = "G:\\Tesseract-OCR\\tesseract.exe";  // Путь к исполняемому файлу Tesseract
        String imagePath = "C:\\Users\\GameOn\\Desktop\\AAA\\image.jpg";  // Путь к изображению
        String outputBaseName = "C:\\Users\\GameOn\\Desktop\\AAA\\output";  // Путь к выходному файлу (без расширения)
        String hocrFilePath = outputBaseName + ".hocr";  // Добавляем расширение .hocr

        try {
            // Формируем команду
            ProcessBuilder pb = new ProcessBuilder(tesseractPath, imagePath, outputBaseName, "-l", "eng", "hocr");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Чтение
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();

            int exitCode = process.waitFor();
            System.out.println("Tesseract exited with code " + exitCode);

            // Проверка
            File hocrFile = new File(hocrFilePath);
            if (hocrFile.exists()) {
                System.out.println("HOCR file created: " + hocrFilePath);

                // Чтение и вывод
                parseAndPrintHOCR(hocrFilePath);
            } else {
                System.out.println("HOCR file not found: " + hocrFilePath);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Метод для парсинга
    private static void parseAndPrintHOCR(String hocrFilePath) {
        try {
            // Чтение и парсинг
            File input = new File(hocrFilePath);
            Document doc = Jsoup.parse(input, "UTF-8");

            // Извлечение всех элементов
            Elements spans = doc.select("span.ocrx_word");

            System.out.println("Extracted Text:");

            // Вывод текста
            for (Element span : spans) {
                String text = span.text();
                System.out.print(text + "\n");  // Выводим слова в одной строке с пробелом между ними
            }
            System.out.println();  // Переход на новую строку после вывода всего текста

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}