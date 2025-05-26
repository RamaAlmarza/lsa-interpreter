package com.lsa.interpreter.ui;

import com.lsa.interpreter.logic.DictionaryManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictionaryUI {
    private static final Logger logger = LoggerFactory.getLogger(DictionaryUI.class);

    private final VBox view;
    private final TextField searchField;
    private final ListView<DictionaryManager.SignEntry> signList;
    private final Label descriptionLabel;
    private final MediaView videoPlayer;
    private MediaPlayer mediaPlayer;
    private final DictionaryManager dictionaryManager;

    public DictionaryUI() {
        dictionaryManager = new DictionaryManager();

        // Initialize search field
        searchField = new TextField();
        searchField.setPromptText("Search signs...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, oldText, newText) -> updateSignList(newText));

        // Initialize sign list
        signList = new ListView<>();
        signList.getStyleClass().add("dictionary-list");
        signList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showSignDetails(newVal);
            }
        });

        // Initialize description label
        descriptionLabel = new Label();
        descriptionLabel.getStyleClass().add("description-label");
        descriptionLabel.setWrapText(true);

        // Initialize video player
        videoPlayer = new MediaView();
        videoPlayer.getStyleClass().add("video-player");
        videoPlayer.setFitWidth(320);
        videoPlayer.setFitHeight(240);

        // Create main layout
        view = new VBox(10);
        view.setPadding(new Insets(10));
        view.getChildren().addAll(
            searchField,
            signList,
            videoPlayer,
            descriptionLabel
        );
        view.getStyleClass().add("dictionary-ui");

        // Load initial dictionary data
        loadDictionary();

        logger.info("DictionaryUI initialized successfully");
    }

    public VBox getView() {
        return view;
    }

    public void addDictionaryListener(DictionaryManager.DictionaryListener listener) {
        dictionaryManager.addListener(listener);
    }

    private void loadDictionary() {
        try {
            dictionaryManager.loadDictionary();
            updateSignList("");
            logger.info("Dictionary loaded successfully");
        } catch (Exception e) {
            logger.error("Failed to load dictionary", e);
            showError("Failed to load dictionary");
        }
    }

    private void updateSignList(String searchText) {
        signList.getItems().clear();
        signList.getItems().addAll(dictionaryManager.searchSigns(searchText));
    }

    private void showSignDetails(DictionaryManager.SignEntry sign) {
        descriptionLabel.setText(sign.getDescription());

        // Stop current video if playing
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        // Load and play new video
        try {
            Media media = new Media(sign.getVideoUrl());
            mediaPlayer = new MediaPlayer(media);
            videoPlayer.setMediaPlayer(mediaPlayer);
            mediaPlayer.play();
        } catch (Exception e) {
            logger.error("Failed to load sign video", e);
            showError("Failed to load video");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
