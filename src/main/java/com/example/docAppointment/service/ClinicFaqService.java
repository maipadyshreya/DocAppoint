
package com.example.docAppointment.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClinicFaqService {

    private final List<String> faqEntries = new ArrayList<>();

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "a", "an", "is", "are", "i", "my",
            "me", "do", "does", "how", "what", "can",
            "to", "for", "of", "in", "on", "and",
            "you", "your", "it", "with", "after"
    );

    public ClinicFaqService() {
        try {
            ClassPathResource resource =
                    new ClassPathResource("clinic-faq.txt");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            resource.getInputStream(),
                            StandardCharsets.UTF_8))) {

                StringBuilder entry = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Q:")) {
                        if (!entry.isEmpty()) {
                            faqEntries.add(entry.toString().trim());
                        }
                        entry = new StringBuilder();
                    }

                    if (line.startsWith("Q:")
                            || line.startsWith("A:")) {
                        entry.append(line).append("\n");
                    }
                }

                if (!entry.isEmpty()) {
                    faqEntries.add(entry.toString().trim());
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to load clinic-faq.txt", e);
        }
    }

    public String searchFaq(String question) {
        Set<String> queryWords = tokenize(question);

        String bestEntry = null;
        long bestScore = 0;

        for (String entry : faqEntries) {
            Set<String> entryWords = tokenize(entry);

            long score = queryWords.stream()
                    .filter(entryWords::contains)
                    .count();

            if (score > bestScore) {
                bestScore = score;
                bestEntry = entry;
            }
        }

        if (bestEntry == null) {
            return "No relevant clinic FAQ information found.";
        }

        return bestEntry;
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(
                        text.toLowerCase()
                                .split("[^a-z0-9]+"))
                .filter(word -> !word.isBlank())
                .filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.toSet());
    }
}
