package com.example.roller.convertor;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.util.ImageHelper;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import org.opencv.core.Rect;


import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.util.ImageHelper;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Tests {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        double x1D;
        double y1D;
        double x2D;
        double y2D;

        double aX1 = 0;
        double bY1 = 0;
        double cX2 = 0;
        double dY2 = 0;

        Mat imageE = Imgcodecs.imread("C:\\Users\\GameOn\\Desktop\\AAA\\j\\image.jpg", Imgcodecs.IMREAD_GRAYSCALE);

        // Применить размытие для уменьшения шума
        Imgproc.GaussianBlur(imageE, imageE, new Size(5, 5), 0);

        // Использовать Tesseract для распознавания текста
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("G:\\Tesseract-OCR\\tessdata"); // Укажите путь к tessdata
        List<Rectangle> textRegions = new ArrayList<>();

        try {
            BufferedImage bufferedImage = ImageIO.read(new File("C:\\Users\\GameOn\\Desktop\\AAA\\j\\image.jpg"));
            bufferedImage = ImageHelper.convertImageToGrayscale(bufferedImage); // Преобразовать в градации серого
            List<Word> words = tesseract.getWords(bufferedImage, ITessAPI.TessPageIteratorLevel.RIL_WORD);

            // Получение координат bounding boxes для текста
            for (Word word : words) {
                Rectangle rect = new Rectangle(word.getBoundingBox().x, word.getBoundingBox().y,
                        word.getBoundingBox().width, word.getBoundingBox().height);
                textRegions.add(rect);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Выделить края с помощью оператора Кэнни
        Mat edges = new Mat();
        Imgproc.Canny(imageE, edges, 20, 80);

        // Найти линии с использованием преобразования Хафа
        Mat lines = new Mat();
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 50, 100, 10);

        // Преобразование линий в список
        List<double[]> linesList = new ArrayList<>();
        for (int i = 0; i < lines.rows(); i++) {
            linesList.add(lines.get(i, 0));
        }

        // Создаем список для объединенных линий
        List<double[]> mergedLines = new ArrayList<>();

        for (int i = 0; i < linesList.size(); i++) {
            double[] line1 = linesList.get(i);
            double x1 = line1[0];
            double y1 = line1[1];
            double x2 = line1[2];
            double y2 = line1[3];

            // Проверка всех следующих линий на близость и объединение их
            for (int j = i + 1; j < linesList.size(); j++) {
                double[] line2 = linesList.get(j);
                double x3 = line2[0];
                double y3 = line2[1];
                double x4 = line2[2];
                double y4 = line2[3];

                // Проверка близости по координатам
                if (Math.abs(y1 - y3) < 6 && Math.abs(y2 - y4) < 6 && Math.abs(x2 - x3) < 10) {
                    // Объединение линий
                    x1 = Math.min(x1, x3);
                    x2 = Math.max(x2, x4);
                    // Удаление объединенной линии из списка
                    linesList.remove(j);
                    j--;
                }
            }
            // Добавление объединенной линии в список
            mergedLines.add(new double[]{x1, y1, x2, y2});
        }

        // Вывод объединенных и фильтрованных линий
        for (double[] line : mergedLines) {
            x1D = line[0];
            y1D = line[1];
            x2D = line[2];
            y2D = line[3];

            // Проверка на горизонтальность (небольшое изменение по оси Y)
            if (Math.abs(y1D - y2D) <= 5) {
                double thickness = Math.sqrt((x2D - x1D) * (x2D - x1D) + (y2D - y1D) * (y2D - y1D));

                double thicknessThreshold = 450; // Порог для более толстых линий
                if (thickness > thicknessThreshold && !isLineInTextRegion(x1D, y1D, x2D, y2D, textRegions)) {
                    System.out.println("Толстая горизонтальная линия: [" + x1D + ", " + y1D + "] -> [" + x2D + ", " + y2D + "], толщина: " + thickness);
                    aX1 = 330;//x1D;
                    bY1 = 355;//y1D;
                    cX2 = 779;//x2D;
                    dY2 = 370;//y2D;
                }
            }
        }

        // Путь к исходному изображению
        String inputImagePath = "C:\\Users\\GameOn\\Desktop\\AAA\\j\\image.jpg";
        // Путь к папке, где будет сохранено обрезанное изображение
        String outputImagePath = "C:\\Users\\GameOn\\Desktop\\AAA\\j\\cropped_image.jpg";

        // Координаты для обрезки (например, координаты горизонтальной линии)
        int x1 = (int) aX1;
        int y1 = (int) bY1;
        int x2 = (int) cX2;
        int y2 = (int) dY2;

        System.out.println("x1 " + x1);
        System.out.println("y1 " + y1);
        System.out.println("x2 " + x2);
        System.out.println("y2 " + y2);

        // Обрезка области по заданным координатам
        try {
            Mat image = Imgcodecs.imread(inputImagePath);

            // Убедимся, что координаты корректны и находятся в пределах изображения
            int cropX = Math.min(x1, x2);
            int cropY = Math.min(y1, y2);
            int cropWidth = Math.abs(x2 - x1);
            int cropHeight = Math.abs((y2 + 1) - y1);
            // Обрезка области изображения
            Rect roi = new Rect(cropX, cropY, cropWidth, cropHeight);
            Mat croppedImage = new Mat(image, roi);
            // Сохранение обрезанного изображения
            Imgcodecs.imwrite(outputImagePath, croppedImage);

            System.out.println("Обрезанное изображение сохранено в: " + outputImagePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод проверки, пересекается ли линия с текстовой областью
    private static boolean isLineInTextRegion(double x1, double y1, double x2, double y2, List<Rectangle> textRegions) {
        for (Rectangle region : textRegions) {
            if (region.contains(x1, y1) || region.contains(x2, y2)) {
                return true;
            }
        }
        return false;
    }
}