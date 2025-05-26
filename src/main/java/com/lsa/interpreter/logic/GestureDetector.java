package com.lsa.interpreter.logic;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GestureDetector {
    private static final Logger logger = LoggerFactory.getLogger(GestureDetector.class);
    
    private final List<GestureDetectionListener> listeners;
    private final Mat skinMask;
    private final MatOfPoint hull;
    private final MatOfInt hullIndices;
    private final MatOfInt4 defects;

    public GestureDetector() {
        this.listeners = new ArrayList<>();
        this.skinMask = new Mat();
        this.hull = new MatOfPoint();
        this.hullIndices = new MatOfInt();
        this.defects = new MatOfInt4();
        
        logger.info("GestureDetector initialized");
    }

    public void processFrame(Mat frame) {
        try {
            // Convert to HSV color space for better skin detection
            Mat hsvFrame = new Mat();
            Imgproc.cvtColor(frame, hsvFrame, Imgproc.COLOR_BGR2HSV);

            // Detect skin color
            detectSkin(hsvFrame);

            // Find contours in the skin mask
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(skinMask, contours, hierarchy, 
                Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Process the largest contour (assumed to be the hand)
            if (!contours.isEmpty()) {
                MatOfPoint largestContour = findLargestContour(contours);
                processHandContour(largestContour, frame);
            }

            // Clean up
            hsvFrame.release();
            hierarchy.release();
            contours.forEach(MatOfPoint::release);

        } catch (Exception e) {
            logger.error("Error processing frame in gesture detector", e);
        }
    }

    private void detectSkin(Mat hsvFrame) {
        // Define skin color range in HSV
        Scalar lowerBound = new Scalar(0, 20, 70);
        Scalar upperBound = new Scalar(20, 255, 255);

        // Create binary mask for skin color
        Core.inRange(hsvFrame, lowerBound, upperBound, skinMask);

        // Apply morphological operations to clean up the mask
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
        Imgproc.erode(skinMask, skinMask, kernel);
        Imgproc.dilate(skinMask, skinMask, kernel);
        kernel.release();
    }

    private MatOfPoint findLargestContour(List<MatOfPoint> contours) {
        return contours.stream()
            .max((c1, c2) -> Double.compare(Imgproc.contourArea(c1), Imgproc.contourArea(c2)))
            .orElse(new MatOfPoint());
    }

    private void processHandContour(MatOfPoint contour, Mat frame) {
        // Find convex hull
        Imgproc.convexHull(contour, hull);
        Imgproc.convexHull(contour, hullIndices);

        // Find convexity defects
        if (contour.total() > 3) {
            Imgproc.convexityDefects(contour, hullIndices, defects);

            // Count fingers based on defects
            int fingerCount = countFingers(defects, contour);

            // Draw results
            drawResults(frame, contour, hull, defects);

            // Notify listeners
            notifyGestureDetected(fingerCount);
        }
    }

    private int countFingers(MatOfInt4 defects, MatOfPoint contour) {
        int fingerCount = 1; // Start with 1 for the thumb
        Point[] contourPoints = contour.toArray();
        int[] defectArray = defects.toArray();

        for (int i = 0; i < defectArray.length; i += 4) {
            Point start = contourPoints[defectArray[i]];
            Point end = contourPoints[defectArray[i + 1]];
            Point far = contourPoints[defectArray[i + 2]];
            float depth = defectArray[i + 3] / 256f;

            // Filter defects based on depth and angle
            if (depth > 10 && isValidFinger(start, end, far)) {
                fingerCount++;
            }
        }

        return Math.min(fingerCount, 5); // Cap at 5 fingers
    }

    private boolean isValidFinger(Point start, Point end, Point far) {
        double a = Math.sqrt(Math.pow(end.x - start.x, 2) + Math.pow(end.y - start.y, 2));
        double b = Math.sqrt(Math.pow(far.x - start.x, 2) + Math.pow(far.y - start.y, 2));
        double c = Math.sqrt(Math.pow(end.x - far.x, 2) + Math.pow(end.y - far.y, 2));
        
        // Calculate angle using cosine law
        double angle = Math.acos((b * b + c * c - a * a) / (2 * b * c)) * 180 / Math.PI;
        
        return angle <= 90; // Consider as finger if angle is less than 90 degrees
    }

    private void drawResults(Mat frame, MatOfPoint contour, MatOfPoint hull, MatOfInt4 defects) {
        // Draw contour
        Imgproc.drawContours(frame, List.of(contour), -1, new Scalar(0, 255, 0), 2);
        
        // Draw hull
        Imgproc.drawContours(frame, List.of(hull), -1, new Scalar(255, 0, 0), 2);
        
        // Draw defect points
        Point[] contourPoints = contour.toArray();
        int[] defectArray = defects.toArray();
        
        for (int i = 0; i < defectArray.length; i += 4) {
            Point far = contourPoints[defectArray[i + 2]];
            Imgproc.circle(frame, far, 4, new Scalar(0, 0, 255), -1);
        }
    }

    public void addListener(GestureDetectionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GestureDetectionListener listener) {
        listeners.remove(listener);
    }

    private void notifyGestureDetected(int fingerCount) {
        for (GestureDetectionListener listener : listeners) {
            listener.onGestureDetected(fingerCount);
        }
    }

    public interface GestureDetectionListener {
        void onGestureDetected(int fingerCount);
    }
}
