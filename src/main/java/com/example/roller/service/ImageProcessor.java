//package com.example.roller.service;
//import org.opencv.core.*;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//import java.util.ArrayList;
//import java.util.List;
//
//public class ImageProcessor {
//
//    static {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//    }
//
//    public static List<Point[]> detectHorizontalLines(String imagePath) {
//        // Загрузка изображения
//        Mat src = Imgcodecs.imread(imagePath);
//
//        // Преобразование в градации серого
//        Mat gray = new Mat();
//        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
//
//        // Обнаружение краев
//        Mat edges = new Mat();
//        Imgproc.Canny(gray, edges, 50, 150, 3, false);
//
//        // Обнаружение линий с использованием преобразования Хафа
//        Mat lines = new Mat();
//        Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 100, 50, 10);
//
//        List<Point[]> horizontalLines = new ArrayList<>();
//
//        // Фильтрация горизонтальных линий
//        for (int i = 0; i < lines.rows(); i++) {
//            double[] line = lines.get(i, 0);
//            Point pt1 = new Point(line[0], line[1]);
//            Point pt2 = new Point(line[2], line[3]);
//
//            // Проверка, является ли линия горизонтальной по сравнению координат y
//            if (Math.abs(pt1.y - pt2.y) < 10) {  // Настройте порог по необходимости
//                horizontalLines.add(new Point[]{pt1, pt2});
//            }
//        }
//
//        return horizontalLines;
//    }
//
//    public static void main(String[] args) {
//        String imagePath = "путь_к_вашему_изображению.jpeg";
//        List<Point[]> horizontalLines = detectHorizontalLines(imagePath);
//
//        for (Point[] line : horizontalLines) {
//            System.out.println("Линия от: " + line[0] + " до " + line[1]);
//        }
//    }
//}
