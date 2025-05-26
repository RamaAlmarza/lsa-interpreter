package com.lsa.interpreter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ErrorLogger {
    private static final Logger logger = LoggerFactory.getLogger(ErrorLogger.class);
    private static final int MAX_ERROR_HISTORY = 100;
    
    private static final ErrorLogger instance = new ErrorLogger();
    private final ConcurrentLinkedQueue<ErrorEntry> errorHistory;

    private ErrorLogger() {
        this.errorHistory = new ConcurrentLinkedQueue<>();
        logger.info("ErrorLogger initialized");
    }

    public static ErrorLogger getInstance() {
        return instance;
    }

    public void logError(String component, String message, Throwable error) {
        ErrorEntry entry = new ErrorEntry(component, message, error);
        
        // Add to history
        errorHistory.add(entry);
        
        // Trim history if needed
        while (errorHistory.size() > MAX_ERROR_HISTORY) {
            errorHistory.poll();
        }
        
        // Log to SLF4J
        logger.error("[{}] {}: {}", component, message, error.getMessage(), error);
    }

    public void logError(String component, String message) {
        logError(component, message, new Exception(message));
    }

    public List<ErrorEntry> getRecentErrors() {
        List<ErrorEntry> errors = new ArrayList<>(errorHistory);
        Collections.reverse(errors);
        return Collections.unmodifiableList(errors);
    }

    public void clearHistory() {
        errorHistory.clear();
        logger.info("Error history cleared");
    }

    public static class ErrorEntry {
        private final String component;
        private final String message;
        private final Throwable error;
        private final LocalDateTime timestamp;

        public ErrorEntry(String component, String message, Throwable error) {
            this.component = component;
            this.message = message;
            this.error = error;
            this.timestamp = LocalDateTime.now();
        }

        public String getComponent() {
            return component;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getError() {
            return error;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getStackTrace() {
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement element : error.getStackTrace()) {
                sb.append("\tat ").append(element.toString()).append("\n");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return String.format("[%s] %s: %s - %s",
                timestamp,
                component,
                message,
                error.getMessage()
            );
        }
    }
}
