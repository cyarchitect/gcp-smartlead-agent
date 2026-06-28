package com.example.smartlead.domain.service;

import org.springframework.stereotype.Service;


@Service
public class JaroWinklerMatcher {

    private static final double DEFAULT_SCALING_FACTOR = 0.1;
    private static final int PREFIX_LIMIT = 4;

    /**
     * Calculates the Jaro-Winkler similarity score between two strings.
     * Returns a score between 0.0 (completely different) and 1.0 (exact match).
     */
    public double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        s1 = s1.trim().toLowerCase();
        s2 = s2.trim().toLowerCase();

        if (s1.equals(s2)) {
            return 1.0;
        }

        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 == 0 || len2 == 0) {
            return 0.0;
        }

        // 1. Calculate Jaro Distance
        int matchDistance = Math.max(0, Math.max(len1, len2) / 2 - 1);

        boolean[] s1Matches = new boolean[len1];
        boolean[] s2Matches = new boolean[len2];

        int matches = 0;
        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, len2);

            for (int j = start; j < end; j++) {
                if (s2Matches[j]) {
                    continue;
                }
                if (s1.charAt(i) == s2.charAt(j)) {
                    s1Matches[i] = true;
                    s2Matches[j] = true;
                    matches++;
                    break;
                }
            }
        }

        if (matches == 0) {
            return 0.0;
        }

        // 2. Calculate Transpositions
        int transpositions = 0;
        int k = 0;
        for (int i = 0; i < len1; i++) {
            if (!s1Matches[i]) {
                continue;
            }
            while (!s2Matches[k]) {
                k++;
            }
            if (s1.charAt(i) != s2.charAt(k)) {
                transpositions++;
            }
            k++;
        }

        double jaro = (((double) matches / len1) +
                ((double) matches / len2) +
                (((double) matches - (transpositions / 2.0)) / matches)) / 3.0;

        // 3. Calculate Jaro-Winkler Prefix Scale
        int prefixLength = 0;
        for (int i = 0; i < Math.min(PREFIX_LIMIT, Math.min(len1, len2)); i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                prefixLength++;
            } else {
                break;
            }
        }

        return jaro + (DEFAULT_SCALING_FACTOR * prefixLength * (1.0 - jaro));
    }
}
