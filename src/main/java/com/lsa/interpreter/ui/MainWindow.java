package com.lsa.interpreter.ui;

import com.lsa.interpreter.logic.DictionaryManager;
import java.util.List;

import com.lsa.interpreter.logic.FusionAI;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainWindow {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);
    private static final String WINDOW_TITLE = "LSA Interpreter";
    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 720;

    private final DetectorUI detectorUI;
    private final DictionaryUI dictionaryUI;
    private final HistorySidebar historySidebar;
    private final FusionAI fusionAI;

    public MainWindow() {
        fusionAI = new FusionAI();
        detectorUI = new DetectorUI(fusionAI);
        dictionaryUI = new DictionaryUI();
        historySidebar = new HistorySidebar();

        // Set up communication between components
        setupComponentInteractions();
        
        // Set up dictionary listener
        dictionaryUI.addDictionaryListener(dictionary -> {
            // Forward dictionary updates to FusionAI if needed
            logger.info("Dictionary updated with {} entries", dictionary.size());
        });
    }

    public void show(Stage stage) {
        try {
            BorderPane root = new BorderPane();
            
            // Create main content split pane
            SplitPane mainContent = new SplitPane();
            mainContent.getItems().addAll(detectorUI.getView(), dictionaryUI.getView());
            mainContent.setDividerPositions(0.7);

            // Create right sidebar split pane
            SplitPane rightSidebar = new SplitPane();
            rightSidebar.getItems().add(historySidebar.getView());
            
            // Set up layout
            root.setCenter(mainContent);
            root.setRight(rightSidebar);

            // Create and configure scene
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

            // Configure and show stage
            stage.setTitle(WINDOW_TITLE);
            stage.setScene(scene);
            stage.show();

            logger.info("Main window initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize main window", e);
            throw new RuntimeException("Failed to initialize main window", e);
        }
    }

    private void setupComponentInteractions() {
        // Set up listeners and event handlers between components
        fusionAI.addListener(historySidebar);
        // Dictionary updates are handled through lambda in constructor
    }
}
