package com.example.smartlead.presentation.controller;

import com.example.smartlead.application.service.LeadService;
import com.example.smartlead.domain.model.GoldenLead;
import com.example.smartlead.domain.model.Lead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@CrossOrigin(origins = "*") // Allow React frontend to connect during local dev
public class LeadController {

    @Autowired
    private LeadService leadService;

    @PostMapping("/seed")
    public Mono<String> seedLeads() {
        return leadService.seedDemoData()
                .thenReturn("Database successfully seeded with messy leads.");
    }

    @GetMapping
    public Flux<Lead> getLeads() {
        return leadService.getAllLeads();
    }

    @GetMapping("/golden")
    public Flux<GoldenLead> getGoldenLeads() {
        return leadService.getAllGoldenLeads();
    }

    @GetMapping("/duplicates")
    public Flux<List<Lead>> getDuplicates(@RequestParam(defaultValue = "0.7") double threshold) {
        return leadService.findPotentialDuplicates(threshold);
    }

    @PostMapping("/merge")
    public Mono<GoldenLead> merge(@RequestBody MergeRequest request) {
        return leadService.mergeLeads(request.getRawLeadIds(), request.getPrimaryLeadId());
    }

    // Inner class representing the Merge API Payload
    public static class MergeRequest {
        private List<String> rawLeadIds;
        private String primaryLeadId;

        public List<String> getRawLeadIds() {
            return rawLeadIds;
        }

        public void setRawLeadIds(List<String> rawLeadIds) {
            this.rawLeadIds = rawLeadIds;
        }

        public String getPrimaryLeadId() {
            return primaryLeadId;
        }

        public void setPrimaryLeadId(String primaryLeadId) {
            this.primaryLeadId = primaryLeadId;
        }
    }
}
