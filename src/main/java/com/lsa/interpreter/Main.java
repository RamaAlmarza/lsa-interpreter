package com.lsa.interpreter;

import com.lsa.interpreter.ui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            MainWindow mainWindow = new MainWindow();
            mainWindow.show(primaryStage);
            logger.info("LSA Interpreter application started successfully");
        } catch (Exception e) {
            logger.error("Failed to start LSA Interpreter application", e);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        // Load OpenCV native library
        nu.pattern.OpenCV.loadShared();
        logger.info("OpenCV native library loaded successfully");
        
        // Launch JavaFX application
        launch(args);
    }
}
