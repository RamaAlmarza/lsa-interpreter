package com.lsa.interpreter.logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DictionaryManager {
    private static final Logger logger = LoggerFactory.getLogger(DictionaryManager.class);
    private static final String DICTIONARY_FILE = "/dictionary/lsa_dictionary.json";

    private final List<SignEntry> dictionary;
    private final List<DictionaryListener> listeners;
    private final ObjectMapper objectMapper;

    public DictionaryManager() {
        this.dictionary = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.objectMapper = new ObjectMapper();
        
        logger.info("DictionaryManager initialized");
    }

    public void loadDictionary() {
        try {
            InputStream is = getClass().getResourceAsStream(DICTIONARY_FILE);
            if (is == null) {
                throw new RuntimeException("Dictionary file not found: " + DICTIONARY_FILE);
            }

            JsonNode root = objectMapper.readTree(is);
            if (!root.isArray()) {
                throw new RuntimeException("Invalid dictionary format");
            }

            dictionary.clear();
            for (JsonNode entry : root) {
                dictionary.add(new SignEntry(
                    entry.get("sign").asText(),
                    entry.get("description").asText(),
                    entry.get("videoUrl").asText(),
                    entry.get("tags").toString()
                ));
            }

            logger.info("Dictionary loaded successfully with {} entries", dictionary.size());
            notifyDictionaryUpdated();
        } catch (Exception e) {
            logger.error("Failed to load dictionary", e);
            throw new RuntimeException("Failed to load dictionary", e);
        }
    }

    public List<SignEntry> searchSigns(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(dictionary);
        }

        String normalizedQuery = query.toLowerCase().trim();
        return dictionary.stream()
            .filter(entry -> matchesQuery(entry, normalizedQuery))
            .collect(Collectors.toList());
    }

    private boolean matchesQuery(SignEntry entry, String query) {
        return entry.getSign().toLowerCase().contains(query) ||
               entry.getDescription().toLowerCase().contains(query) ||
               entry.getTags().toLowerCase().contains(query);
    }

    public SignEntry findSign(String sign) {
        return dictionary.stream()
            .filter(entry -> entry.getSign().equalsIgnoreCase(sign))
            .findFirst()
            .orElse(null);
    }

    public List<SignEntry> getAllSigns() {
        return Collections.unmodifiableList(dictionary);
    }

    public void addListener(DictionaryListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DictionaryListener listener) {
        listeners.remove(listener);
    }

    private void notifyDictionaryUpdated() {
        for (DictionaryListener listener : listeners) {
            listener.onDictionaryUpdated(dictionary);
        }
    }

    public interface DictionaryListener {
        void onDictionaryUpdated(List<SignEntry> dictionary);
    }

    public static class SignEntry {
        private final String sign;
        private final String description;
        private final String videoUrl;
        private final String tags;

        public SignEntry(String sign, String description, String videoUrl, String tags) {
            this.sign = sign;
            this.description = description;
            this.videoUrl = videoUrl;
            this.tags = tags;
        }

        public String getSign() {
            return sign;
        }

        public String getDescription() {
            return description;
        }

        public String getVideoUrl() {
            return videoUrl;
        }

        public String getTags() {
            return tags;
        }

        @Override
        public String toString() {
            return sign;
        }
    }
}
