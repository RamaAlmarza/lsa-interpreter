package com.lsa.interpreter.logic;

import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class FusionAI implements GestureDetector.GestureDetectionListener, DictionaryManager.DictionaryListener {
    private static final Logger logger = LoggerFactory.getLogger(FusionAI.class);
    private static final double CONFIDENCE_THRESHOLD = 0.7;

    private final GestureDetector gestureDetector;
    private final FaceDetector faceDetector;
    private final GrammarProcessor grammarProcessor;
    private final List<FusionListener> listeners;
    private DetectionResult lastGestureResult;
    private DetectionResult lastFaceResult;

    public FusionAI() {
        this.gestureDetector = new GestureDetector();
        this.faceDetector = new FaceDetector();
        this.grammarProcessor = new GrammarProcessor();
        this.listeners = new ArrayList<>();

        // Set up internal listeners
        gestureDetector.addListener(this);
        faceDetector.addListener(new FaceDetector.FaceDetectionListener() {
            @Override
            public void onFrameProcessed(Mat processedFrame) {
                // Handle processed frame if needed
            }

            @Override
            public void onFacialExpressionDetected(String expression, double confidence) {
                handleFacialExpression(expression, confidence);
            }
        });

        logger.info("FusionAI initialized successfully");
    }

    public Mat processFrame(Mat frame) {
        try {
            // Process frame through both detectors
            gestureDetector.processFrame(frame);
            faceDetector.processFrame(frame);

            // Return the processed frame (with annotations)
            return frame;
        } catch (Exception e) {
            logger.error("Error processing frame in FusionAI", e);
            return frame;
        }
    }

    @Override
    public void onGestureDetected(int fingerCount) {
        lastGestureResult = new DetectionResult(
            DetectionType.GESTURE,
            String.valueOf(fingerCount),
            calculateGestureConfidence(fingerCount)
        );
        
        fusionAnalysis();
    }

    private void handleFacialExpression(String expression, double confidence) {
        lastFaceResult = new DetectionResult(
            DetectionType.FACIAL_EXPRESSION,
            expression,
            confidence
        );
        
        fusionAnalysis();
    }

    private void fusionAnalysis() {
        if (lastGestureResult != null && lastFaceResult != null) {
            // Combine results from both detectors
            FusionResult result = new FusionResult(
                interpretSign(),
                calculateCombinedConfidence()
            );

            // Process through grammar processor
            result = grammarProcessor.processResult(result);

            // Notify listeners
            notifyListeners(result);

            // Reset results
            lastGestureResult = null;
            lastFaceResult = null;
        }
    }

    private String interpretSign() {
        // TODO: Implement more sophisticated sign interpretation
        return lastGestureResult.value + "_" + lastFaceResult.value;
    }

    private double calculateGestureConfidence(int fingerCount) {
        // TODO: Implement more sophisticated confidence calculation
        return fingerCount > 0 && fingerCount <= 5 ? 0.8 : 0.3;
    }

    private double calculateCombinedConfidence() {
        return (lastGestureResult.confidence + lastFaceResult.confidence) / 2.0;
    }

    public void addListener(FusionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(FusionListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(FusionResult result) {
        if (result.getConfidence() >= CONFIDENCE_THRESHOLD) {
            for (FusionListener listener : listeners) {
                listener.onDetectionResult(result);
            }
            logger.debug("Detection result: {}", result);
        }
    }

    public interface FusionListener {
        void onDetectionResult(FusionResult result);
    }

    public static class FusionResult {
        private final String detectedSign;
        private final double confidence;

        public FusionResult(String detectedSign, double confidence) {
            this.detectedSign = detectedSign;
            this.confidence = confidence;
        }

        public String getDetectedSign() {
            return detectedSign;
        }

        public double getConfidence() {
            return confidence;
        }

        @Override
        public String toString() {
            return String.format("Sign: %s (Confidence: %.2f)", detectedSign, confidence);
        }
    }

    private static class DetectionResult {
        private final DetectionType type;
        private final String value;
        private final double confidence;

        public DetectionResult(DetectionType type, String value, double confidence) {
            this.type = type;
            this.value = value;
            this.confidence = confidence;
        }
    }

    public enum DetectionType {
        GESTURE,
        FACIAL_EXPRESSION
    }
}
