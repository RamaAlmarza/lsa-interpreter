package com.lsa.interpreter.ui;

import com.lsa.interpreter.logic.FusionAI;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistorySidebar implements FusionAI.FusionListener {
    private static final Logger logger = LoggerFactory.getLogger(HistorySidebar.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final VBox view;
    private final ListView<HistoryItem> historyList;
    private final Label statisticsLabel;
    private final List<HistoryItem> history;

    public HistorySidebar() {
        history = new ArrayList<>();

        // Initialize history list
        historyList = new ListView<>();
        historyList.getStyleClass().add("history-list");
        historyList.setCellFactory(list -> new HistoryItemCell());

        // Initialize statistics label
        statisticsLabel = new Label("Total detections: 0");
        statisticsLabel.getStyleClass().add("statistics-label");

        // Initialize clear button
        Button clearButton = new Button("Clear History");
        clearButton.getStyleClass().add("clear-button");
        clearButton.setOnAction(e -> clearHistory());

        // Create main layout
        view = new VBox(10);
        view.setPadding(new Insets(10));
        view.getChildren().addAll(historyList, statisticsLabel, clearButton);
        view.getStyleClass().add("history-sidebar");

        logger.info("HistorySidebar initialized successfully");
    }

    public VBox getView() {
        return view;
    }

    @Override
    public void onDetectionResult(FusionAI.FusionResult result) {
        Platform.runLater(() -> {
            HistoryItem item = new HistoryItem(
                result.getDetectedSign(),
                result.getConfidence(),
                LocalDateTime.now()
            );
            history.add(0, item);
            historyList.getItems().add(0, item);
            updateStatistics();
            logger.debug("New detection added to history: {}", item);
        });
    }

    private void clearHistory() {
        history.clear();
        historyList.getItems().clear();
        updateStatistics();
        logger.info("History cleared");
    }

    private void updateStatistics() {
        statisticsLabel.setText(String.format("Total detections: %d", history.size()));
    }

    private static class HistoryItem {
        private final String sign;
        private final double confidence;
        private final LocalDateTime timestamp;

        public HistoryItem(String sign, double confidence, LocalDateTime timestamp) {
            this.sign = sign;
            this.confidence = confidence;
            this.timestamp = timestamp;
        }

        public String getSign() {
            return sign;
        }

        public double getConfidence() {
            return confidence;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s (%.1f%%)",
                timestamp.format(TIME_FORMATTER),
                sign,
                confidence * 100
            );
        }
    }

    private static class HistoryItemCell extends ListCell<HistoryItem> {
        @Override
        protected void updateItem(HistoryItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                getStyleClass().removeAll("high-confidence", "medium-confidence", "low-confidence");
            } else {
                setText(item.toString());
                
                // Apply confidence-based styling
                getStyleClass().removeAll("high-confidence", "medium-confidence", "low-confidence");
                if (item.getConfidence() >= 0.8) {
                    getStyleClass().add("high-confidence");
                } else if (item.getConfidence() >= 0.5) {
                    getStyleClass().add("medium-confidence");
                } else {
                    getStyleClass().add("low-confidence");
                }
            }
        }
    }
}
