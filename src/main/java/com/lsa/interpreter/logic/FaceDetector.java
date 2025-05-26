package com.lsa.interpreter.logic;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FaceDetector {
    private static final Logger logger = LoggerFactory.getLogger(FaceDetector.class);
    private static final String FACE_CASCADE_FILE = "/haarcascades/haarcascade_frontalface_default.xml";
    private static final String EYE_CASCADE_FILE = "/haarcascades/haarcascade_eye.xml";
    private static final double CONFIDENCE_THRESHOLD = 0.7;

    private final CascadeClassifier faceCascade;
    private final CascadeClassifier eyeCascade;
    private final List<FaceDetectionListener> listeners;
    private Mat lastFrame;

    public FaceDetector() {
        this.listeners = new ArrayList<>();
        this.faceCascade = loadCascadeClassifier(FACE_CASCADE_FILE);
        this.eyeCascade = loadCascadeClassifier(EYE_CASCADE_FILE);
        logger.info("FaceDetector initialized successfully");
    }

    private CascadeClassifier loadCascadeClassifier(String cascadeFile) {
        try {
            // Load cascade file from resources
            InputStream is = getClass().getResourceAsStream(cascadeFile);
            if (is == null) {
                throw new RuntimeException("Cannot load cascade file: " + cascadeFile);
            }

            // Create a temporary file
            File tempFile = File.createTempFile("cascade", ".xml");
            try (FileOutputStream os = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            // Create and return the classifier
            CascadeClassifier classifier = new CascadeClassifier(tempFile.getAbsolutePath());
            if (classifier.empty()) {
                throw new RuntimeException("Failed to load cascade classifier");
            }

            // Clean up
            tempFile.delete();
            return classifier;

        } catch (Exception e) {
            logger.error("Error loading cascade classifier", e);
            throw new RuntimeException("Failed to load cascade classifier", e);
        }
    }

    public void processFrame(Mat frame) {
        try {
            this.lastFrame = frame.clone();
            Mat grayFrame = new Mat();
            Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(grayFrame, grayFrame);

            // Detect faces
            MatOfRect faces = new MatOfRect();
            faceCascade.detectMultiScale(grayFrame, faces,
                1.1, 3, 0,
                new Size(30, 30), new Size());

            // Process each detected face
            for (Rect faceRect : faces.toArray()) {
                processFace(frame, grayFrame, faceRect);
            }

            // Clean up
            grayFrame.release();
            faces.release();

            // Notify listeners
            notifyFrameProcessed(frame);

        } catch (Exception e) {
            logger.error("Error processing frame", e);
        }
    }

    private void processFace(Mat frame, Mat grayFrame, Rect faceRect) {
        try {
            // Draw face rectangle
            Imgproc.rectangle(frame, faceRect, new Scalar(0, 255, 0), 2);

            // Extract face ROI
            Mat faceROI = grayFrame.submat(faceRect);

            // Detect eyes
            MatOfRect eyes = new MatOfRect();
            eyeCascade.detectMultiScale(faceROI, eyes);

            // Process eyes
            for (Rect eyeRect : eyes.toArray()) {
                // Adjust eye coordinates to frame coordinates
                eyeRect.x += faceRect.x;
                eyeRect.y += faceRect.y;
                Imgproc.rectangle(frame, eyeRect, new Scalar(255, 0, 0), 2);
            }

            // Analyze facial expression
            analyzeFacialExpression(faceROI);

            // Clean up
            faceROI.release();
            eyes.release();

        } catch (Exception e) {
            logger.error("Error processing face", e);
        }
    }

    private void analyzeFacialExpression(Mat faceROI) {
        try {
            // TODO: Implement more sophisticated facial expression analysis
            // Currently using a simplified approach based on pixel intensity distribution

            // Calculate basic statistics
            MatOfDouble mean = new MatOfDouble();
            MatOfDouble stddev = new MatOfDouble();
            Core.meanStdDev(faceROI, mean, stddev);

            // Simple expression classification based on intensity distribution
            double meanIntensity = mean.get(0, 0)[0];
            double stdDeviation = stddev.get(0, 0)[0];

            String expression;
            double confidence;

            if (stdDeviation > 50) {
                expression = "EXPRESSIVE";
                confidence = Math.min(1.0, stdDeviation / 100.0);
            } else if (meanIntensity > 127) {
                expression = "POSITIVE";
                confidence = Math.min(1.0, meanIntensity / 255.0);
            } else {
                expression = "NEUTRAL";
                confidence = 0.5;
            }

            // Notify listeners if confidence meets threshold
            if (confidence >= CONFIDENCE_THRESHOLD) {
                notifyFacialExpressionDetected(expression, confidence);
            }

            // Clean up
            mean.release();
            stddev.release();

        } catch (Exception e) {
            logger.error("Error analyzing facial expression", e);
        }
    }

    public void addListener(FaceDetectionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(FaceDetectionListener listener) {
        listeners.remove(listener);
    }

    private void notifyFrameProcessed(Mat processedFrame) {
        for (FaceDetectionListener listener : listeners) {
            listener.onFrameProcessed(processedFrame);
        }
    }

    private void notifyFacialExpressionDetected(String expression, double confidence) {
        for (FaceDetectionListener listener : listeners) {
            listener.onFacialExpressionDetected(expression, confidence);
        }
    }

    public interface FaceDetectionListener {
        void onFrameProcessed(Mat processedFrame);
        void onFacialExpressionDetected(String expression, double confidence);
    }
}
