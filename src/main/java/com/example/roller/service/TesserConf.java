package com.example.roller.service;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.*;
import org.springframework.stereotype.Service;

@Service
public class TesserConf {
    private final AmazonTextract textractClient;

    public TesserConf(AmazonTextract textractClient) {
        this.textractClient = textractClient;
    }
    public TesserConf(){
        BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAVVZOORQN6IX5AFZQ", "qVHTgti2MY5Iz2lak/bQm7iQLBsiD9PxFbZ/f4oF");
        this.textractClient = AmazonTextractClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }


    public AnalyzeDocumentResult analyzeDocument(String bucketName, String documentName) {
        Document document = new Document().withS3Object(new S3Object().withBucket(bucketName).withName(documentName));

        AnalyzeDocumentRequest request = new AnalyzeDocumentRequest()
                .withDocument(document)
                .withFeatureTypes(FeatureType.TABLES, FeatureType.FORMS); // Указываем типы анализа

        return textractClient.analyzeDocument(request);
    }
}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//        /*String tesseractPath = "G:\\Tesseract-OCR\\tesseract.exe";  // Путь к исполняемому файлу Tesseract
//        String imagePath = "C:\\Users\\GameOn\\Desktop\\AAA\\image.jpg";  // Путь к изображению
//        String outputBaseName = "C:\\Users\\GameOn\\Desktop\\AAA\\output";  // Путь к выходному файлу (без расширения)
//        String hocrFilePath = outputBaseName + ".hocr";  // Добавляем расширение .hocr
//
//        try {
//            // Формируем команду
//            ProcessBuilder pb = new ProcessBuilder(tesseractPath, imagePath, outputBaseName, "-l", "eng", "hocr");
//            pb.redirectErrorStream(true);
//            Process process = pb.start();
//
//            // Чтение
//            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line;
//            while ((line = in.readLine()) != null) {
//                System.out.println(line);
//            }
//            in.close();
//
//            int exitCode = process.waitFor();
//            System.out.println("Tesseract exited with code " + exitCode);
//
//            // Проверка
//            File hocrFile = new File(hocrFilePath);
//            if (hocrFile.exists()) {
//                System.out.println("HOCR file created: " + hocrFilePath);
//
//                // Чтение и вывод
//                parseAndPrintHOCR(hocrFilePath);
//            } else {
//                System.out.println("HOCR file not found: " + hocrFilePath);
//            }
//
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Метод для парсинга
//    private static void parseAndPrintHOCR(String hocrFilePath) {
//        try {
//            // Чтение и парсинг
//            File input = new File(hocrFilePath);
//            Document doc = Jsoup.parse(input, "UTF-8");
//
//            // Извлечение всех элементов
//            Elements spans = doc.select("span.ocrx_word");
//
//            System.out.println("Extracted Text:");
//
//            // Вывод текста
//            for (Element span : spans) {
//                String text = span.text();
//                System.out.print(text + "\n");  // Выводим слова в одной строке с пробелом между ними
//            }
//            System.out.println();  // Переход на новую строку после вывода всего текста
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }*/