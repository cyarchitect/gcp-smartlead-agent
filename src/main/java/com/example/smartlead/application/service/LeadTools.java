package com.example.smartlead.application.service;

import com.example.smartlead.domain.model.GoldenLead;
import com.example.smartlead.domain.model.Lead;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeadTools {

    @Autowired
    private LeadService leadService;

    @Tool("Retrieves all raw, unprocessed sales leads from the database")
    public List<Lead> getRawLeads() {
        return leadService.getRawLeads()
                .collectList()
                .block();
    }

    @Tool("Runs the similarity matching engine to identify groups of duplicate lead candidates. Needs a threshold (e.g. 0.7)")
    public List<List<Lead>> findDuplicateCandidates(double threshold) {
        return leadService.findPotentialDuplicates(threshold)
                .collectList()
                .block();
    }

    @Tool("Enriches a specific lead record with company profile info (industry, company size, tech stack) and a personalized outreach message. Mark status as ENRICHED.")
    public Lead enrichLeadRecord(String leadId, String industry, int companySize, String techStack, String outreachMessage) {
        return leadService.enrichLead(leadId, industry, companySize, techStack, outreachMessage).block();
    }

    @Tool("Merges a list of duplicate raw lead IDs into a single consolidated Golden Record. The primaryLeadId is the lead whose basic contact info should be preserved.")
    public GoldenLead mergeDuplicateLeads(List<String> rawLeadIds, String primaryLeadId) {
        return leadService.mergeLeads(rawLeadIds, primaryLeadId).block();
    }
}
