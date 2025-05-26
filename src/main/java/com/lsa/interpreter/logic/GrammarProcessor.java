package com.lsa.interpreter.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GrammarProcessor {
    private static final Logger logger = LoggerFactory.getLogger(GrammarProcessor.class);
    private static final int MAX_HISTORY_SIZE = 10;
    private static final double SEQUENCE_CONFIDENCE_THRESHOLD = 0.75;

    private final Queue<WordInfo> wordHistory;
    private final List<ProcessingListener> listeners;
    private final Map<String, List<String>> grammarRules;

    public GrammarProcessor() {
        this.wordHistory = new ConcurrentLinkedQueue<>();
        this.listeners = new ArrayList<>();
        this.grammarRules = initializeGrammarRules();

        logger.info("GrammarProcessor initialized successfully");
    }

    public FusionAI.FusionResult processResult(FusionAI.FusionResult result) {
        try {
            // Add new word to history
            WordInfo newWord = new WordInfo(result.getDetectedSign(), result.getConfidence());
            addToHistory(newWord);

            // Apply grammar rules
            String processedSign = applyGrammarRules(result.getDetectedSign());
            double adjustedConfidence = adjustConfidence(result.getConfidence(), processedSign);

            // Create processed result
            FusionAI.FusionResult processedResult = new FusionAI.FusionResult(
                processedSign,
                adjustedConfidence
            );

            // Notify listeners
            notifyListeners(processedResult);

            return processedResult;
        } catch (Exception e) {
            logger.error("Error processing result", e);
            return result; // Return original result if processing fails
        }
    }

    private Map<String, List<String>> initializeGrammarRules() {
        Map<String, List<String>> rules = new HashMap<>();
        
        // Basic LSA grammar rules (simplified example)
        rules.put("SUBJECT", Arrays.asList("I", "YOU", "HE", "SHE", "WE", "THEY"));
        rules.put("VERB", Arrays.asList("AM", "IS", "ARE", "GO", "WANT", "LIKE"));
        rules.put("OBJECT", Arrays.asList("FOOD", "WATER", "HOME", "SCHOOL", "FRIEND"));

        return rules;
    }

    private void addToHistory(WordInfo word) {
        wordHistory.offer(word);
        while (wordHistory.size() > MAX_HISTORY_SIZE) {
            wordHistory.poll();
        }
    }

    private String applyGrammarRules(String sign) {
        // Get last few signs to establish context
        List<String> recentSigns = getRecentSigns(3);
        
        // Apply basic grammar rules
        if (recentSigns.size() >= 2) {
            String previousSign = recentSigns.get(recentSigns.size() - 2);
            
            // Example rule: If we have a subject followed by a verb-like sign,
            // ensure proper subject-verb agreement
            if (isInCategory(previousSign, "SUBJECT")) {
                sign = adjustVerbAgreement(previousSign, sign);
            }
        }

        return sign;
    }

    private List<String> getRecentSigns(int count) {
        List<String> signs = new ArrayList<>();
        for (WordInfo word : wordHistory) {
            signs.add(word.getWord());
            if (signs.size() >= count) break;
        }
        return signs;
    }

    private boolean isInCategory(String sign, String category) {
        return grammarRules.containsKey(category) && 
               grammarRules.get(category).contains(sign);
    }

    private String adjustVerbAgreement(String subject, String verb) {
        // Example verb agreement rules
        if (verb.startsWith("IS") || verb.startsWith("ARE")) {
            if (subject.equals("I")) {
                return "AM";
            } else if (subject.equals("YOU") || subject.equals("WE") || subject.equals("THEY")) {
                return "ARE";
            } else {
                return "IS";
            }
        }
        return verb;
    }

    private double adjustConfidence(double originalConfidence, String processedSign) {
        // Adjust confidence based on grammar context
        double contextBonus = calculateContextBonus();
        return Math.min(1.0, originalConfidence + contextBonus);
    }

    private double calculateContextBonus() {
        // Calculate a confidence bonus based on grammatical consistency
        if (wordHistory.size() < 2) return 0.0;

        int validSequences = 0;
        int totalSequences = 0;

        Iterator<WordInfo> iterator = wordHistory.iterator();
        WordInfo previous = iterator.next();

        while (iterator.hasNext()) {
            WordInfo current = iterator.next();
            if (isValidSequence(previous.getWord(), current.getWord())) {
                validSequences++;
            }
            totalSequences++;
            previous = current;
        }

        return totalSequences > 0 ? 
            (double) validSequences / totalSequences * 0.1 : // Max 10% bonus
            0.0;
    }

    private boolean isValidSequence(String first, String second) {
        // Check if the sequence follows basic grammar rules
        return (isInCategory(first, "SUBJECT") && isInCategory(second, "VERB")) ||
               (isInCategory(first, "VERB") && isInCategory(second, "OBJECT"));
    }

    public void addListener(ProcessingListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ProcessingListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(FusionAI.FusionResult result) {
        for (ProcessingListener listener : listeners) {
            listener.onResultProcessed(result);
        }
    }

    public interface ProcessingListener {
        void onResultProcessed(FusionAI.FusionResult result);
    }

    private static class WordInfo {
        private final String word;
        private final double confidence;
        private final long timestamp;

        public WordInfo(String word, double confidence) {
            this.word = word;
            this.confidence = confidence;
            this.timestamp = System.currentTimeMillis();
        }

        public String getWord() {
            return word;
        }

        public double getConfidence() {
            return confidence;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
