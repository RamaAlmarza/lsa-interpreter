package com.lsa.interpreter.ui;

import com.lsa.interpreter.logic.FusionAI;
import com.lsa.interpreter.util.VideoUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetectorUI {
    private static final Logger logger = LoggerFactory.getLogger(DetectorUI.class);
    private static final double VIDEO_WIDTH = 640;
    private static final double VIDEO_HEIGHT = 480;

    private final VBox view;
    private final ImageView videoFeed;
    private final Label statusLabel;
    private final Button startButton;
    private final Button stopButton;
    private final FusionAI fusionAI;
    private volatile boolean isRunning;

    public DetectorUI(FusionAI fusionAI) {
        this.fusionAI = fusionAI;
        this.isRunning = false;

        // Initialize UI components
        videoFeed = new ImageView();
        videoFeed.setFitWidth(VIDEO_WIDTH);
        videoFeed.setFitHeight(VIDEO_HEIGHT);
        videoFeed.getStyleClass().add("video-feed");

        statusLabel = new Label("Ready to start detection");
        statusLabel.getStyleClass().add("status-label");

        startButton = new Button("Start Detection");
        startButton.getStyleClass().add("control-button");
        startButton.setOnAction(e -> startDetection());

        stopButton = new Button("Stop Detection");
        stopButton.getStyleClass().add("control-button");
        stopButton.setOnAction(e -> stopDetection());
        stopButton.setDisable(true);

        // Create control panel
        HBox controls = new HBox(10);
        controls.getChildren().addAll(startButton, stopButton);

        // Create main layout
        view = new VBox(10);
        view.setPadding(new Insets(10));
        view.getChildren().addAll(videoFeed, controls, statusLabel);
        view.getStyleClass().add("detector-ui");

        logger.info("DetectorUI initialized successfully");
    }

    public VBox getView() {
        return view;
    }

    private void startDetection() {
        try {
            isRunning = true;
            startButton.setDisable(true);
            stopButton.setDisable(false);
            statusLabel.setText("Detection running...");

            // Start detection in a separate thread
            Thread detectionThread = new Thread(this::processVideoFeed);
            detectionThread.setDaemon(true);
            detectionThread.start();

            logger.info("Detection started");
        } catch (Exception e) {
            logger.error("Failed to start detection", e);
            statusLabel.setText("Error starting detection");
        }
    }

    private void stopDetection() {
        isRunning = false;
        startButton.setDisable(false);
        stopButton.setDisable(true);
        statusLabel.setText("Detection stopped");
        logger.info("Detection stopped");
    }

    private void processVideoFeed() {
        // TODO: Replace with actual video capture
        while (isRunning) {
            try {
                // Simulate video processing
                Mat frame = new Mat(); // Replace with actual frame capture
                
                // Process frame through FusionAI
                Mat processedFrame = fusionAI.processFrame(frame);
                
                // Update UI with processed frame
                Platform.runLater(() -> {
                    videoFeed.setImage(VideoUtils.matToImage(processedFrame));
                });

                Thread.sleep(33); // ~30 FPS
            } catch (InterruptedException e) {
                logger.error("Video processing interrupted", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error processing video frame", e);
            }
        }
    }
}
