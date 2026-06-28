package com.example.smartlead.application.service;

import com.example.smartlead.domain.model.GoldenLead;
import com.example.smartlead.domain.model.Lead;
import com.example.smartlead.domain.service.JaroWinklerMatcher;
import com.example.smartlead.infrastructure.adapter.GoldenLeadRepository;
import com.example.smartlead.infrastructure.adapter.LeadRepository;
import com.example.smartlead.infrastructure.config.EventPublisher;
import com.example.smartlead.infrastructure.config.MockLeadAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private GoldenLeadRepository goldenLeadRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Spy
    private JaroWinklerMatcher similarityMatcher = new JaroWinklerMatcher();

    @InjectMocks
    private LeadService leadService;

    private Lead lead1;
    private Lead lead2;

    @BeforeEach
    public void setUp() {
        lead1 = Lead.builder()
                .id("1")
                .name("John S.")
                .company("Google")
                .email("john.smith@google.com")
                .status("RAW")
                .createdAt(System.currentTimeMillis())
                .build();

        lead2 = Lead.builder()
                .id("2")
                .name("John S.")
                .company("Google Inc.")
                .email("john.smith@google.com")
                .status("RAW")
                .createdAt(System.currentTimeMillis())
                .build();
    }

    @Test
    public void testGetRawLeads() {
        when(leadRepository.findByStatus("RAW")).thenReturn(Flux.just(lead1, lead2));

        List<Lead> rawLeads = leadService.getRawLeads().collectList().block();

        assertNotNull(rawLeads);
        assertEquals(2, rawLeads.size());
        assertEquals("John S.", rawLeads.get(0).getName());
    }

    @Test
    public void testFindPotentialDuplicates() {
        when(leadRepository.findByStatus("RAW")).thenReturn(Flux.just(lead1, lead2));

        List<List<Lead>> duplicates = leadService.findPotentialDuplicates(0.7).collectList().block();

        assertNotNull(duplicates);
        assertEquals(1, duplicates.size());
        assertEquals(2, duplicates.get(0).size());
    }

    @Test
    public void testMergeLeads() {
        when(leadRepository.findById("1")).thenReturn(Mono.just(lead1));
        when(leadRepository.findById("2")).thenReturn(Mono.just(lead2));
        when(goldenLeadRepository.save(any(GoldenLead.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        GoldenLead golden = leadService.mergeLeads(Arrays.asList("1", "2"), "1").block();

        assertNotNull(golden);
        assertEquals("John S.", golden.getName());
        assertEquals("Google", golden.getCompany());
        assertEquals("john.smith@google.com", golden.getEmail());
        assertTrue(golden.getMergedRecordIds().contains("1"));
        assertTrue(golden.getMergedRecordIds().contains("2"));

        verify(eventPublisher, times(1)).publish(any(GoldenLead.class));
        verify(leadRepository, times(2)).save(any(Lead.class));
    }

    @Test
    public void testEnrichLead() {
        when(leadRepository.findById("1")).thenReturn(Mono.just(lead1));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Lead enriched = leadService.enrichLead("1", "SaaS", 150000, "Java", "Hello").block();

        assertNotNull(enriched);
        assertEquals("ENRICHED", enriched.getStatus());
        assertEquals("SaaS", enriched.getEnrichedIndustry());
        assertEquals(150000, enriched.getEnrichedSize());
        assertEquals("Java", enriched.getEnrichedTechStack());
        assertEquals("Hello", enriched.getOutreachMessage());

        verify(eventPublisher, times(1)).publish(any(Lead.class));
    }

    @Test
    public void testMockEnrichmentFlow() {
        when(leadRepository.findByStatus("RAW")).thenReturn(Flux.just(lead1));
        when(leadRepository.findById("1")).thenReturn(Mono.just(lead1));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        MockLeadAgent mockAgent = new MockLeadAgent(leadService);
        String reply = mockAgent.chat("enrich all raw leads");

        assertNotNull(reply);
        assertTrue(reply.contains("Successfully enriched"));
        assertTrue(reply.contains("John S."));
        assertTrue(reply.contains("Search & Cloud SaaS"));

        verify(eventPublisher, times(1)).publish(any(Lead.class));
    }
}
