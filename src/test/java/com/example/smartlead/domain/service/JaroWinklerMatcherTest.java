package com.example.smartlead.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JaroWinklerMatcherTest {

    private JaroWinklerMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new JaroWinklerMatcher();
    }

    @Test
    public void testExactMatch() {
        assertEquals(1.0, matcher.calculateSimilarity("Google", "Google"), 0.0001);
        assertEquals(1.0, matcher.calculateSimilarity("salesforce", "  SALESFORCE  "), 0.0001);
    }

    @Test
    public void testNullOrEmpty() {
        assertEquals(0.0, matcher.calculateSimilarity(null, "Google"), 0.0001);
        assertEquals(0.0, matcher.calculateSimilarity("Google", null), 0.0001);
        assertEquals(0.0, matcher.calculateSimilarity("", "Google"), 0.0001);
        assertEquals(0.0, matcher.calculateSimilarity("Google", ""), 0.0001);
    }

    @Test
    public void testCompletelyDifferent() {
        assertEquals(0.0, matcher.calculateSimilarity("ABCD", "EFGH"), 0.0001);
    }

    @Test
    public void testTypoSimilarity() {
        // "Salesforce" and "Salesforce Inc."
        double score = matcher.calculateSimilarity("Salesforce", "Salesforce Inc.");
        assertTrue(score > 0.8, "Score should be high for similar company names: " + score);
        
        // "John S." and "J. Smith"
        double scoreName = matcher.calculateSimilarity("John S.", "J. Smith");
        assertTrue(scoreName > 0.5, "Score should reflect name variations: " + scoreName);
    }
}
