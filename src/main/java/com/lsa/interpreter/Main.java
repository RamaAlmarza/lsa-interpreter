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
        // Initialize OpenCV (using JavaCV)
        try {
            org.bytedeco.javacpp.Loader.load(org.bytedeco.opencv.opencv_java.class);
            logger.info("OpenCV native library loaded successfully");
        } catch (Exception e) {
            logger.error("Failed to load OpenCV native library", e);
            System.exit(1);
        }
        
        // Launch JavaFX application
        launch(args);
    }
}
