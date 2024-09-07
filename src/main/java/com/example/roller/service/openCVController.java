package com.example.roller.service;
import com.example.roller.RollerApplication;
import com.example.roller.controllerRoller.ControllerClass;
import com.example.roller.convertor.Convertor;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class openCVController {

    public void getHorizontalLines(String inputPath, String outputPath){


        // Load OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Load the image in grayscale
        Mat src = Imgcodecs.imread(inputPath, Imgcodecs.IMREAD_GRAYSCALE);

        // Apply Gaussian blur to reduce noise
        Imgproc.GaussianBlur(src, src, new Size(3, 3), 0);

        // Use the Canny edge detector
        Mat edges = new Mat();
        Imgproc.Canny(src, edges, 50, 150);

        // Convert the grayscale edges to BGR (3-channel image)
        Mat edgesColor = new Mat();
        Imgproc.cvtColor(edges, edgesColor, Imgproc.COLOR_GRAY2BGR);

        // Change the color of the edges to red
        Mat redEdges = new Mat(edgesColor.size(), edgesColor.type(), new Scalar(255, 255, 255));
        Core.bitwise_and(redEdges, edgesColor, edgesColor);

        // Load the original image to draw the lines and edges on
        Mat result = Imgcodecs.imread(inputPath);

        // Overlay the red edges onto the original image
        Core.addWeighted(result, 1.0, edgesColor, 1.0, 0, result);

        // Use HoughLinesP to detect lines
        Mat lines = new Mat();
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 50, 250, 1);

        for (int i = 0; i < lines.rows(); i++) {
            double[] l = lines.get(i, 0);

            // Check if the line is horizontal (small difference in y-coordinates)
            if (Math.abs(l[1] - l[3]) < 10) {
                // Calculate the length of the line using Euclidean distance
                double lineLength = Math.sqrt(Math.pow(l[2] - l[0], 2) + Math.pow(l[3] - l[1], 2));

                // Filter lines by length (adjust length threshold if necessary)
                if (lineLength > 250) { // This is a heuristic threshold, adjust it according to your image scale
                    // Draw the line on the image in green
                    System.out.println("d");
                    Imgproc.line(result, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
                }
            }
        }

        // Save the result
        Imgcodecs.imwrite(outputPath, result);
    }
}
